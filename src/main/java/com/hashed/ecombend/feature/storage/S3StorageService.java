package com.hashed.ecombend.feature.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * S3 implementation stub not active in any current profile.
 * To activate: add @Primary here and remove it from LocalStorageService.
 * Implement store() and delete() using the AWS SDK (add to pom.xml when needed).
 *
 * @Profile("s3") means this bean is only loaded when spring.profiles.active=s3.
 */
@Service
@Profile("s3")
public class S3StorageService implements StorageService {

    @Override
    public String store(MultipartFile file, String folder) {
        // TODO: implement with AWS SDK s3Client.putObject(...)
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public void delete(String url) {
        // TODO: implement with AWS SDK s3Client.deleteObject(...)
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }
}
