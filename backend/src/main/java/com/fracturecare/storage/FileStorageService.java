package com.fracturecare.storage;

import com.fracturecare.common.BadRequestException;
import com.fracturecare.common.NotFoundException;
import com.fracturecare.config.AppProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png"
    );
    private final Path uploadRoot;

    public FileStorageService(AppProperties properties) {
        this.uploadRoot = properties.storage().uploads().toAbsolutePath().normalize();
    }

    @PostConstruct
    void initialize() throws IOException {
        Files.createDirectories(uploadRoot);
    }

    public StoredImage validateAndStore(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Choose a JPEG or PNG X-ray image to upload.");
        }
        String contentType = file.getContentType();
        String extension = ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new BadRequestException("The selected file is not supported. Choose a JPEG or PNG image.");
        }
        verifyDecodableImage(file);
        String reference = UUID.randomUUID() + extension;
        Path target = safeResolve(reference);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store the uploaded image", exception);
        }
        return new StoredImage(reference, safeOriginalName(file.getOriginalFilename()), contentType, target);
    }

    public Resource load(String reference) {
        try {
            Path path = safeResolve(reference);
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("The stored image is no longer available.");
            }
            return resource;
        } catch (IOException exception) {
            throw new NotFoundException("The stored image is no longer available.");
        }
    }

    private void verifyDecodableImage(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            BufferedImage image = ImageIO.read(input);
            if (image == null || image.getWidth() < 128 || image.getHeight() < 128) {
                throw new BadRequestException("The file is not a valid X-ray image or its dimensions are too small.");
            }
            if (image.getWidth() > 8000 || image.getHeight() > 8000) {
                throw new BadRequestException("The image dimensions are too large. Use an image no larger than 8000 by 8000 pixels.");
            }
        } catch (IOException exception) {
            throw new BadRequestException("The image is corrupted or could not be decoded.", exception);
        }
    }

    private Path safeResolve(String reference) {
        Path resolved = uploadRoot.resolve(reference).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            throw new BadRequestException("Invalid stored-file reference.");
        }
        return resolved;
    }

    private String safeOriginalName(String original) {
        if (original == null || original.isBlank()) return "xray-image";
        String name = Path.of(original).getFileName().toString().replaceAll("[\\r\\n]", "");
        return name.length() > 100 ? name.substring(0, 100) : name;
    }
}
