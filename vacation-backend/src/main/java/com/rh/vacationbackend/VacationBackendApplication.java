package com.rh.vacationbackend;

import com.rh.vacationbackend.config.JwtProperties; // 1. Importe a nova classe
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties; // 2. Importe a anotação

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class) // 3. Ative a sua classe de propriedades aqui
public class VacationBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VacationBackendApplication.class, args);
    }

}