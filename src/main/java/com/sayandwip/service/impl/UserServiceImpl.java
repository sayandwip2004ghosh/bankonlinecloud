package com.sayandwip.service.impl;

import com.sayandwip.config.JwtTokenProvider;
import com.sayandwip.dto.*;
import com.sayandwip.entity.Role;
import com.sayandwip.entity.User;
import com.sayandwip.repository.UserRepository;
import com.sayandwip.service.EmailService;
import com.sayandwip.service.TransactionService;
import com.sayandwip.service.UserService;
import com.sayandwip.utils.AccountUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private TransactionService transactionService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    // ── Create Account ────────────────────────────────────────────────────────
    @Override
    public BankResponse createAccount(UserRequest userRequest) {

        // Check duplicate email
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .build();
        }

        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                // FIX: pass duplicate checker — retries until account number is unique
                .accountNumber(AccountUtils.generateAccountNumber(
                        userRepository::existsByAccountNumber))
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                // FIX: role field removed from UserRequest — always ROLE_USER here.
                // To create an ADMIN use create_admin.sql or a protected admin endpoint.
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(newUser);

        // Send welcome email — failure does NOT fail account creation
        try {
            emailService.sendEmailAlert(EmailDetails.builder()
                    .recipient(savedUser.getEmail())
                    .subject("Account Created — SmartBank")
                    .messageBody("Welcome to SmartBank!\n\n" +
                            "Name: " + savedUser.getFirstName() + " " + savedUser.getLastName() + "\n" +
                            "Account Number: " + savedUser.getAccountNumber() + "\n\n" +
                            "Keep your account number safe.")
                    .build());
        } catch (Exception ignored) { }

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(savedUser.getFirstName() + " " + savedUser.getLastName())
                        .role(savedUser.getRole().name())
                        .build())
                .build();
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    // FIX: BadCredentialsException is now caught by GlobalExceptionHandler → proper 401
    @Override
    public BankResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        try {
            emailService.sendEmailAlert(EmailDetails.builder()
                    .recipient(user.getEmail())
                    .subject("Login Alert — SmartBank")
                    .messageBody("New login detected on your account.\n" +
                            "Name: " + user.getFirstName() + "\n" +
                            "Account: " + user.getAccountNumber() + "\n\n" +
                            "If this was not you, contact support immediately.")
                    .build());
        } catch (Exception ignored) { }

        return BankResponse.builder()
                .responseCode("LOGIN_SUCCESS")
                .responseMessage("Login successful! Welcome " + user.getFirstName())
                .token(token)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(user.getAccountNumber())
                        .accountName(user.getFirstName() + " " + user.getLastName())
                        .accountBalance(user.getAccountBalance())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    // ── Balance Enquiry ───────────────────────────────────────────────────────
    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        if (user == null) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .build();
        }
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_SUCCESS)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(user.getAccountNumber())
                        .accountName(user.getFirstName() + " " + user.getLastName())
                        .accountBalance(user.getAccountBalance())
                        .build())
                .build();
    }

    // ── Name Enquiry ──────────────────────────────────────────────────────────
    @Override
    public String nameEnquiry(EnquiryRequest request) {
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        if (user == null) return AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE;
        return user.getFirstName() + " " + user.getLastName();
    }

    // ── Credit Account ────────────────────────────────────────────────────────
    @Override
    @Transactional  // FIX: added — DB changes are atomic
    public BankResponse creditAccount(CreditDebitRequest request) {
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        if (user == null) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .build();
        }
        user.setAccountBalance(user.getAccountBalance().add(request.getAmount()));
        userRepository.save(user);
        transactionService.saveTransaction(TransactionDto.builder()
                .accountNumber(user.getAccountNumber())
                .transactionType("CREDIT")
                .amount(request.getAmount())
                .build());
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(user.getAccountNumber())
                        .accountName(user.getFirstName() + " " + user.getLastName())
                        .accountBalance(user.getAccountBalance())
                        .build())
                .build();
    }

    // ── Debit Account ─────────────────────────────────────────────────────────
    @Override
    @Transactional  // FIX: added — DB changes are atomic
    public BankResponse debitAccount(CreditDebitRequest request) {
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        if (user == null) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .build();
        }
        // FIX: was toBigInteger() which truncates decimals — use BigDecimal.compareTo() instead
        if (user.getAccountBalance().compareTo(request.getAmount()) < 0) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .build();
        }
        user.setAccountBalance(user.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(user);
        transactionService.saveTransaction(TransactionDto.builder()
                .accountNumber(user.getAccountNumber())
                .transactionType("DEBIT")
                .amount(request.getAmount())
                .build());
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_DEBITED_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(user.getAccountNumber())
                        .accountName(user.getFirstName() + " " + user.getLastName())
                        .accountBalance(user.getAccountBalance())
                        .build())
                .build();
    }

    // ── Transfer ──────────────────────────────────────────────────────────────
    @Override
    @Transactional  // FIX: critical — if second save fails, first is rolled back
    public BankResponse transfer(TransferRequest request) {
        // FIX: prevent transferring to self
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            return BankResponse.builder()
                    .responseCode("400")
                    .responseMessage("Source and destination account cannot be the same")
                    .build();
        }

        User source = userRepository.findByAccountNumber(request.getSourceAccountNumber());
        User destination = userRepository.findByAccountNumber(request.getDestinationAccountNumber());

        if (source == null || destination == null) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .build();
        }
        if (source.getAccountBalance().compareTo(request.getAmount()) < 0) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .build();
        }

        source.setAccountBalance(source.getAccountBalance().subtract(request.getAmount()));
        destination.setAccountBalance(destination.getAccountBalance().add(request.getAmount()));
        userRepository.save(source);
        userRepository.save(destination);

        transactionService.saveTransaction(TransactionDto.builder()
                .accountNumber(source.getAccountNumber())
                .transactionType("DEBIT")
                .amount(request.getAmount())
                .build());
        transactionService.saveTransaction(TransactionDto.builder()
                .accountNumber(destination.getAccountNumber())
                .transactionType("CREDIT")
                .amount(request.getAmount())
                .build());

        return BankResponse.builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESSFUL_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE)
                .build();
    }
}
