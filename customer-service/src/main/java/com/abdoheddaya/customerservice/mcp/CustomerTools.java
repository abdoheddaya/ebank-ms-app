package com.abdoheddaya.customerservice.mcp;

import com.abdoheddaya.customerservice.entities.Customer;
import com.abdoheddaya.customerservice.service.CustomerService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerTools {
    private final CustomerService customerService;

    public CustomerTools(CustomerService customerService) {
        this.customerService = customerService;
    }

    @McpTool(name = "list_customers", description = "List all bank customers")
    public List<Customer> listCustomers() {
        return customerService.getAllCustomers();
    }

    @McpTool(name = "get_customer", description = "Find a bank customer by numeric ID")
    public Customer getCustomer(@McpToolParam(description = "Customer ID", required = true) Long id) {
        return customerService.findCustomerById(id);
    }
}
