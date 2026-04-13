package com.hashed.ecombend.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StorageResourceConfiguration implements WebMvcConfigurer {

    @Value("${storage.base-path:./uploads}")
    private String storageBasePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(storageBasePath).toAbsolutePath().normalize();
        String uploadLocation = uploadPath.toUri().toString();

        registry.addResourceHandler("/files/**")
                .addResourceLocations(uploadLocation);
    }
}

