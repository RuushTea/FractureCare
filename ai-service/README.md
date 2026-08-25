# FractureCare AI service

This is the separate Python/TensorFlow service for training and serving the FracAtlas fracture classifier. It is intentionally independent from the Spring Boot backend and React frontend so the model can be developed in PyCharm.

## Dataset

The service expects the supplied dataset at:

```text
../Dataset/FracAtlas/dataset.csv
../Dataset/FracAtlas/images/
```

The CSV is converted into the three classes used by the application:

| FracAtlas `fractured` / `fracture_count` | Service class |
| --- | --- |
| `fractured=0` and `fracture_count=0` | `NO_FRACTURE` |
| `fracture_count=1` | `ONE_FRACTURE` |
| `fracture_count>=2` | `MULTIPLE_FRACTURES` |

The notebooks create a stratified train/validation/test split, calculate class weights for the imbalanced dataset, train three independent Keras classifiers, compare their per-class and macro metrics, and write the selected model plus comparison results to `artifacts/`.

## PyCharm setup

1. Open the `ai-service` folder in PyCharm.
2. Create a Python 3.12 or 3.13 64-bit virtual environment. Python 3.14 is not supported by the available Windows TensorFlow wheels yet.
3. Install dependencies:

   ```powershell
   python -m pip install -r requirements.txt
   ```

4. Open `notebooks/FracAtlas_Model_Comparison.ipynb` and use **Run All** to train and compare the three models:

   ```powershell
   jupyter lab
   ```

   Select the `Python (FractureCare AI)` kernel. The comparison notebook trains a custom CNN, MobileNetV2 and EfficientNetB0 separately, then selects the strongest model by macro F1. `notebooks/FracAtlas_Train_Model.ipynb` remains available when you want to experiment with the baseline model alone.

5. Start the API after training:

   ```powershell
   python -m app.main
   ```

The API listens on `http://127.0.0.1:8090` by default. The Spring Boot integration can be added later using the contract below.

To install the notebook tools into the supported environment:

```powershell
python -m pip install -r requirements-jupyter.txt
jupyter lab
```

### Windows/PyCharm troubleshooting

If pip says `No matching distribution found for tensorflow`, check the interpreter selected by PyCharm:

```powershell
python --version
```

It must report Python 3.12 or 3.13. Install one of those 64-bit Python versions, then in PyCharm select **Settings > Project > Python Interpreter > Add Interpreter > Add Local Interpreter**, choose the new Python executable, and recreate the `ai-service/.venv` environment. For example, from PowerShell after Python 3.12 is installed:

```powershell
cd ai-service
py -3.12 -m venv .venv312
.\.venv312\Scripts\python.exe -m pip install --upgrade pip
.\.venv312\Scripts\python.exe -m pip install -r requirements.txt
```

## API contract

`GET /health` returns whether a trained model is loaded:

```json
{
  "status": "UP",
  "modelVersion": "fracatlas-cnn-1.0.0",
  "modelLoaded": true
}
```

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

The service returns `503 MODEL_NOT_READY` until a trained artifact exists. It returns a `400` response for unsupported, unreadable or oversized images and does not expose internal stack traces.

This model is research/development software. It must not be described as a medical diagnosis or used to make treatment or emergency decisions. A qualified professional must review every result.
