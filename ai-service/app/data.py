from __future__ import annotations

from pathlib import Path

import pandas as pd
from sklearn.model_selection import train_test_split

from .config import DATASET_CSV, IMAGE_DIR, SEED
from .labels import CLASS_NAMES, to_service_class


def load_manifest(csv_path: Path = DATASET_CSV, image_dir: Path = IMAGE_DIR) -> pd.DataFrame:
    if not csv_path.exists():
        raise FileNotFoundError(f"Dataset CSV not found: {csv_path}")
    frame = pd.read_csv(csv_path)
    required = {"image_id", "fractured", "fracture_count"}
    missing = required.difference(frame.columns)
    if missing:
        raise ValueError(f"Dataset CSV is missing columns: {sorted(missing)}")

    frame = frame.copy()
    frame["label"] = frame.apply(to_service_class, axis=1)
    frame["path"] = frame["image_id"].map(lambda name: str(image_dir / Path(str(name)).name))
    frame = frame[frame["path"].map(lambda path: Path(path).is_file())].reset_index(drop=True)
    if frame.empty:
        raise ValueError(f"No image files matching the dataset were found under: {image_dir}")
    frame["label_index"] = frame["label"].map({name: index for index, name in enumerate(CLASS_NAMES)})
    return frame[["image_id", "path", "label", "label_index"]]


def split_manifest(frame: pd.DataFrame) -> tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
    train, holdout = train_test_split(
        frame,
        test_size=0.2,
        random_state=SEED,
        stratify=frame["label_index"],
    )
    validation, test = train_test_split(
        holdout,
        test_size=0.5,
        random_state=SEED,
        stratify=holdout["label_index"],
    )
    return (
        train.reset_index(drop=True),
        validation.reset_index(drop=True),
        test.reset_index(drop=True),
    )

