package com.glamik.webpconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebpConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebpConverterApplication.class, args);
    }

}