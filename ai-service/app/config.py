from __future__ import annotations

import os
from pathlib import Path


SERVICE_DIR = Path(__file__).resolve().parents[1]
PROJECT_DIR = SERVICE_DIR.parent


def _path_from_env(name: str, default: Path) -> Path:
    configured = os.getenv(name)
    if not configured:
        return default
    path = Path(configured)
    return path if path.is_absolute() else (SERVICE_DIR / path).resolve()


DATASET_CSV = _path_from_env(
    "AI_DATASET_CSV", PROJECT_DIR / "Dataset" / "FracAtlas" / "dataset.csv"
)
IMAGE_DIR = _path_from_env(
    "AI_IMAGE_DIR", PROJECT_DIR / "Dataset" / "FracAtlas" / "images"
)
ARTIFACT_DIR = _path_from_env("AI_ARTIFACT_DIR", SERVICE_DIR / "artifacts")
MODEL_PATH = ARTIFACT_DIR / "fracture_classifier.keras"
METADATA_PATH = ARTIFACT_DIR / "model_metadata.json"

HOST = os.getenv("AI_HOST", "127.0.0.1")
PORT = int(os.getenv("AI_PORT", "8090"))
MODEL_VERSION = os.getenv("AI_MODEL_VERSION", "fracatlas-cnn-1.0.0")
MAX_UPLOAD_BYTES = int(os.getenv("AI_MAX_UPLOAD_BYTES", str(10 * 1024 * 1024)))
IMAGE_SIZE = (224, 224)
SEED = 42

