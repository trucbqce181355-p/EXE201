package com.group1.customer_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerProfileResponse {
    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("customer_code")
    private String customerCode;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String email;
    private String phone;
    private String address;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private CustomerProfileSummaryResponse summary;
    private CustomerProfileLoyaltyResponse loyalty;
    private List<CustomerProfileOrderResponse> orders;
    private List<CustomerProfileAddressResponse> addresses;
}
