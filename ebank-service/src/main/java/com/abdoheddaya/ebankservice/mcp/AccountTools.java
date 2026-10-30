package com.abdoheddaya.ebankservice.mcp;

import com.abdoheddaya.ebankservice.entities.BankAccount;
import com.abdoheddaya.ebankservice.services.EbankService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountTools {
    private final EbankService ebankService;

    public AccountTools(EbankService ebankService) {
        this.ebankService = ebankService;
    }

    @McpTool(name = "list_accounts", description = "List all bank accounts")
    public List<BankAccount> listAccounts() {
        return ebankService.getAllBankAccounts();
    }

    @McpTool(name = "get_account", description = "Find a bank account by ID, including customer information when available")
    public BankAccount getAccount(@McpToolParam(description = "Bank account ID", required = true) String id) {
        return ebankService.getBankAccountById(id);
    }
}
