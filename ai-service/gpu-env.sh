#!/usr/bin/env bash
# Source this file inside Ubuntu/WSL2 before launching Jupyter or the API.
export FRACTURECARE_GPU_PYTHON="/home/rushd/fracturecare-ai-venv/bin/python"
export PATH="/home/rushd/fracturecare-ai-venv/bin:${PATH}"
export LD_LIBRARY_PATH="$(find /home/rushd/fracturecare-ai-venv/lib/python3.12/site-packages/nvidia -type d -name lib -printf '%p:')${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}}"

