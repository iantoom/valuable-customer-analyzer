package com.ian.vca.utils;

public enum SupportedEvaluation {

	MIN_VALUE(1, "Minimum Transaction Value"),
	MIN_COUNT(2, "Minimum Transaction Count"),
	UNKNOWN(0, "UNKNOWN");
	
	int evaluationId;
	String evaluationName;
	
	SupportedEvaluation(int evaluationId, String evaluationName) {
		this.evaluationId = evaluationId;
		this.evaluationName = evaluationName;
	}
	
	public static SupportedEvaluation resolve(int evaluationId) {
		for (SupportedEvaluation eval : values()) {
			if (eval.evaluationId == evaluationId) {
				return eval;
			}
		}
		return UNKNOWN;
	}
}
