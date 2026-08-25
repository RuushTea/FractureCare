package com.fracturecare.prediction;

import java.nio.file.Path;

public interface InferenceClient {
    InferenceResult predict(Path imagePath);
}
