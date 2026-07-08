package com.group1.engagement_service.dto.loyalty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdjustPointsRequest {
	public enum AdjustType {
		ADD, DEDUCT
	}

	@NotNull(message = "type is required")
	private AdjustType type;

	@NotNull(message = "amount is required")
	@Min(value = 1, message = "amount must be positive")
	private Integer amount;

	@NotBlank(message = "reason is required")
	@Size(min = 10, message = "reason must be at least 10 characters")
	private String reason;
}

