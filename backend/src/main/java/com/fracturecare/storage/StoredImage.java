package com.fracturecare.storage;

import java.nio.file.Path;

public record StoredImage(String reference, String originalFileName, String contentType, Path path) {}
