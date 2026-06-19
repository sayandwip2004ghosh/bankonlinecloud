package com.sayandwip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails {

    @Schema(name = "Recipient Email Address")
    private String recipient;

    @Schema(name = "Email Message Body")
    private String messageBody;

    @Schema(name = "Email Subject")
    private String subject;

    @Schema(name = "Email Attachment Path")
    private String attachment;
}
