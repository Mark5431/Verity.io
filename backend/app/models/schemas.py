"""Pydantic request and response models for the fraud-decision API."""

from datetime import datetime
from enum import Enum
from typing import List

from pydantic import BaseModel, Field


class DecisionAction(str, Enum):
    """Business actions that the client can apply to a transaction."""

    APPROVE = "approve"
    HOLD = "hold"
    REVIEW = "review"
    BLOCK = "block"


class ReviewPriority(str, Enum):
    """Priority levels for operational follow-up on flagged transactions."""

    NONE = "none"
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"


class UserContext(BaseModel):
    """User-level attributes needed for thin-file and history-aware decisions."""

    user_id: str
    account_age_days: int = Field(..., ge=0)
    recent_tx_count: int = Field(..., ge=0)


class RecipientContext(BaseModel):
    """Recipient trust-state fields used to detect first-transfer risk."""

    payee_id: str
    payee_name: str
    is_new_payee: bool
    previous_transfer_count: int = Field(..., ge=0)


class DeviceContext(BaseModel):
    """Device and session signals associated with the current transfer."""

    device_id: str
    is_new_device: bool
    session_anomaly_count: int = Field(0, ge=0)


class TransactionContext(BaseModel):
    """Core transaction fields required for fraud evaluation."""

    tx_type: str
    amount: float = Field(..., gt=0)
    currency: str = Field(..., min_length=3, max_length=3)
    timestamp: datetime


class JourneyContext(BaseModel):
    """Journey-level risk indicators captured during the payment flow."""

    payee_added_this_session: bool
    otp_retry_count: int = Field(0, ge=0)
    recent_support_contact: bool = False


class RiskDecisionRequest(BaseModel):
    """Top-level request body for fraud decision evaluation."""

    transaction_id: str
    user: UserContext
    recipient: RecipientContext
    device: DeviceContext
    transaction: TransactionContext
    journey: JourneyContext


class RiskDecisionResponse(BaseModel):
    """Product-facing fraud decision returned to the client application."""

    risk_score: float = Field(..., ge=0.0, le=1.0)
    action: DecisionAction
    reason_codes: List[str]
    explanation: str
    review_priority: ReviewPriority
    model_version: str


class MetadataResponse(BaseModel):
    """Service metadata describing the active engine and supported contract."""

    name: str
    version: str
    engine_mode: str
    decision_endpoint: str
    supports_review_queue: bool
    inputs: List[str]
    outputs: List[str]


class HealthResponse(BaseModel):
    """Simple health probe response model."""

    status: str
