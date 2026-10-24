package com.example.vongtay.catalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageUploadService {
    private final Path uploadDir;
    private final ProductImageService images;

    public ProductImageUploadService(@Value("${app.uploads.dir:uploads}") String dir,
                                     ProductImageService images) {
        this.uploadDir = Path.of(dir).toAbsolutePath().normalize();
        this.images = images;
    }

    public void upload(long productId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Hãy chọn một ảnh JPG hoặc PNG.");
        if (file.getSize() > 5L * 1024 * 1024)
            throw new IllegalArgumentException("Ảnh không được vượt quá 5 MB.");
        byte[] content = file.getBytes();
        boolean jpeg = content.length >= 3 && (content[0] & 255) == 0xff
                && (content[1] & 255) == 0xd8 && (content[2] & 255) == 0xff;
        boolean png = content.length >= 8 && (content[0] & 255) == 0x89
                && content[1] == 'P' && content[2] == 'N' && content[3] == 'G'
                && content[4] == 13 && content[5] == 10 && (content[6] & 255) == 26 && content[7] == 10;
        if (!jpeg && !png)
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPG hoặc PNG thật.");
        // Confirm that ImageIO can decode the file, not only its header.
        try (var in = new java.io.ByteArrayInputStream(content)) {
            var decoded = ImageIO.read(in);
            if (decoded == null || decoded.getWidth() > 6000 || decoded.getHeight() > 6000)
                throw new IllegalArgumentException("Ảnh không hợp lệ hoặc kích thước quá lớn (tối đa 6000px).");
        }
        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + (jpeg ? ".jpg" : ".png");
        Path target = uploadDir.resolve(filename);
        Files.write(target, content, StandardOpenOption.CREATE_NEW);
        try {
            images.add(productId, "/uploads/" + filename);
        } catch (RuntimeException ex) {
            Files.deleteIfExists(target);
            throw ex;
        }
    }
}
