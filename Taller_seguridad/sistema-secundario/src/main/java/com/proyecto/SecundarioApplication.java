package com.proyecto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecundarioApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecundarioApplication.class, args);
        System.out.println("=== SISTEMA SECUNDARIO CORRIENDO EN PUERTO 8081 ===");
    }
}
