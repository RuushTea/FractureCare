# FractureCare AI service boundary

The prototype deliberately keeps inference behind the backend `InferenceClient` interface. The current `mock` implementation produces deterministic simulated results so the complete product flow can be developed and demonstrated without presenting fabricated output as a medical diagnosis.

## Standalone service

The `ai-service/` folder contains a separate Python service using TensorFlow/Keras 2.21, OpenCV, NumPy, pandas, scikit-learn and FastAPI. On Windows, use a 64-bit Python 3.12 or 3.13 interpreter; Python 3.14 does not currently have a compatible TensorFlow wheel. Spring Boot will be its only browser-facing client; the React application will continue to call the Spring Boot REST API. Run all cells in `ai-service/notebooks/FracAtlas_Model_Comparison.ipynb` to train and save the model, then start the API with `python -m app.main`.

The model-comparison notebook trains three separate candidates: a custom CNN baseline for a transparent low-compute reference, MobileNetV2 transfer learning for efficient inference on a limited dataset, and EfficientNetB0 transfer learning as a stronger accuracy/efficiency candidate. The notebook compares accuracy, macro precision, macro recall and macro F1 on the same untouched test split. Macro F1 is used for model selection because the multiple-fracture class is smaller than the no-fracture class. The selected model is copied to `artifacts/fracture_classifier.keras`, while all three model files and the comparison CSV remain available under `artifacts/models/` and `artifacts/model_comparison.csv`.

### Health check

`GET /health`

```json
{
  "status": "UP",
  "modelVersion": "fracatlas-cnn-1.0.0",
  "modelLoaded": true
}
```

### Prediction

`POST /predict` with `multipart/form-data` and one `image` part containing a validated JPEG or PNG.

```json
{
  "status": "COMPLETED",
  "predictedClass": "ONE_FRACTURE",
  "confidence": 0.87,
  "modelVersion": "fracatlas-cnn-1.0.0",
  "probabilities": {
    "NO_FRACTURE": 0.06,
    "ONE_FRACTURE": 0.87,
    "MULTIPLE_FRACTURES": 0.07
  }
}
```

Allowed `predictedClass` values are `NO_FRACTURE`, `ONE_FRACTURE`, and `MULTIPLE_FRACTURES`. Confidence must be a decimal from 0 to 1. The service must return a non-success response for unreadable or unsupported images and must not expose internal stack traces.

## Integration rules

- Keep preprocessing identical to the model's training pipeline.
- The FracAtlas `fracture_count` label maps to `NO_FRACTURE` (0), `ONE_FRACTURE` (1), or `MULTIPLE_FRACTURES` (2 or more).
- Version the model and return that version with every prediction.
- Apply a strict request timeout and treat unavailable inference as a failed prediction, not a negative diagnosis.
- Do not expose the Python service publicly in production.
- Never describe a prediction as a diagnosis. It is a second-opinion decision-support result that requires clinical review.
