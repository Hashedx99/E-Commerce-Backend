package com.hashed.ecombend.feature.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    @Override
    public String store(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder",
                    "ecombend/" + folder, "resource_type", "auto", "use_filename", true, "unique_filename", true));
            String url = (String) result.get("secure_url");
            log.info("Uploaded to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String url) {
        if (url == null || url.isBlank()) return;
        try {
            // Extract the public_id from the URL
            // URL format: https://res.cloudinary.com/{cloud}/image/upload/v123/{folder}/{file}.jpg
            String publicId = extractPublicId(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.warn("Could not delete Cloudinary asset {}: {}", url, e.getMessage());
        }
    }

    private String extractPublicId(String url) {
        String afterUpload = url.substring(url.indexOf("/upload/") + 8);
        if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        int dotIndex = afterUpload.lastIndexOf(".");
        return dotIndex > 0 ? afterUpload.substring(0, dotIndex) : afterUpload;
    }
}