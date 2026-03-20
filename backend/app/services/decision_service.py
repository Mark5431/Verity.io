"""Application service that coordinates the full fraud-decision flow."""

import logging
from time import perf_counter

from app.core.config import settings
from app.ml.scoring_engine import ScoringEngine
from app.models.schemas import (
    DecisionAction,
    ReviewPriority,
    RiskDecisionRequest,
    RiskDecisionResponse,
)
from app.services.feature_assembler import FeatureAssembler
from app.services.policy_engine import PolicyEngine
from app.services.review_service import ReviewService
from app.utils.metrics import record_decision_metric

logger = logging.getLogger("uvicorn.error")


class DecisionService:
    """Orchestrate feature assembly, scoring, policy, review, and metrics."""

    def __init__(
        self,
        feature_assembler: FeatureAssembler | None = None,
        scoring_engine: ScoringEngine | None = None,
        policy_engine: PolicyEngine | None = None,
        review_service: ReviewService | None = None,
    ) -> None:
        self.feature_assembler = feature_assembler or FeatureAssembler()
        self.scoring_engine = scoring_engine or ScoringEngine()
        self.policy_engine = policy_engine or PolicyEngine()
        self.review_service = review_service or ReviewService()

    def ready(self) -> bool:
        """Return whether the backing scoring stack is currently available."""
        return self.scoring_engine.ready()

    def evaluate(self, request: RiskDecisionRequest) -> RiskDecisionResponse:
        """Process a validated request into a stable fraud decision response."""
        started_at = perf_counter()
        features = self.feature_assembler.assemble(request)
        scoring_result = self.scoring_engine.score(features)
        policy_decision = self.policy_engine.decide(features, scoring_result)

        if (
            settings.REVIEW_QUEUE_ENABLED
            and policy_decision.review_priority != ReviewPriority.NONE
        ):
            self.review_service.create_case(
                request=request,
                priority=policy_decision.review_priority,
                reason_codes=policy_decision.reason_codes,
            )

        response = RiskDecisionResponse(
            risk_score=scoring_result.risk_score,
            action=policy_decision.action,
            reason_codes=policy_decision.reason_codes,
            explanation=policy_decision.explanation,
            review_priority=policy_decision.review_priority,
            model_version=scoring_result.engine_version,
        )

        latency_ms = (perf_counter() - started_at) * 1000
        record_decision_metric(
            action=response.action.value,
            latency_ms=latency_ms,
            fallback_used=scoring_result.fallback_used,
        )
        logger.info(
            "decision_complete transaction_id=%s action=%s risk_score=%.3f version=%s",
            request.transaction_id,
            response.action.value,
            response.risk_score,
            response.model_version,
        )
        return response


_SINGLETON: DecisionService | None = None


def get_decision_service() -> DecisionService:
    """Return a singleton decision service for dependency injection."""
    global _SINGLETON
    if _SINGLETON is None:
        _SINGLETON = DecisionService()
    return _SINGLETON