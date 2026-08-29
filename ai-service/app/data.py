from __future__ import annotations

from pathlib import Path
import warnings

import pandas as pd
import tensorflow as tf
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

    # FracAtlas stores files in images/Fractured and images/Non_fractured,
    # rather than directly under images/. Build one filename index so the
    # manifest works with both the supplied layout and a flattened copy.
    image_index = {
        image_path.name: image_path
        for image_path in image_dir.rglob("*")
        if image_path.is_file()
    }
    frame["path"] = frame["image_id"].map(
        lambda name: str(image_index.get(Path(str(name)).name, ""))
    )
    frame = frame[frame["path"].map(lambda path: bool(path) and Path(path).is_file())].reset_index(drop=True)
    if frame.empty:
        raise ValueError(f"No image files matching the dataset were found under: {image_dir}")
    frame["label_index"] = frame["label"].map({name: index for index, name in enumerate(CLASS_NAMES)})
    manifest = frame[["image_id", "path", "label", "label_index"]]

    # TensorFlow is stricter than some image viewers.  Validate with the same
    # decoder used by the training pipeline so one malformed JPEG cannot abort
    # an epoch halfway through.
    valid = []
    for path in manifest["path"]:
        try:
            # Materialise the tensor so decoding errors are raised here,
            # rather than later when a tf.data iterator reaches the file.
            tf.io.decode_jpeg(tf.io.read_file(path), channels=3).numpy()
            valid.append(True)
        except (tf.errors.InvalidArgumentError, tf.errors.OpError, OSError):
            valid.append(False)
    invalid_count = len(valid) - sum(valid)
    if invalid_count:
        warnings.warn(
            f"Skipped {invalid_count} unreadable image file(s) from the manifest.",
            RuntimeWarning,
            stacklevel=2,
        )
        manifest = manifest.loc[valid].reset_index(drop=True)
    if manifest.empty:
        raise ValueError(f"No readable image files were found under: {image_dir}")
    return manifest


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


def undersample_majority(
    frame: pd.DataFrame,
    target_minority_fraction: float = 0.10,
    majority_label_index: int = 0,
) -> pd.DataFrame:
    """Reduce only NO_FRACTURE examples so the rarest class reaches the target share.

    The validation and test manifests must remain untouched. A seeded sample keeps
    this operation reproducible while retaining every fracture example.
    """
    if not 0 < target_minority_fraction < 1:
        raise ValueError("target_minority_fraction must be between 0 and 1")
    majority = frame[frame["label_index"] == majority_label_index]
    minority = frame[frame["label_index"] != majority_label_index]
    if majority.empty or minority.empty:
        return frame.reset_index(drop=True)
    rarest_count = int(frame["label_index"].value_counts().min())
    target_majority = int(rarest_count * (1 - target_minority_fraction) / target_minority_fraction)
    if target_majority >= len(majority):
        return frame.reset_index(drop=True)
    sampled_majority = majority.sample(n=max(target_majority, 1), random_state=SEED)
    return pd.concat([sampled_majority, minority], ignore_index=True).sample(
        frac=1, random_state=SEED
    ).reset_index(drop=True)
