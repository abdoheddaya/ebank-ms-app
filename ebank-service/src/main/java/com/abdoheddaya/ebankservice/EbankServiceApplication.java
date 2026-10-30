package com.abdoheddaya.ebankservice;

import com.abdoheddaya.ebankservice.entities.BankAccount;
import com.abdoheddaya.ebankservice.repository.BankAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.UUID;

@SpringBootApplication
@EnableFeignClients
public class EbankServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner demoAccounts(BankAccountRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                for (long customerId = 1; customerId <= 3; customerId++) {
                    repository.save(BankAccount.builder()
                            .id(UUID.randomUUID().toString())
                            .createdAt(new Date())
                            .balance(1000.0 * customerId)
                            .type(customerId == 1 ? "CURRENT" : "SAVINGS")
                            .customerId(customerId)
                            .build());
                }
            }
        };
    }

}
