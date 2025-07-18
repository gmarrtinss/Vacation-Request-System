package com.rh.vacationbackend; // pacote da sua classe principal (ajuste se diferente)

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EntityScan("com.rh.vacationbackend.model") // pacote das suas entidades
@EnableJpaRepositories("com.rh.vacationbackend.repository") // pacote dos seus repositórios
public class VacationBackendApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        SpringApplication.run(VacationBackendApplication.class, args);
    }

}
