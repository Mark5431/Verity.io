"""Model adapter interfaces that isolate the scoring backend from the API."""

from dataclasses import dataclass

from app.core.config import settings
from app.services.feature_assembler import FeaturePayload


class ModelAdapterError(RuntimeError):
    """Raised when the model adapter cannot produce a score."""

    pass


@dataclass(frozen=True)
class ModelScore:
    """Normalized score payload returned by a model adapter."""

    risk_score: float
    engine_type: str
    engine_version: str


class BaseModelAdapter:
    """Abstract interface for local or remote model inference implementations."""

    def is_ready(self) -> bool:
        """Return whether the underlying model backend is available."""
        return True

    def score(self, features: FeaturePayload) -> ModelScore:
        """Score a normalized feature payload and return engine metadata."""
        raise NotImplementedError


class StubModelAdapter(BaseModelAdapter):
    """Provide deterministic placeholder scoring until real model serving is ready."""

    def score(self, features: FeaturePayload) -> ModelScore:
        """Return a bounded placeholder score derived from key risk signals."""
        score = 0.05
        if features.new_recipient_high_value:
            score += 0.50
        if features.is_new_device:
            score += 0.17
        if features.payee_added_this_session:
            score += 0.10
        if features.high_otp_retry:
            score += 0.07
        if features.low_account_age:
            score += 0.05
        if features.session_anomaly_count:
            score += min(0.06, 0.02 * features.session_anomaly_count)

        return ModelScore(
            risk_score=max(0.0, min(score, 0.99)),
            engine_type="stub_model",
            engine_version=settings.MODEL_VERSION,
        )