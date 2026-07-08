package com.group1.customer_service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name too long")
    private String fullName;
    @NotBlank(message = "Address is required")
    @Size(max = 100, message = "Address too long")
    private String address;
    @NotNull(message = "DateOfBirth is required")
    @Past(message = "The date of birth must be a date in the past.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    @NotBlank(message = "Avatar URL is required")
    @URL(message = "Invalid URL format")
    private String avatarUrl;
    @NotBlank(message = "Gender is required")
    @Size(max = 100, message = "Gender too long")
    private String gender;
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone format")
    private String phone;
}
