![Verity.io](resources/verityio_logo.svg)

## Verity.io — ML-Powered Trust Layer for Wallet Transfers

Verity.io is an explainable, real-time risk decision system for digital wallet transfers. It helps wallets intercept risky payments before money moves, while preserving user trust through clear explanations and recovery-first actions.

## Problem We Solve

Wallet users, especially thin-file and low-digital-literacy users, are vulnerable to scam-driven transfers. Existing controls often fail in two ways: they miss risky payments, or they block legitimate ones without clear reasons. Both outcomes reduce trust and increase support burden.

## Proposed Solution

Verity.io evaluates transfer context using recipient trust, device continuity, transaction amount, and journey signals, then returns a stable API contract:

- `risk_score`
- `action` (`approve`, `hold`, `review`, `block`)
- `reason_codes`
- `explanation`
- `review_priority`
- `model_version`

## Primary Validation Scenario

- API returns an explainable `hold` decision for a risky transfer pattern (new recipient + high amount + new device).
- Frontend surfaces `reason_codes` and `explanation` with recovery-first actions (`Verify it's you` / `Cancel transfer`).

## Architecture Snapshot

- **Frontend**: Android (Kotlin + Compose) wallet flow
- **Backend**: FastAPI decision API
- **Decision pipeline**: feature assembly → scoring layer → policy/explanation mapping
- **Integration goal**: stable API contract even as scoring internals evolve

For detailed backend architecture and endpoint contract, see [backend/README.md](backend/README.md).

## Repository Structure

- [frontend](frontend) — Mobile app demo flow
- [backend](backend) — Risk decision API

## Quick Run (Windows)

### 1) Run Backend

```powershell
cd backend
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

### 2) Open Frontend

1. Open Android Studio
2. Open the [frontend](frontend) folder
3. Sync Gradle and run the `app` module on emulator/device

## Key Links

- Backend API docs (Swagger): `http://127.0.0.1:8000/docs`
- OpenAPI JSON: `http://127.0.0.1:8000/openapi.json`
- Backend architecture + API details: [backend/README.md](backend/README.md)
