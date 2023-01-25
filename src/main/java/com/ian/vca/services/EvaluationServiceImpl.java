package com.ian.vca.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ian.vca.entities.DestinationEvaluationMappingId;
import com.ian.vca.entities.DestinationType;
import com.ian.vca.models.response.EvaluationResponseDto;
import com.ian.vca.repositories.cached.CachedDestinationEvaluationMappingRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class EvaluationServiceImpl implements EvaluationService {
	
	private final CachedDestinationEvaluationMappingRepository cachedRepository;
	
	@Override
	public Mono<List<EvaluationResponseDto>> getAllEvaluation() {
		
		return cachedRepository.findAllReactive()
		.flatMapMany(Flux::fromIterable)
		.map(entity -> {
			DestinationEvaluationMappingId id = entity.getId();
			Integer evaluationId = id.getEvaluationId();
			DestinationType destinationType = id.getDestinationType();
			Integer destinationId = destinationType.getDestinationId();
			String destinationName = destinationType.getDestinationName();
			
			BigDecimal target = entity.getTarget();
			String tag = entity.getTag();
			
			EvaluationResponseDto dto = new EvaluationResponseDto();
			dto.setDestinationId(destinationId);
			dto.setDestinationName(destinationName);
			dto.setEvaluationId(evaluationId);
			dto.setTag(tag);
			dto.setTarget(target);
			
			return dto;
		}).collectList();
	}
}
