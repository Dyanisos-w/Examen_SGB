package com.example.padelbackend.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.padelbackend")
public class PadelBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PadelBackendApplication.class, args);
    }

}
