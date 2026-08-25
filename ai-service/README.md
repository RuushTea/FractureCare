# FractureCare AI service

This is the separate Python/TensorFlow service for training and serving the FracAtlas fracture classifier. It is intentionally independent from the Spring Boot backend and React frontend so the model can be developed in PyCharm.

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

The notebooks create a stratified train/validation/test split, calculate class weights for the imbalanced dataset, train three independent Keras classifiers, compare their per-class and macro metrics, and write the selected model plus comparison results to `artifacts/`.

## PyCharm setup

1. Open the `ai-service` folder in PyCharm.
2. Create a Python 3.12 or 3.13 64-bit virtual environment. Python 3.14 is not supported by the available Windows TensorFlow wheels yet.
3. Install dependencies:

   ```powershell
   python -m pip install -r requirements.txt
   ```

4. You can train each model independently in its own notebook:

   - `notebooks/01_Custom_CNN.ipynb` — lightweight, transparent baseline
   - `notebooks/02_MobileNetV2.ipynb` — efficient transfer-learning candidate
   - `notebooks/03_EfficientNetB0.ipynb` — accuracy-oriented transfer-learning candidate

   Each notebook uses the same fixed stratified split and writes its model and macro-metric JSON file under `artifacts/models/`. When you are ready to compare all three on the same split, use `notebooks/FracAtlas_Model_Comparison.ipynb`:

   ```powershell
   jupyter lab
   ```

   Select the `Python (FractureCare AI)` kernel. The comparison notebook trains a custom CNN, MobileNetV2 and EfficientNetB0 separately, then selects the strongest model by macro F1 and copies it to `artifacts/fracture_classifier.keras`. `notebooks/FracAtlas_Train_Model.ipynb` remains available as an older baseline walkthrough.

5. Start the API after training:

   ```powershell
   python -m app.main
   ```

The API listens on `http://127.0.0.1:8090` by default. The Spring Boot integration can be added later using the contract below.

### RTX GPU on Windows

Your RTX 5060 and NVIDIA driver are detected, but the native Windows TensorFlow package is CPU-only for current TensorFlow releases. TensorFlow 2.10 was the last native-Windows CUDA release; current TensorFlow GPU training should run inside WSL2. [TensorFlow documents this limitation](https://www.tensorflow.org/install/pip), and Microsoft recommends CUDA in WSL for NVIDIA GPUs.

Inside an Ubuntu/WSL2 terminal, create a separate environment and install the GPU dependencies:

```bash
cd /mnt/c/Users/Rushd/OneDrive\ -\ wslqd/Documents/Uni\ Documents/ICBT/Development\ Project\ Final\ Year/Final\ Documents/fracturecare-prototype/ai-service
python3 -m venv .venv-gpu
source .venv-gpu/bin/activate
python -m pip install --upgrade pip
python -m pip install -r requirements-wsl-gpu.txt
python -c "import tensorflow as tf; print(tf.config.list_physical_devices('GPU'))"
```

The ready-to-use environment created for this machine is `/home/rushd/fracturecare-ai-venv`. From the project folder, source `gpu-env.sh` before opening Jupyter or starting the API:

```bash
source gpu-env.sh
python -c "import tensorflow as tf; print(tf.config.list_physical_devices('GPU'))"
jupyter lab
```

In PyCharm, select `/home/rushd/fracturecare-ai-venv/bin/python` as the WSL interpreter. Keep the `LD_LIBRARY_PATH` environment variable from `gpu-env.sh` in the run configuration.

Select the WSL interpreter/kernel (`.venv-gpu`) in PyCharm or Jupyter. It should print a GPU device such as `PhysicalDevice(name='/physical_device:GPU:0', ...)`. Do not install the old DirectML plugin for this project: Microsoft marks it discontinued and it only supports Python up to 3.10.

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

