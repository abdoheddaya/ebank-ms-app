package com.abdoheddaya.ebankbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EbankBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(EbankBotApplication.class, args);
    }
}
