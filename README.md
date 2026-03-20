# Verity_io

Unified workspace for the demo system:
- `frontend/` — Android Jetpack Compose app (mock-first UX flow)
- `backend/` — FastAPI fraud-decision service

## Repository Structure

- `frontend/` Android app project (open this folder directly in Android Studio)
- `backend/` Python API service
- `docs/archive/` Archived markdown docs moved from frontend root

## Quick Start (Windows)

### 1) Start Backend

```powershell
cd backend
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

Backend URLs:
- Swagger UI: `http://127.0.0.1:8000/docs`
- OpenAPI: `http://127.0.0.1:8000/openapi.json`

### 2) Run Backend Tests

```powershell
cd backend
.\.venv\Scripts\Activate.ps1
pytest -q
```

### 3) Open Frontend

1. Open Android Studio
2. Select **Open** and choose the `frontend/` folder inside this repo
3. Wait for Gradle sync to complete
4. Run the `app` module on an emulator/device

## Demo Notes

- The frontend is currently mock-first and includes the payment soft-hold narrative flow.
- Local OS notification behavior is implemented for the hold experience (mocked locally, no Firebase required for demo).
- Backend can run independently and is ready for API contract demos.

## Troubleshooting

- If backend install fails, ensure Python and `pip` are available in PATH.
- If Android sync fails, let Android Studio recreate `local.properties` and re-sync.
- If `8000` is occupied, run backend on another port:

```powershell
uvicorn app.main:app --reload --host 127.0.0.1 --port 8001
```
