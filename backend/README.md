# Fraud Decision FastAPI Skeleton

This folder contains a FastAPI backend shaped around a fraud-decision API rather than a generic model-serving stub. The public contract is designed to stay stable while the scoring engine evolves from deterministic rules to local ONNX inference or a future remote MLOps service.

Endpoints (all under `/api`):

- `POST /api/decision` - main fraud-decision endpoint
- `POST /api/risk-evaluate` - alias for the main decision endpoint
- `GET /api/metadata` - engine metadata and contract summary
- `GET /api/live` - liveness probe
- `GET /api/ready` - readiness probe
- `GET /api/metrics` - Prometheus-style decision metrics
- `GET /api/docs-info` - pointer to interactive docs

Internal design:

- typed request and response schemas
- decision orchestration service
- feature assembly layer
- scoring engine with swap-ready model adapter boundary
- policy and explanation layer
- lightweight review-case simulation and decision metrics

Run locally:

```bash
python -m pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Interactive docs are available at `/docs` (Swagger UI) and `/redoc` (ReDoc).
