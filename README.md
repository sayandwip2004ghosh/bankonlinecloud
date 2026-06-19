# ========================
# App Config
# ========================
spring.application.name=banking-system
server.port=8080

# ========================
# Database (MySQL)
# ========================
spring.datasource.url=jdbc:mysql://localhost:3306/bankdb
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# ========================
# JWT
# ========================
app.jwt-secret=${JWT_SECRET}
app.jwt-expiration=86400000

# ========================
# Email (Gmail SMTP)
# ========================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=sayandwipghosh007@gmail.com
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ========================
# Import Secret File
# ========================
spring.config.import=optional:application-secret.properties