package com.back.baton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BatonApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatonApplication.class, args);
    }

}
