package com.sayandwip.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sayandwip.dto.EmailDetails;
import com.sayandwip.entity.Transaction;
import com.sayandwip.entity.User;
import com.sayandwip.repository.TransactionRepository;
import com.sayandwip.repository.UserRepository;
import com.sayandwip.service.EmailService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class BankStatement {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final String FILE = System.getProperty("user.home") + "/bankStatement.pdf";

    public List<Transaction> generateStatement(String accountNumber, String startDate, String endDate)
            throws FileNotFoundException, DocumentException {

        LocalDate startDateParsed = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate endDateParsed   = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        LocalDateTime start = startDateParsed.atStartOfDay();
        LocalDateTime end   = endDateParsed.atTime(23, 59, 59);

        List<Transaction> transactionList = transactionRepository
                .findByAccountNumberAndCreatedAtBetween(accountNumber, start, end);

        User user = userRepository.findByAccountNumber(accountNumber);

        String customerName = user.getFirstName() + " " + user.getLastName() + " "
                + (user.getOtherName() != null ? user.getOtherName() : "");

        Document document = new Document(PageSize.A4);
        OutputStream outputStream = new FileOutputStream(FILE);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.WHITE);
        Font cellFont  = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldCell  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        // Bank header
        PdfPTable bankHeader = new PdfPTable(1);
        PdfPCell bankName = new PdfPCell(new Phrase("SmartBank", titleFont));
        bankName.setBackgroundColor(BaseColor.BLUE);
        bankName.setPadding(20f);
        bankName.setBorder(0);
        bankName.setHorizontalAlignment(Element.ALIGN_CENTER);
        bankHeader.addCell(bankName);
        document.add(bankHeader);

        // Info table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10f);
        infoTable.addCell(createCell("Start Date: " + startDate, cellFont, Element.ALIGN_LEFT));
        infoTable.addCell(createCell("End Date: " + endDate, cellFont, Element.ALIGN_RIGHT));

        PdfPCell title = new PdfPCell(new Phrase("STATEMENT OF ACCOUNT", boldCell));
        title.setColspan(2);
        title.setBorder(0);
        title.setPadding(10f);
        title.setHorizontalAlignment(Element.ALIGN_CENTER);
        infoTable.addCell(title);
        infoTable.addCell(fullWidthCell("Customer Name: " + customerName, cellFont));
        infoTable.addCell(fullWidthCell("Address: " + user.getAddress(), cellFont));
        document.add(infoTable);

        // Transactions table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.addCell(headerCell("DATE"));
        table.addCell(headerCell("TYPE"));
        table.addCell(headerCell("AMOUNT"));
        table.addCell(headerCell("STATUS"));

        double totalCredit = 0, totalDebit = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Transaction tx : transactionList) {
            table.addCell(createCell(tx.getCreatedAt().format(formatter), cellFont));
            table.addCell(createCell(tx.getTransactionType(), cellFont, Element.ALIGN_CENTER));
            table.addCell(createCell(tx.getAmount().toString(), cellFont, Element.ALIGN_RIGHT));
            table.addCell(createCell(tx.getStatus(), cellFont, Element.ALIGN_CENTER));
            if ("CREDIT".equalsIgnoreCase(tx.getTransactionType())) totalCredit += tx.getAmount().doubleValue();
            else totalDebit += tx.getAmount().doubleValue();
        }
        document.add(table);

        // Totals
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(50);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setSpacingBefore(15f);
        totals.addCell(createCell("Total Credit:", boldCell));
        totals.addCell(createCell(String.format("%.2f", totalCredit), boldCell, Element.ALIGN_RIGHT));
        totals.addCell(createCell("Total Debit:", boldCell));
        totals.addCell(createCell(String.format("%.2f", totalDebit), boldCell, Element.ALIGN_RIGHT));
        document.add(totals);

        // Footer
        PdfPTable footer = new PdfPTable(1);
        footer.setSpacingBefore(30f);
        PdfPCell note = new PdfPCell(new Phrase(
                "System-generated statement by SmartBank. Contact support for queries.", cellFont));
        note.setBorder(0);
        note.setHorizontalAlignment(Element.ALIGN_CENTER);
        note.setPadding(15f);
        footer.addCell(note);
        document.add(footer);

        document.close();

        // Email the statement
        try {
            emailService.sendEmailWithAttachment(EmailDetails.builder()
                    .recipient(user.getEmail())
                    .subject("Bank Statement - SmartBank")
                    .messageBody("Your account statement is attached.\n\nSmartBank")
                    .attachment(FILE)
                    .build());
        } catch (Exception e) {
            log.warn("Could not email bank statement: {}", e.getMessage());
        }

        return transactionList;
    }

    private PdfPCell createCell(String content, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(8f);
        cell.setBorder(0);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private PdfPCell createCell(String content, Font font) {
        return createCell(content, font, Element.ALIGN_LEFT);
    }

    private PdfPCell headerCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.BLUE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10f);
        return cell;
    }

    private PdfPCell fullWidthCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setColspan(2);
        cell.setBorder(0);
        cell.setPadding(8f);
        return cell;
    }
}
