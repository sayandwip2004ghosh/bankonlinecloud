package com.sayandwip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "First name is required")
    @Schema(name = "First Name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(name = "Last Name")
    private String lastName;

    @Schema(name = "Other Name")
    private String otherName;

    @Schema(name = "Gender")
    private String gender;

    @Schema(name = "Address")
    private String address;

    @Schema(name = "State of Origin")
    private String stateOfOrigin;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Schema(name = "Email Address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(name = "Password")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Schema(name = "Primary Phone Number")
    private String phoneNumber;

    @Schema(name = "Alternative Phone Number")
    private String alternativePhoneNumber;

    // FIX: role field REMOVED — any user could send "ROLE_ADMIN" and become admin.
    // All new accounts are always ROLE_USER. Admins are created via separate SQL/admin panel.
}
