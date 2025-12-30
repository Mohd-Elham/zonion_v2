package com.example.demo.config;


import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("cloud_name", "dh6fr0k2v");
        config.put("api_key", "646988264789224");
        config.put("api_secret", "89lM2WmFQzsDYJuYKFsvR9gDuz4");
        return new Cloudinary(config);
    }
}
