from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import cv2
import numpy as np
import tensorflow as tf

from .config import IMAGE_SIZE, METADATA_PATH, MODEL_PATH, MODEL_VERSION
from .labels import CLASS_NAMES

MODEL_NAMES = ("custom_cnn", "mobilenetv2", "efficientnetb0")


def _compile(model: tf.keras.Model, learning_rate: float = 1e-3) -> tf.keras.Model:
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=learning_rate),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"],
    )
    return model


def build_model(
    input_shape: tuple[int, int, int] = (*IMAGE_SIZE, 3),
    learning_rate: float = 1e-3,
) -> tf.keras.Model:
    inputs = tf.keras.Input(shape=input_shape, name="xray")
    x = tf.keras.layers.Rescaling(1.0 / 255.0)(inputs)
    x = tf.keras.layers.RandomRotation(0.035)(x)
    x = tf.keras.layers.RandomZoom(0.08)(x)
    for filters in (32, 64, 128):
        x = tf.keras.layers.Conv2D(filters, 3, padding="same", use_bias=False)(x)
        x = tf.keras.layers.BatchNormalization()(x)
        x = tf.keras.layers.Activation("relu")(x)
        x = tf.keras.layers.MaxPooling2D()(x)
        x = tf.keras.layers.Dropout(0.2)(x)
    x = tf.keras.layers.GlobalAveragePooling2D()(x)
    x = tf.keras.layers.Dense(128, activation="relu")(x)
    x = tf.keras.layers.Dropout(0.35)(x)
    outputs = tf.keras.layers.Dense(len(CLASS_NAMES), activation="softmax", name="class_probabilities")(x)
    model = tf.keras.Model(inputs, outputs, name="fracatlas_fracture_classifier")
    return _compile(model, learning_rate)


def build_transfer_model(
    backbone_name: str,
    input_shape: tuple[int, int, int] = (*IMAGE_SIZE, 3),
    weights: str | None = "imagenet",
    learning_rate: float = 1e-3,
) -> tf.keras.Model:

    if backbone_name not in {"mobilenetv2", "efficientnetb0"}:
        raise ValueError(f"Unsupported transfer-learning backbone: {backbone_name}")

    inputs = tf.keras.Input(shape=input_shape, name="xray")
    x = tf.keras.layers.RandomRotation(0.035)(inputs)
    x = tf.keras.layers.RandomZoom(0.08)(x)

    if backbone_name == "mobilenetv2":
        x = tf.keras.applications.mobilenet_v2.preprocess_input(x)
        backbone = tf.keras.applications.MobileNetV2(
            include_top=False, weights=weights, input_shape=input_shape, pooling="avg"
        )
    else:
        backbone = tf.keras.applications.EfficientNetB0(
            include_top=False, weights=weights, input_shape=input_shape, pooling="avg"
        )

    backbone.trainable = False
    x = backbone(x, training=False)
    x = tf.keras.layers.Dense(128, activation="relu")(x)
    x = tf.keras.layers.Dropout(0.35)(x)
    outputs = tf.keras.layers.Dense(len(CLASS_NAMES), activation="softmax", name="class_probabilities")(x)
    model = tf.keras.Model(inputs, outputs, name=f"fracatlas_{backbone_name}")
    return _compile(model, learning_rate)


def decode_image(raw: bytes) -> np.ndarray:
    array = np.frombuffer(raw, dtype=np.uint8)
    decoded = cv2.imdecode(array, cv2.IMREAD_GRAYSCALE)
    if decoded is None:
        raise ValueError("The uploaded file is not a readable image.")
    resized = cv2.resize(decoded, IMAGE_SIZE, interpolation=cv2.INTER_AREA)
    return cv2.cvtColor(resized, cv2.COLOR_GRAY2RGB)


def load_artifact(model_path: Path = MODEL_PATH) -> tuple[tf.keras.Model | None, dict[str, Any]]:
    if not model_path.exists():
        return None, {}
    model = tf.keras.models.load_model(model_path, compile=False)
    metadata = json.loads(METADATA_PATH.read_text(encoding="utf-8")) if METADATA_PATH.exists() else {}
    return model, metadata


def predict(model: tf.keras.Model, image: np.ndarray) -> tuple[str, float, dict[str, float]]:
    probabilities = model.predict(np.expand_dims(image, axis=0), verbose=0)[0]
    index = int(np.argmax(probabilities))
    values = {name: round(float(probabilities[i]), 6) for i, name in enumerate(CLASS_NAMES)}
    return CLASS_NAMES[index], float(probabilities[index]), values
