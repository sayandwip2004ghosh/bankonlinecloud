package com.sayandwip.exception;

import com.sayandwip.dto.BankResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// FIX: was completely missing — every unhandled exception returned a raw 500
// Now all errors return proper BankResponse JSON with meaningful messages
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Wrong email or password at login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BankResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BankResponse.builder()
                        .responseCode("401")
                        .responseMessage("Invalid email or password")
                        .build());
    }

    // Account locked
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<BankResponse> handleLocked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BankResponse.builder()
                        .responseCode("403")
                        .responseMessage("Account is locked. Please contact support.")
                        .build());
    }

    // Account disabled
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<BankResponse> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BankResponse.builder()
                        .responseCode("403")
                        .responseMessage("Account is disabled. Please contact support.")
                        .build());
    }

    // @Valid validation failures — returns all field errors in one message
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BankResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BankResponse.builder()
                        .responseCode("400")
                        .responseMessage("Validation failed: " + errors)
                        .build());
    }

    // IllegalArgumentException (e.g. bad Role enum value)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BankResponse> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BankResponse.builder()
                        .responseCode("400")
                        .responseMessage("Bad request: " + ex.getMessage())
                        .build());
    }

    // Catch-all — never expose stack traces to the client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BankResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BankResponse.builder()
                        .responseCode("500")
                        .responseMessage("An unexpected error occurred. Please try again later.")
                        .build());
    }
}
