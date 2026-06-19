package com.sayandwip.controller;

import com.sayandwip.dto.*;
import com.sayandwip.service.UserService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/chatbot")
public class ChatBotController {

    @Autowired
    private UserService userService;

    @PostMapping("/ask")
    public ResponseEntity<String> handleChat(@Valid @RequestBody ChatRequest chatRequest) {
        String message = chatRequest.getMessage().toLowerCase();
        String accountNumber = chatRequest.getAccountNumber();

        if (message.contains("balance")) {
            BankResponse response = userService.balanceEnquiry(new EnquiryRequest(accountNumber));
            if (response.getAccountInfo() != null) {
                return ResponseEntity.ok("Your balance is ₹" + response.getAccountInfo().getAccountBalance());
            }
            return ResponseEntity.ok("Unable to fetch account details.");

        } else if (message.contains("name")) {
            String name = userService.nameEnquiry(new EnquiryRequest(accountNumber));
            return ResponseEntity.ok("The account holder is: " + name);

        } else if (message.contains("deposit") || message.contains("credit")) {
            CreditDebitRequest request = new CreditDebitRequest();
            request.setAccountNumber(accountNumber);
            request.setAmount(BigDecimal.valueOf(chatRequest.getAmount()));
            BankResponse response = userService.creditAccount(request);
            return ResponseEntity.ok(response.getResponseMessage());

        } else if (message.contains("withdraw") || message.contains("debit")) {
            CreditDebitRequest request = new CreditDebitRequest();
            request.setAccountNumber(accountNumber);
            request.setAmount(BigDecimal.valueOf(chatRequest.getAmount()));
            BankResponse response = userService.debitAccount(request);
            return ResponseEntity.ok(response.getResponseMessage());

        } else if (message.contains("transfer")) {
            TransferRequest request = new TransferRequest();
            request.setSourceAccountNumber(accountNumber);
            request.setDestinationAccountNumber(chatRequest.getToAccount());
            request.setAmount(BigDecimal.valueOf(chatRequest.getAmount()));
            BankResponse response = userService.transfer(request);
            return ResponseEntity.ok(response.getResponseMessage());
        }

        return ResponseEntity.ok("Sorry, I didn't understand that. Try: 'Check balance', 'Transfer 500', etc.");
    }
}
