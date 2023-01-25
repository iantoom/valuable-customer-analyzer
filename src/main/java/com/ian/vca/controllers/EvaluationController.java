package com.ian.vca.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ian.vca.models.response.EvaluationResponseDto;
import com.ian.vca.repositories.cached.CachedDestinationEvaluationMappingRepository;
import com.ian.vca.services.EvaluationService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/evaluation")
@RequiredArgsConstructor
public class EvaluationController {
	
	private final EvaluationService evaluationService;
	
	@GetMapping("/get-all")
	public Mono<ResponseEntity<List<EvaluationResponseDto>>> getAllEvaluation() {
		return evaluationService.getAllEvaluation().map(ResponseEntity::ok);
	}
	
	@GetMapping("/save")
	public Mono<ResponseEntity<List<EvaluationResponseDto>>> get() {
		return evaluationService.getAllEvaluation().map(ResponseEntity::ok);
	}
}
