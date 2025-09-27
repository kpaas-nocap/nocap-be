package com.example.nocap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NocapApplication {

    public static void main(String[] args) {
        SpringApplication.run(NocapApplication.class, args);
    }

}
