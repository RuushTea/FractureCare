"""Reusable evaluation plots for the FractureCare model-comparison notebooks.

This module keeps the confusion-matrix and grouped vertical bar-chart logic in one
place so the same presentation can be reused after model selection.
"""

from __future__ import annotations

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
from sklearn.metrics import confusion_matrix


METRIC_COLUMNS = [
    "accuracy",
    "average_precision",
    "average_recall",
    "average_f1",
]

METRIC_LABELS = ["Accuracy", "Precision", "Recall", "F1"]


def plot_confusion_matrix(test_actual, test_predicted, class_names) -> None:
    """Display a labelled confusion matrix for the selected model."""
    matrix = confusion_matrix(
        test_actual,
        test_predicted,
        labels=range(len(class_names)),
    )

    plt.figure(figsize=(7, 5))
    sns.heatmap(
        matrix,
        annot=True,
        fmt="d",
        cmap="Blues",
        xticklabels=class_names,
        yticklabels=class_names,
    )
    plt.xlabel("Predicted class")
    plt.ylabel("Actual class")
    plt.title("Confusion Matrix")
    plt.tight_layout()
    plt.show()


def plot_model_metrics(best_frame: pd.DataFrame) -> None:
    """Display grouped vertical bars for each model's best validation metrics."""
    missing = [column for column in ["model", *METRIC_COLUMNS] if column not in best_frame.columns]
    if missing:
        raise ValueError(f"best_frame is missing required columns: {missing}")

    plot_data = best_frame.set_index("model")[METRIC_COLUMNS]

    ax = plot_data.plot(
        kind="bar",
        figsize=(10, 5),
        ylim=(0, 1),
        width=0.75,
    )

    ax.set_title("Performance Metrics by Model")
    ax.set_xlabel("Model")
    ax.set_ylabel("Score (0–1)")
    ax.legend(METRIC_LABELS, loc="lower right")
    ax.grid(axis="y", alpha=0.25)

    for container in ax.containers:
        ax.bar_label(container, fmt="%.3f", padding=2, fontsize=8)

    plt.xticks(rotation=0)
    plt.tight_layout()
    plt.show()
