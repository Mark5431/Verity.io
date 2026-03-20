"""FastAPI routes for fraud-decision evaluation and service metadata."""

from fastapi import APIRouter, Depends

from app.core.config import settings
from app.models import schemas
from app.services.decision_service import DecisionService, get_decision_service
from app.utils.metrics import get_metrics

router = APIRouter()


@router.post("/decision", response_model=schemas.RiskDecisionResponse)
async def decision(
    request: schemas.RiskDecisionRequest,
    decision_service: DecisionService = Depends(get_decision_service),
):
    """Evaluate a transfer request and return a product-facing fraud decision."""
    return decision_service.evaluate(request)


@router.post("/risk-evaluate", response_model=schemas.RiskDecisionResponse)
async def risk_evaluate(
    request: schemas.RiskDecisionRequest,
    decision_service: DecisionService = Depends(get_decision_service),
):
    """Provide a compatibility alias for the main decision endpoint."""
    return decision_service.evaluate(request)


@router.get("/metadata", response_model=schemas.MetadataResponse)
async def metadata() -> schemas.MetadataResponse:
    """Expose the active engine mode and the stable request and response contract."""
    return schemas.MetadataResponse(
        name=settings.MODEL_NAME,
        version=settings.MODEL_VERSION,
        engine_mode=settings.ENGINE_MODE,
        decision_endpoint="/api/decision",
        supports_review_queue=settings.REVIEW_QUEUE_ENABLED,
        inputs=[
            "transaction_id",
            "user",
            "recipient",
            "device",
            "transaction",
            "journey",
        ],
        outputs=[
            "risk_score",
            "action",
            "reason_codes",
            "explanation",
            "review_priority",
            "model_version",
        ],
    )


@router.get("/live", response_model=schemas.HealthResponse)
async def live() -> schemas.HealthResponse:
    """Return liveness status for container and process checks."""
    return schemas.HealthResponse(status="alive")


@router.get("/ready", response_model=schemas.HealthResponse)
async def ready(
    decision_service: DecisionService = Depends(get_decision_service),
) -> schemas.HealthResponse:
    """Report whether the decision stack is ready to serve requests."""
    status = "ready" if decision_service.ready() else "degraded"
    return schemas.HealthResponse(status=status)


@router.get("/metrics")
async def metrics():
    """Expose Prometheus-style service metrics for local observability."""
    return get_metrics()


@router.get("/docs-info")
async def docs_info():
    """Return the locations of the generated OpenAPI documentation UIs."""
    return {"swagger_ui": "/docs", "redoc": "/redoc"}
