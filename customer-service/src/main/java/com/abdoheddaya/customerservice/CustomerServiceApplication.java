package com.abdoheddaya.customerservice;

import com.abdoheddaya.customerservice.entities.Customer;
import com.abdoheddaya.customerservice.service.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner loadCustomers(CustomerService customerService) {
        return args -> {
            customerService.saveCustomer(
                    Customer.builder().name("Amine").email("amine@example.com").build());
            customerService.saveCustomer(
                    Customer.builder().name("Sara").email("sara@example.com").build());
            customerService.saveCustomer(
                    Customer.builder().name("Yassine").email("yassine@example.com").build());
        };
    }
}