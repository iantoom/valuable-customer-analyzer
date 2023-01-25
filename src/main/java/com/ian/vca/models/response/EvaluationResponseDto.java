package com.ian.vca.models.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EvaluationResponseDto {
	
	private Integer evaluationId;
	
	private Integer destinationId;
	
	private String destinationName;

	private BigDecimal target;
	
	private String tag;
}
