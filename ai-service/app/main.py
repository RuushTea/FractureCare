from __future__ import annotations

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware

from .config import HOST, MAX_UPLOAD_BYTES, MODEL_PATH, MODEL_VERSION, PORT
from .model import decode_image, load_artifact, predict

logger = logging.getLogger("fracturecare-ai")
runtime_model = None
runtime_metadata: dict[str, object] = {}


@asynccontextmanager
async def lifespan(_: FastAPI):
    global runtime_model, runtime_metadata
    runtime_model, runtime_metadata = load_artifact()
    if runtime_model is None:
        logger.warning("No trained model found at %s. Run train.py before prediction.", MODEL_PATH)
    yield
    runtime_model = None
    runtime_metadata = {}


def loaded_model_version() -> str:
    return str(runtime_metadata.get("modelVersion", MODEL_VERSION))


app = FastAPI(title="FractureCare AI service", version=MODEL_VERSION, lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://127.0.0.1:8081", "http://localhost:8081"],
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)


@app.get("/health")
def health() -> dict[str, object]:
    return {
        "status": "UP",
        "modelVersion": loaded_model_version(),
        "modelLoaded": runtime_model is not None,
    }


@app.post("/predict")
async def predict_image(image: UploadFile = File(...)) -> dict[str, object]:
    if image.content_type not in {"image/jpeg", "image/png"}:
        raise HTTPException(status_code=400, detail={"code": "UNSUPPORTED_IMAGE", "message": "Upload a JPEG or PNG image."})
    if runtime_model is None:
        raise HTTPException(status_code=503, detail={"code": "MODEL_NOT_READY", "message": "The AI model has not been trained or loaded yet."})
    raw = await image.read(MAX_UPLOAD_BYTES + 1)
    if len(raw) > MAX_UPLOAD_BYTES:
        raise HTTPException(status_code=413, detail={"code": "IMAGE_TOO_LARGE", "message": "The image must be 10 MB or smaller."})
    try:
        decoded = decode_image(raw)
        predicted_class, confidence, probabilities = predict(runtime_model, decoded)
    except ValueError as exception:
        raise HTTPException(status_code=400, detail={"code": "UNREADABLE_IMAGE", "message": str(exception)}) from exception
    except Exception:
        logger.exception("Image inference failed")
        raise HTTPException(status_code=500, detail={"code": "INFERENCE_FAILED", "message": "The image could not be analysed."}) from None
    return {
        "status": "COMPLETED",
        "predictedClass": predicted_class,
        "confidence": round(confidence, 6),
        "modelVersion": loaded_model_version(),
        "probabilities": probabilities,
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host=HOST, port=PORT, reload=False)
