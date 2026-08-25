from __future__ import annotations

from typing import Any

CLASS_NAMES = ("NO_FRACTURE", "ONE_FRACTURE", "MULTIPLE_FRACTURES")


def to_service_class(row: Any) -> str:
    """Convert one FracAtlas CSV row into the application class contract."""
    fracture_count = int(row["fracture_count"] or 0)
    fractured = int(row["fractured"] or 0)
    if fractured == 0 or fracture_count == 0:
        return CLASS_NAMES[0]
    if fracture_count == 1:
        return CLASS_NAMES[1]
    return CLASS_NAMES[2]

