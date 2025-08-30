// src/main/java/com/example/demo/image/ImageController.java
package com.example.demo.image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageNormalizeService service;
    private final Path uploadDir = Paths.get("uploads"); // プロジェクト直下 /uploads

    public ImageController(ImageNormalizeService service) throws IOException {
        this.service = service;
        Files.createDirectories(uploadDir);
    }

    /** cover 方式：中央トリミングで3:5に統一 */
    @PostMapping(value = "/normalize/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> normalizeCover(@RequestPart("file") MultipartFile file) throws IOException {
        return saveNormalized(file, true);
    }

    /** contain 方式：余白を付けて3:5に統一 */
    @PostMapping(value = "/normalize/contain", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> normalizeContain(@RequestPart("file") MultipartFile file) throws IOException {
        return saveNormalized(file, false);
    }

    private ResponseEntity<Map<String, String>> saveNormalized(MultipartFile file, boolean cover) throws IOException {
        String ext = getExt(file.getOriginalFilename());
        if (ext == null) ext = "png"; // デフォルト

        byte[] out;
        try (InputStream in = file.getInputStream()) {
            out = cover
                    ? service.normalizeCover(in, ext)
                    : service.normalizeContain(in, ext);
        }
        String name = UUID.randomUUID() + "." + ext;
        Path path = uploadDir.resolve(name);
        Files.write(path, out);

        // /uploads/** で静的配信する前提
        String url = "/uploads/" + name;
        return ResponseEntity.ok(Map.of(
                "url", url,
                "width", String.valueOf(ImageNormalizeService.TARGET_W),
                "height", String.valueOf(ImageNormalizeService.TARGET_H),
                "mode", cover ? "cover" : "contain"
        ));
    }

    private String getExt(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) return null;
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
