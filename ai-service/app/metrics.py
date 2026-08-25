"""Metrics and model-selection policy for the imbalanced FracAtlas task."""

from __future__ import annotations

from typing import Any, Iterable

import numpy as np
from sklearn.metrics import (
    accuracy_score,
    balanced_accuracy_score,
    precision_recall_fscore_support,
)

from .labels import CLASS_NAMES

# The two fracture categories are the safety-relevant minority classes.
FRACTURE_CLASS_INDICES = (1, 2)
SELECTION_METRIC = "fracture_macro_recall"


def calculate_classification_metrics(
    actual: Iterable[int], predicted: Iterable[int]
) -> dict[str, Any]:
    """Return overall and fracture-focused metrics for one model."""
    actual_array = np.asarray(list(actual), dtype=np.int32)
    predicted_array = np.asarray(list(predicted), dtype=np.int32)
    labels = np.arange(len(CLASS_NAMES))

    macro_precision, macro_recall, macro_f1, _ = precision_recall_fscore_support(
        actual_array,
        predicted_array,
        labels=labels,
        average="macro",
        zero_division=0,
    )
    fracture_precision, fracture_recall, fracture_f1, _ = precision_recall_fscore_support(
        actual_array,
        predicted_array,
        labels=FRACTURE_CLASS_INDICES,
        average="macro",
        zero_division=0,
    )

    return {
        "accuracy": float(accuracy_score(actual_array, predicted_array)),
        "balanced_accuracy": float(
            balanced_accuracy_score(actual_array, predicted_array)
        ),
        "macro_precision": float(macro_precision),
        "macro_recall": float(macro_recall),
        "macro_f1": float(macro_f1),
        "fracture_macro_precision": float(fracture_precision),
        "fracture_macro_recall": float(fracture_recall),
        "fracture_macro_f1": float(fracture_f1),
    }


def select_best_result(results: list[dict[str, Any]]) -> dict[str, Any]:
    """Select for fracture recall first, then use macro F1 as a tie-breaker."""
    if not results:
        raise ValueError("At least one model result is required")
    return max(
        results,
        key=lambda result: (
            float(result[SELECTION_METRIC]),
            float(result["macro_f1"]),
            float(result["balanced_accuracy"]),
            str(result["model"]),
        ),
    )

