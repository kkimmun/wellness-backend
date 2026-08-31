package com.kh.wellness.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 요청 → ./uploads/ 폴더에서 찾아서 서빙
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}