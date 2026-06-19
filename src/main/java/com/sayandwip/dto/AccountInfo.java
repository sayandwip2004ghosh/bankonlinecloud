package com.sayandwip.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfo {

    @Schema(name = "User Account Name")
    private String accountName;

    @Schema(name = "User Account Balance")
    private BigDecimal accountBalance;

    @Schema(name = "User Account Number")
    private String accountNumber;

    @Schema(name = "User Role")
    private String role;
}
