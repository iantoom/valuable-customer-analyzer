package com.ian.vca.models.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EvaluationRequestDto {

	private Integer evaluationId;
	private Integer destinationId;
	private BigDecimal target;
	private String tag;
}
