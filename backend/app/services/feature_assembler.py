"""Feature assembly for converting API requests into stable scoring inputs."""

from dataclasses import dataclass

from app.core.config import settings
from app.models.schemas import RiskDecisionRequest


@dataclass(frozen=True)
class FeaturePayload:
    """Normalized and derived fields consumed by the scoring engine."""

    transaction_id: str
    user_id: str
    amount: float
    currency: str
    tx_type: str
    account_age_days: int
    recent_tx_count: int
    is_new_payee: bool
    previous_transfer_count: int
    is_new_device: bool
    session_anomaly_count: int
    payee_added_this_session: bool
    otp_retry_count: int
    recent_support_contact: bool
    new_recipient_high_value: bool
    low_account_age: bool
    high_otp_retry: bool


class FeatureAssembler:
    """Build derived fraud features from the validated request payload."""

    def assemble(self, request: RiskDecisionRequest) -> FeaturePayload:
        """Normalize request data and compute deterministic rule-support fields."""
        amount = request.transaction.amount
        is_new_payee = request.recipient.is_new_payee or (
            request.recipient.previous_transfer_count == 0
        )

        return FeaturePayload(
            transaction_id=request.transaction_id,
            user_id=request.user.user_id,
            amount=amount,
            currency=request.transaction.currency.upper(),
            tx_type=request.transaction.tx_type,
            account_age_days=request.user.account_age_days,
            recent_tx_count=request.user.recent_tx_count,
            is_new_payee=is_new_payee,
            previous_transfer_count=request.recipient.previous_transfer_count,
            is_new_device=request.device.is_new_device,
            session_anomaly_count=request.device.session_anomaly_count,
            payee_added_this_session=request.journey.payee_added_this_session,
            otp_retry_count=request.journey.otp_retry_count,
            recent_support_contact=request.journey.recent_support_contact,
            new_recipient_high_value=(
                is_new_payee and amount >= settings.HIGH_VALUE_AMOUNT_THRESHOLD
            ),
            low_account_age=request.user.account_age_days <= settings.LOW_ACCOUNT_AGE_DAYS,
            high_otp_retry=(
                request.journey.otp_retry_count >= settings.HIGH_OTP_RETRY_THRESHOLD
            ),
        )