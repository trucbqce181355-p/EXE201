package com.group1.customer_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileAddressResponse {
    @JsonProperty("address_id")
    private Long addressId;

    @JsonProperty("address_line")
    private String addressLine;

    @JsonProperty("is_default")
    private Boolean isDefault;
}
