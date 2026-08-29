# FractureCare AI service

This is the separate Python/TensorFlow service for training and serving the FracAtlas fracture classifier. It is independent from the Spring Boot backend and React frontend so the model can be developed in PyCharm.

## Dataset

The service expects the supplied dataset at:

```text
../Dataset/FracAtlas/dataset.csv
../Dataset/FracAtlas/images/
```

The CSV is converted into the three classes used by the application:

The loader searches recursively under `images/`, so the supplied FracAtlas layout (`images/Fractured/` and `images/Non_fractured/`) is supported directly. It also validates files with TensorFlow's JPEG decoder and skips malformed images with a warning instead of failing during an epoch. The `tf.data` pipelines include a second error filter as a safeguard.

| FracAtlas `fractured` / `fracture_count` | Service class |
| --- | --- |
| `fractured=0` and `fracture_count=0` | `NO_FRACTURE` |
| `fracture_count=1` | `ONE_FRACTURE` |
| `fracture_count>=2` | `MULTIPLE_FRACTURES` |

The notebooks create a stratified train/validation/test split, calculate class weights for the imbalanced dataset, train three independent Keras classifiers, compare their per-class and macro metrics, and write the selected model plus comparison results to `artifacts/`. The comparison notebook also exports `model_metric_comparison.png` (accuracy, macro precision, macro recall and macro F1) and `fracture_metric_comparison.png` (fracture-class precision, recall and F1) for the project report.

## Canonical metric results

Use `artifacts/FINAL_MODEL_METRICS.csv` as the single report-ready metric table. It contains the measured validation results for the untuned and hyperparameter-tuned comparisons, plus the selected EfficientNetB0 model's held-out test results. Human-readable `average_*` columns represent the average across the three classes; `fracture_average_*` columns average the two fracture classes.

The source notebooks are:

- `notebooks/FracAtlas_Model_Comparison.ipynb` creates the untuned comparison in `artifacts/model_comparison.csv`.
- `notebooks/04_Hyperparameter_Tuning.ipynb` creates the best tuned result for each architecture in `artifacts/tuned_model_comparison.csv` and writes the selected model's final test metrics to `artifacts/model_metadata.json`.

The other per-model metric JSON files and trial-level CSVs are intermediate training outputs and are not used by the FastAPI runtime.


## API

`GET /health` returns whether a trained model is loaded:

```json
{
  "status": "UP",
  "modelVersion": "fracatlas-cnn-1.0.0",
  "modelLoaded": true
}
```

Train one architecture at a time with the same reproducible data split by opening `notebooks/01_Custom_CNN.ipynb`, `02_MobileNetV2.ipynb`, or `03_EfficientNetB0.ipynb`.

Open `notebooks/04_Hyperparameter_Tuning.ipynb` to run the reproducible learning-rate, batch-size, and majority-undersampling search across all three architectures in one run. It selects the best architecture and saves the final model for the API.

Performance values are written only after a local training/evaluation run; no metrics are fabricated in this repository.

`POST /predict` accepts one `image` multipart field containing a JPEG or PNG:

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
