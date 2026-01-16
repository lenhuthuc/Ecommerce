SMTP configuration notes

- Do not commit real credentials to the repo. Use environment-specific files (`application-local.properties`) or environment variables, or a secrets manager.

Gmail (example):
- spring.mail.host=smtp.gmail.com
- spring.mail.port=587
- spring.mail.username=your-email@gmail.com
- spring.mail.password=<app-specific-password>
- spring.mail.properties.mail.smtp.auth=true
- spring.mail.properties.mail.smtp.starttls.enable=true

Mailtrap (recommended for development):
- Use credentials from Mailtrap inbox. Host, port, username and password go into the properties above.

Local testing options:
- Use Mailtrap, GreenMail, or a local SMTP dev server.
- For integration tests, consider an embedded mail server or GreenMail test container.

Running tests:
- Run all tests: mvn test
- Run only password reset controller tests: mvn -Dtest=UserControllerPasswordResetTest test
- Run only the new service tests: mvn -Dtest=UserServiceImplTest,EmailServiceImplTest test

Notes:
- `UserService.resetPassword` stores the OTP in Redis and then calls `EmailService.sendEmail`. If the SMTP server fails, the OTP is still created; `EmailServiceImpl` now logs failures and does not throw to avoid breaking the password reset flow.
