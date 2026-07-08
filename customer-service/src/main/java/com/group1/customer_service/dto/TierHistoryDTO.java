package com.group1.customer_service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierHistoryDTO {

    private String fromTier;      
    private String toTier;       
    private String reason;        
    private LocalDateTime changedAt;
}