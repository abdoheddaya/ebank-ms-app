package com.abdoheddaya.ebankservice.services;

import com.abdoheddaya.ebankservice.entities.BankAccount;
import com.abdoheddaya.ebankservice.repository.BankAccountRepository;
import com.abdoheddaya.ebankservice.feign.CustomerRestClient;
import com.abdoheddaya.ebankservice.model.Customer;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class EbankService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerRestClient customerRestClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public EbankService(BankAccountRepository bankAccountRepository,
                        CustomerRestClient customerRestClient,
                        CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerRestClient = customerRestClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public List<BankAccount> getAllBankAccounts() {
        return bankAccountRepository.findAll();
    }

    public BankAccount getBankAccountById(String id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bank account not found"));
        Customer customer = circuitBreakerFactory.create("customerService").run(
                () -> customerRestClient.findById(account.getCustomerId()),
                failure -> null);
        account.setCustomer(customer);
        account.setCustomerStatus(customer == null ? "UNAVAILABLE" : "AVAILABLE");
        return account;
    }

    public BankAccount save(BankAccount bankAccount) {
        if (bankAccount.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        try {
            customerRestClient.findById(bankAccount.getCustomerId());
        } catch (feign.FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer not found", e);
        } catch (feign.FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Customer service unavailable", e);
        }
        bankAccount.setId(UUID.randomUUID().toString());
        bankAccount.setCreatedAt(new Date());
        return bankAccountRepository.save(bankAccount);
    }
}
