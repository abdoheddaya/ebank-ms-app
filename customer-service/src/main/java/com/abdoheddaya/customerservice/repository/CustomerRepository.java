package com.abdoheddaya.customerservice.repository;

import com.abdoheddaya.customerservice.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}