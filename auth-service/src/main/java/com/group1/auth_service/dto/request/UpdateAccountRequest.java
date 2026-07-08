package com.group1.auth_service.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Data
public class UpdateAccountRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name too long")
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone format")
    private String phone;

    @NotBlank(message = "Avatar URL is required")
    @URL(message = "Invalid URL format")
    private String avatarUrl;

    @Size(max = 255, message = "Address too long")
    private String address;

    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;
}
