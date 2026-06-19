package com.sayandwip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Message must not be blank")
    private String message;

    private String accountNumber;
    private String toAccount;
    private Double amount;
}
