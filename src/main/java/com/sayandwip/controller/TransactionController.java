package com.sayandwip.controller;

import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.DocumentException;
import com.sayandwip.entity.Transaction;
import com.sayandwip.service.impl.BankStatement;
import lombok.AllArgsConstructor;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bankStatement")
@AllArgsConstructor
public class TransactionController {

    private BankStatement bankStatement;

    @GetMapping
    public List<Transaction> generateBankStatement(
            @RequestParam String accountNumber,
            @RequestParam String startDate,
            @RequestParam String endDate)
            throws FileNotFoundException, DocumentException {
        return bankStatement.generateStatement(accountNumber, startDate, endDate);
    }
}
