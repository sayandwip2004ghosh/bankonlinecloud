package com.sayandwip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryRequest {

    @NotBlank(message = "Account number is required")
    @Schema(name = "User Account Number")
    private String accountNumber;
}
