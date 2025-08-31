package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class ResourceDebugConfig {
  private static final Logger log = LoggerFactory.getLogger(ResourceDebugConfig.class);

  @Bean
  ApplicationRunner listPackagedImages() {
    return args -> {
      var resolver = new PathMatchingResourcePatternResolver();
      Resource[] imgs = resolver.getResources("classpath*:/static/images/*");
      log.info("Packaged images: {}",
          Arrays.stream(imgs).map(r -> {
            try { return r.getURL().toString(); } catch (Exception e) { return r.getFilename(); }
          }).collect(Collectors.toList()));
    };
  }
}
