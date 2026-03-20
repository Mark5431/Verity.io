"""Application settings for the fraud decision backend."""

from pydantic import BaseSettings


class Settings(BaseSettings):
    """Runtime configuration used by the API, rules, and fallback scoring flow."""

    APP_NAME: str = "fraud-shield-api"
    APP_VERSION: str = "0.1.0"
    MODEL_NAME: str = "fraud-decision-engine"
    MODEL_VERSION: str = "demo_rules_v1"
    ENGINE_MODE: str = "rules_only"
    REVIEW_QUEUE_ENABLED: bool = True
    HIGH_VALUE_AMOUNT_THRESHOLD: float = 500.0
    LOW_ACCOUNT_AGE_DAYS: int = 14
    HIGH_OTP_RETRY_THRESHOLD: int = 2


settings = Settings()
