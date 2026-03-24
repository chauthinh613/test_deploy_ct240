package com.ct240.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // lay duong dan project goc
        String baseDir = new File(System.getProperty("user.dir")).getParent();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + baseDir + "/uploads/");
    }
}