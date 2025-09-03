package com.example.demo.web;

import java.util.concurrent.TimeUnit;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/images")
public class ClasspathImageController {

  @GetMapping("/{filename:.+}")
  public ResponseEntity<Resource> serve(@PathVariable String filename) {
    // JAR 内: src/main/resources/static/images/{filename}
    Resource resource = new ClassPathResource("static/images/" + filename);

    if (!resource.exists()) {
      return ResponseEntity.notFound().build();
    }

    MediaType type = MediaTypeFactory.getMediaType(filename)
        .orElse(MediaType.APPLICATION_OCTET_STREAM);

    return ResponseEntity.ok()
        .contentType(type)
        .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
        .body(resource);
  }
}
