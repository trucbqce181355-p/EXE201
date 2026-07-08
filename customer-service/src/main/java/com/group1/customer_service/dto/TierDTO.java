package com.group1.customer_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierDTO {

    private String code; 

    private String name; 

    @JsonProperty("icon_url")
    private String iconUrl; 

    private TierBenefitsDTO benefits;
}