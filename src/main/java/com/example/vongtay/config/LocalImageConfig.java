package com.example.vongtay.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Optional manual image directory for the paths already supported by the image CRUD.
 * Place real product photos in UPLOADS_DIR; this does not accept file uploads from browsers.
 */
@Configuration
public class LocalImageConfig implements WebMvcConfigurer {
    private final Path path;

    public LocalImageConfig(@Value("${app.uploads.dir:uploads}") String uploadsDir) {
        this.path = Path.of(uploadsDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = path.toUri().toASCIIString();
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
