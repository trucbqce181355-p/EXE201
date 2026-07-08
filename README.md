## Local Mail Setup

For `auth-service`, forgot-password email can be configured per machine without committing secrets.

1. Use `auth-service/application-local.properties.example` as the template.
2. Create `auth-service/application-local.properties` on your own machine.
3. Set:
   - `app.mail.username`
   - `app.mail.password`

`auth-service/application-local.properties` is ignored by git.

If the local file is missing, the backend still supports:
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
