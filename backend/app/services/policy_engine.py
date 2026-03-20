"""Policy logic for turning scores and rule flags into business decisions."""

from dataclasses import dataclass
from typing import List

from app.models.schemas import DecisionAction, ReviewPriority
from app.ml.scoring_engine import ScoringResult
from app.services.feature_assembler import FeaturePayload


@dataclass(frozen=True)
class PolicyDecision:
    """Final policy output used to build the API response."""

    action: DecisionAction
    review_priority: ReviewPriority
    reason_codes: List[str]
    explanation: str


class PolicyEngine:
    """Apply product thresholds and explanation templates to scoring results."""

    def decide(self, features: FeaturePayload, scoring_result: ScoringResult) -> PolicyDecision:
        """Map a scoring result into an action, priority, and explanation."""
        reason_codes = scoring_result.rule_flags
        risk_score = scoring_result.risk_score

        if self._should_block(risk_score, reason_codes):
            action = DecisionAction.BLOCK
            review_priority = ReviewPriority.HIGH
        elif self._should_hold(risk_score, reason_codes):
            action = DecisionAction.HOLD
            review_priority = ReviewPriority.HIGH
        elif risk_score >= 0.60:
            action = DecisionAction.REVIEW
            review_priority = ReviewPriority.MEDIUM
        else:
            action = DecisionAction.APPROVE
            review_priority = ReviewPriority.NONE

        explanation = self._build_explanation(action, reason_codes)
        return PolicyDecision(
            action=action,
            review_priority=review_priority,
            reason_codes=reason_codes,
            explanation=explanation,
        )

    def _should_hold(self, risk_score: float, reason_codes: List[str]) -> bool:
        """Return whether the transaction should be paused before completion."""
        hold_combo = {
            "NEW_RECIPIENT_HIGH_VALUE",
            "NEW_DEVICE",
            "PAYEE_ADDED_THIS_SESSION",
        }
        return hold_combo.issubset(set(reason_codes)) or risk_score >= 0.80

    def _should_block(self, risk_score: float, reason_codes: List[str]) -> bool:
        """Return whether the transaction is risky enough to block immediately."""
        return risk_score >= 0.95 and "HIGH_OTP_RETRY" in reason_codes

    def _build_explanation(
        self, action: DecisionAction, reason_codes: List[str]
    ) -> str:
        """Build a deterministic, user-facing explanation from approved templates."""
        if action == DecisionAction.APPROVE:
            return "This payment looks consistent with the current account and device activity."

        core_hold_combo = {
            "NEW_RECIPIENT_HIGH_VALUE",
            "NEW_DEVICE",
            "PAYEE_ADDED_THIS_SESSION",
        }
        if core_hold_combo.issubset(set(reason_codes)):
            return (
                "This payment was paused because it is a first high-value transfer "
                "to a new recipient from a new device."
            )

        fragments = {
            "NEW_RECIPIENT_HIGH_VALUE": "a first high-value transfer to a new recipient",
            "NEW_DEVICE": "a new device",
            "PAYEE_ADDED_THIS_SESSION": "a recipient added in this session",
            "HIGH_OTP_RETRY": "multiple OTP retries",
            "LOW_ACCOUNT_AGE": "a recently created account",
        }
        selected_fragments = [fragments[code] for code in reason_codes if code in fragments]
        if not selected_fragments:
            return "This payment needs additional review based on current risk signals."
        detail = ", ".join(selected_fragments[:3])
        return f"This payment was paused because of {detail}."