from __future__ import annotations

import argparse
import json
from pathlib import Path

import numpy as np
import tensorflow as tf
from sklearn.utils.class_weight import compute_class_weight

from app.config import ARTIFACT_DIR, IMAGE_SIZE, MODEL_VERSION, SEED
from app.data import load_manifest, split_manifest
from app.labels import CLASS_NAMES
from app.model import build_model


def make_dataset(frame, batch_size: int, shuffle: bool) -> tf.data.Dataset:
    paths = frame["path"].to_numpy()
    labels = frame["label_index"].to_numpy(dtype=np.int32)

    def load(path, label):
        image = tf.io.read_file(path)
        image = tf.io.decode_jpeg(image, channels=3)
        image = tf.image.resize(image, IMAGE_SIZE)
        return image, label

    dataset = tf.data.Dataset.from_tensor_slices((paths, labels))
    if shuffle:
        dataset = dataset.shuffle(len(frame), seed=SEED, reshuffle_each_iteration=True)
    return dataset.map(load, num_parallel_calls=tf.data.AUTOTUNE).batch(batch_size).prefetch(tf.data.AUTOTUNE)


def main() -> None:
    parser = argparse.ArgumentParser(description="Train the FracAtlas fracture classifier")
    parser.add_argument("--epochs", type=int, default=20)
    parser.add_argument("--batch-size", type=int, default=32)
    parser.add_argument("--output", type=Path, default=ARTIFACT_DIR)
    args = parser.parse_args()

    tf.keras.utils.set_random_seed(SEED)
    manifest = load_manifest()
    train, validation, test = split_manifest(manifest)
    print(f"Loaded {len(manifest)} images")
    print(f"Train: {len(train)} | validation: {len(validation)} | test: {len(test)}")
    print(manifest["label"].value_counts().reindex(CLASS_NAMES).to_string())

    weights = compute_class_weight(
        class_weight="balanced",
        classes=np.arange(len(CLASS_NAMES)),
        y=train["label_index"].to_numpy(),
    )
    class_weights = {index: float(weight) for index, weight in enumerate(weights)}

    model = build_model()
    args.output.mkdir(parents=True, exist_ok=True)
    callbacks = [
        tf.keras.callbacks.ModelCheckpoint(args.output / "fracture_classifier.keras", monitor="val_accuracy", save_best_only=True),
        tf.keras.callbacks.EarlyStopping(monitor="val_loss", patience=5, restore_best_weights=True),
        tf.keras.callbacks.ReduceLROnPlateau(monitor="val_loss", factor=0.3, patience=2, min_lr=1e-6),
    ]
    history = model.fit(
        make_dataset(train, args.batch_size, shuffle=True),
        validation_data=make_dataset(validation, args.batch_size, shuffle=False),
        epochs=args.epochs,
        class_weight=class_weights,
        callbacks=callbacks,
    )

    best_model = tf.keras.models.load_model(args.output / "fracture_classifier.keras")
    test_loss, test_accuracy = best_model.evaluate(make_dataset(test, args.batch_size, shuffle=False), verbose=0)
    test.to_csv(args.output / "test_manifest.csv", index=False)
    metadata = {
        "modelVersion": MODEL_VERSION,
        "classes": list(CLASS_NAMES),
        "imageSize": list(IMAGE_SIZE),
        "datasetCsv": str(manifest.attrs.get("dataset_csv", "Dataset/FracAtlas/dataset.csv")),
        "trainCount": len(train),
        "validationCount": len(validation),
        "testCount": len(test),
        "classWeights": class_weights,
        "testLoss": float(test_loss),
        "testAccuracy": float(test_accuracy),
        "epochsCompleted": len(history.history["loss"]),
    }
    (args.output / "model_metadata.json").write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    print(json.dumps({"testAccuracy": test_accuracy, "artifacts": str(args.output)}, indent=2))


if __name__ == "__main__":
    main()

