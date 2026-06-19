package com.sayandwip.controller;

import com.sayandwip.dto.*;
import com.sayandwip.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/user")
@Tag(name = "User Account Management APIs")
public class UserController {

    @Autowired
    UserService userService;

    @Operation(summary = "Create new user account")
    @ApiResponse(responseCode = "201", description = "Account created")
    @PostMapping
    public BankResponse createAccount(@Valid @RequestBody UserRequest userRequest) {
        // FIX: @Valid triggers field validation (NotBlank, Email, Size, etc.)
        return userService.createAccount(userRequest);
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public BankResponse login(@Valid @RequestBody LoginDto loginDto) {
        return userService.login(loginDto);
    }

    @Operation(summary = "Balance enquiry")
    @PostMapping("balanceEnquiry")
    public BankResponse balanceEnquiry(@Valid @RequestBody EnquiryRequest request) {
        return userService.balanceEnquiry(request);
    }

    @Operation(summary = "Name enquiry")
    @PostMapping("nameEnquiry")
    public String nameEnquiry(@Valid @RequestBody EnquiryRequest request) {
        return userService.nameEnquiry(request);
    }

    @Operation(summary = "Credit account")
    @PostMapping("credit")
    public BankResponse creditAccount(@Valid @RequestBody CreditDebitRequest request) {
        return userService.creditAccount(request);
    }

    @Operation(summary = "Debit account")
    @PostMapping("debit")
    public BankResponse debitAccount(@Valid @RequestBody CreditDebitRequest request) {
        return userService.debitAccount(request);
    }

    @Operation(summary = "Transfer funds")
    @PostMapping("transfer")
    public BankResponse transfer(@Valid @RequestBody TransferRequest request) {
        return userService.transfer(request);
    }
}
