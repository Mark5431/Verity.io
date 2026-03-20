"""Scoring engine that combines rules and an optional model adapter."""

from dataclasses import dataclass
from typing import List

from app.core.config import settings
from app.ml.model_adapter import BaseModelAdapter, ModelAdapterError, StubModelAdapter
from app.services.feature_assembler import FeaturePayload


@dataclass(frozen=True)
class ScoringResult:
    """Unified scoring output consumed by the policy layer."""

    risk_score: float
    rule_flags: List[str]
    engine_type: str
    engine_version: str
    fallback_used: bool


class ScoringEngine:
    """Produce risk scores while preserving a rules-based fallback path."""

    def __init__(self, model_adapter: BaseModelAdapter | None = None) -> None:
        self.model_adapter = model_adapter or StubModelAdapter()

    def ready(self) -> bool:
        """Return whether the configured model adapter reports readiness."""
        return self.model_adapter.is_ready()

    def score(self, features: FeaturePayload) -> ScoringResult:
        """Score a feature payload using rules, a model adapter, or both."""
        rule_flags = self._collect_rule_flags(features)
        rules_score = self._rules_score(features)

        if settings.ENGINE_MODE == "rules_only":
            return ScoringResult(
                risk_score=rules_score,
                rule_flags=rule_flags,
                engine_type="rules",
                engine_version=settings.MODEL_VERSION,
                fallback_used=False,
            )

        try:
            model_score = self.model_adapter.score(features)
        except ModelAdapterError:
            return ScoringResult(
                risk_score=rules_score,
                rule_flags=rule_flags,
                engine_type="rules_fallback",
                engine_version=settings.MODEL_VERSION,
                fallback_used=True,
            )

        return ScoringResult(
            risk_score=max(rules_score, model_score.risk_score),
            rule_flags=rule_flags,
            engine_type=model_score.engine_type,
            engine_version=model_score.engine_version,
            fallback_used=False,
        )

    def _collect_rule_flags(self, features: FeaturePayload) -> List[str]:
        """Collect deterministic reason codes from the normalized feature payload."""
        flags: List[str] = []
        if features.new_recipient_high_value:
            flags.append("NEW_RECIPIENT_HIGH_VALUE")
        if features.is_new_device:
            flags.append("NEW_DEVICE")
        if features.payee_added_this_session:
            flags.append("PAYEE_ADDED_THIS_SESSION")
        if features.high_otp_retry:
            flags.append("HIGH_OTP_RETRY")
        if features.low_account_age:
            flags.append("LOW_ACCOUNT_AGE")
        return flags

    def _rules_score(self, features: FeaturePayload) -> float:
        """Compute a bounded heuristic score used for rules-only and fallback modes."""
        score = 0.05
        if features.new_recipient_high_value:
            score += 0.49
        if features.is_new_device:
            score += 0.16
        if features.payee_added_this_session:
            score += 0.08
        if features.high_otp_retry:
            score += 0.02
        if features.low_account_age:
            score += 0.02
        if features.session_anomaly_count:
            score += min(0.03, 0.01 * features.session_anomaly_count)
        return max(0.0, min(score, 0.99))