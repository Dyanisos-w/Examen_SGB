package com.example.padelbackend;

import org.springframework.boot.SpringApplication;

public class TestPadelBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(PadelBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
