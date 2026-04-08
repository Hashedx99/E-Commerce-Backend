package com.hashed.ecombend.feature.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for file storage operations.
 * Swap implementations by moving @Primary:
 * - LocalStorageService   → saves to disk
 * - CloudinaryStorageService → add later using existing Cloudinary bean
 * - S3StorageService -> mayhaps
 */
public interface StorageService {

    /**
     * Stores a file and returns its accessible URL.
     *
     * @param file   The multipart file (validated inside each implementation)
     * @param folder sub folder
     * @return The public URL of the stored file
     */
    String store(MultipartFile file, String folder);

    /**
     * Deletes a file by its URL. Fails silently if the file does not exist
     *
     * @param url A URL previously returned by store()
     */
    void delete(String url);
}
