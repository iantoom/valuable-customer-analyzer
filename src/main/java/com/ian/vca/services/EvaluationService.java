package com.ian.vca.services;

import java.util.List;

import com.ian.vca.models.request.EvaluationRequestDto;
import com.ian.vca.models.response.EvaluationResponseDto;

import reactor.core.publisher.Mono;

public interface EvaluationService {

	Mono<List<EvaluationResponseDto>> getAllEvaluation();

	Mono<Boolean> save(EvaluationRequestDto dto);

}
