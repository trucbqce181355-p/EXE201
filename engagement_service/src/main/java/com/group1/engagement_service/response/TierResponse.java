/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.group1.engagement_service.response;
import com.group1.engagement_service.dto.TierBenefitDTO;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierResponse {
    private String name;
    private Integer minPoints;
    private Integer maxPoints;
    private List<TierBenefitDTO> benefits; // Chứa description, type, value
}