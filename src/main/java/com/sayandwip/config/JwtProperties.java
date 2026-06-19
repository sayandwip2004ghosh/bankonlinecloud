package com.sayandwip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

// prefix="app.jwt" means Spring binds:
//   app.jwt.secret     → secret
//   app.jwt.expiration → expiration
// Make sure application.properties uses dots, NOT hyphens after "jwt"
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long expiration;
}
