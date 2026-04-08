package com.hashed.ecombend.feature.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Local filesystem implementation of StorageService.
 * <p>
 * Files are stored at: ${storage.base-path}/{folder}/{uuid}.{ext}
 * URLs returned:       ${storage.base-url}/{folder}/{uuid}.{ext}
 * <p>
 */
@Service
@Primary
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5 MB

    @Value("${storage.base-path:/tmp/ecombend/uploads}")
    private String basePath;

    @Value("${storage.base-url:http://localhost:8080/files}")
    private String baseUrl;

    @Override
    public String store(MultipartFile file, String folder) {
        validateFile(file);

        String ext = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(basePath, folder);
        Path dest = dir.resolve(fileName);

        try {
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
            }
            Files.write(dest, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }

        return baseUrl + "/" + folder + "/" + fileName;
    }

    /**
     * Silently ignores missing files
     */
    @Override
    public void delete(String url) {
        if (url == null || url.isBlank()) return;
        String relative = url.replace(baseUrl + "/", "");
        Path path = Paths.get(basePath, relative);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log but do not throw — missing file is not an error
            System.err.println("Warning: could not delete file " + path + ": " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File must not be empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("File type not allowed. Accepted: jpg, png, webp");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new RuntimeException("File exceeds maximum allowed size of 5 MB");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
