package com.sayandwip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse {

    @Schema(name = "Response Code")
    private String responseCode;

    @Schema(name = "Response Message")
    private String responseMessage;

    @Schema(name = "Account Information")
    private AccountInfo accountInfo;

    @Schema(name = "JWT Token — returned on login")
    private String token;
}
