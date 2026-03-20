"""Lightweight in-memory review queue support for flagged transactions."""

from dataclasses import dataclass
from datetime import datetime, timezone
from threading import Lock
from typing import List

from app.models.schemas import ReviewPriority, RiskDecisionRequest


@dataclass(frozen=True)
class ReviewCase:
    """Minimal review record created for high-priority transaction decisions."""

    case_id: str
    transaction_id: str
    user_id: str
    priority: ReviewPriority
    reason_codes: List[str]
    created_at: datetime


class ReviewService:
    """Create simple review cases without introducing external persistence yet."""

    def __init__(self) -> None:
        self._cases: List[ReviewCase] = []
        self._lock = Lock()

    def create_case(
        self,
        request: RiskDecisionRequest,
        priority: ReviewPriority,
        reason_codes: List[str],
    ) -> ReviewCase:
        """Store an in-memory review case for a flagged decision."""
        with self._lock:
            case = ReviewCase(
                case_id=f"case_{len(self._cases) + 1:04d}",
                transaction_id=request.transaction_id,
                user_id=request.user.user_id,
                priority=priority,
                reason_codes=reason_codes,
                created_at=datetime.now(timezone.utc),
            )
            self._cases.append(case)
            return case