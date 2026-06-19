package com.sayandwip.service.impl;

import com.sayandwip.dto.TransactionDto;
import com.sayandwip.entity.Transaction;
import com.sayandwip.repository.TransactionRepository;
import com.sayandwip.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public void saveTransaction(TransactionDto transactionDto) {
        Transaction transaction = Transaction.builder()
                .transactionType(transactionDto.getTransactionType())
                .accountNumber(transactionDto.getAccountNumber())
                .amount(transactionDto.getAmount())
                .status("SUCCESS")
                .build();
        transactionRepository.save(transaction);
        log.info("Transaction saved for account: {}", transactionDto.getAccountNumber());
    }
}
