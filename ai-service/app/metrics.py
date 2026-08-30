"""Report metrics and model-selection policy for the FracAtlas task."""

from __future__ import annotations

from typing import Any, Iterable

import numpy as np
from sklearn.metrics import accuracy_score, precision_recall_fscore_support

from .labels import CLASS_NAMES

SELECTION_METRIC = "average_recall"


def calculate_classification_metrics(
    actual: Iterable[int], predicted: Iterable[int]
) -> dict[str, Any]:
    """Return the four report metrics used for model comparison.

    Precision, recall and F1 are averaged equally across the three application
    classes so that the reported values are not dominated by the largest class.
    """
    actual_array = np.asarray(list(actual), dtype=np.int32)
    predicted_array = np.asarray(list(predicted), dtype=np.int32)
    labels = np.arange(len(CLASS_NAMES))

    class_precision, class_recall, class_f1, _ = precision_recall_fscore_support(
        actual_array,
        predicted_array,
        labels=labels,
        average=None,
        zero_division=0,
    )
    return {
        "accuracy": float(accuracy_score(actual_array, predicted_array)),
        "average_precision": float(np.mean(class_precision)),
        "average_recall": float(np.mean(class_recall)),
        "average_f1": float(np.mean(class_f1)),
    }


def select_best_result(results: list[dict[str, Any]]) -> dict[str, Any]:
    """Select the model with the strongest average recall score."""
    if not results:
        raise ValueError("At least one model result is required")
    return max(
        results,
        key=lambda result: (
            float(result[SELECTION_METRIC]),
            float(result["average_f1"]),
            float(result["average_precision"]),
            float(result["accuracy"]),
            str(result["model"]),
        ),
    )
