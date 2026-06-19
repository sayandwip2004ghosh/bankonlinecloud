package com.sayandwip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@SpringBootApplication
@EnableAsync   // FIX: needed so @Async on email methods actually works
@OpenAPIDefinition(
    info = @Info(
        title = "SmartBank - Banking System App",
        description = "Backend REST APIs for SmartBank",
        version = "v1.0",
        contact = @Contact(
            name = "Sayandwip",
            email = "sayandwipghosh007@gmail.com"
        ),
        license = @License(name = "SmartBank License")
    ),
    externalDocs = @ExternalDocumentation(description = "SmartBank Documentation")
)
public class BankingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankingSystemApplication.class, args);
    }
}
