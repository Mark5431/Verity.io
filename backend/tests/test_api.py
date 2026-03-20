from fastapi.testclient import TestClient

from app.main import app
from app.core import config

client = TestClient(app)


def test_live():
    r = client.get("/api/live")
    assert r.status_code == 200
    assert r.json() == {"status": "alive"}


def test_ready():
    r = client.get("/api/ready")
    assert r.status_code == 200
    assert r.json() == {"status": "ready"}


def test_metadata():
    r = client.get("/api/metadata")
    assert r.status_code == 200
    data = r.json()
    assert data["name"] == config.settings.MODEL_NAME
    assert data["version"] == config.settings.MODEL_VERSION
    assert "inputs" in data and "outputs" in data


def test_metrics():
    r = client.get("/api/metrics")
    assert r.status_code == 200
    assert "fraud_decision_requests_total" in r.text


def test_docs_info():
    r = client.get("/api/docs-info")
    assert r.status_code == 200
    assert r.json().get("swagger_ui") == "/docs"


def test_docs_root():
    r = client.get("/docs")
    assert r.status_code == 200
    assert "text/html" in r.headers.get("content-type", "")


def test_decision_hero_scenario_returns_hold():
    payload = {
        "transaction_id": "txn_demo_0001",
        "user": {
            "user_id": "usr_demo_aina_01",
            "account_age_days": 9,
            "recent_tx_count": 5,
        },
        "recipient": {
            "payee_id": "payee_maju_001",
            "payee_name": "Maju Services Enterprise",
            "is_new_payee": True,
            "previous_transfer_count": 0,
        },
        "device": {
            "device_id": "device_demo_new_01",
            "is_new_device": True,
            "session_anomaly_count": 1,
        },
        "transaction": {
            "tx_type": "wallet_transfer",
            "amount": 680.0,
            "currency": "MYR",
            "timestamp": "2026-03-08T14:22:31+08:00",
        },
        "journey": {
            "payee_added_this_session": True,
            "otp_retry_count": 2,
            "recent_support_contact": False,
        },
    }
    r = client.post("/api/decision", json=payload)
    assert r.status_code == 200
    data = r.json()
    assert data["action"] == "hold"
    assert data["review_priority"] == "high"
    assert data["model_version"] == config.settings.MODEL_VERSION
    assert data["risk_score"] >= 0.8
    assert data["reason_codes"] == [
        "NEW_RECIPIENT_HIGH_VALUE",
        "NEW_DEVICE",
        "PAYEE_ADDED_THIS_SESSION",
        "HIGH_OTP_RETRY",
        "LOW_ACCOUNT_AGE",
    ]
    assert (
        data["explanation"]
        == "This payment was paused because it is a first high-value transfer to a new recipient from a new device."
    )


def test_risk_evaluate_alias_matches_decision_contract():
    payload = {
        "transaction_id": "txn_demo_0002",
        "user": {
            "user_id": "usr_demo_safe_01",
            "account_age_days": 120,
            "recent_tx_count": 42,
        },
        "recipient": {
            "payee_id": "payee_known_001",
            "payee_name": "Known Merchant",
            "is_new_payee": False,
            "previous_transfer_count": 9,
        },
        "device": {
            "device_id": "device_known_01",
            "is_new_device": False,
            "session_anomaly_count": 0,
        },
        "transaction": {
            "tx_type": "wallet_transfer",
            "amount": 24.5,
            "currency": "MYR",
            "timestamp": "2026-03-08T14:22:31+08:00",
        },
        "journey": {
            "payee_added_this_session": False,
            "otp_retry_count": 0,
            "recent_support_contact": False,
        },
    }
    r = client.post("/api/risk-evaluate", json=payload)
    assert r.status_code == 200
    data = r.json()
    assert data["action"] == "approve"
    assert data["review_priority"] == "none"
    assert data["reason_codes"] == []


def test_invalid_payload_is_rejected():
    payload = {"transaction_id": "txn_bad_01"}
    r = client.post("/api/decision", json=payload)
    assert r.status_code == 422
