package com.ian.vca.services;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ian.vca.configurations.RedisConfiguration.DestinationTypeWrapper;
import com.ian.vca.entities.DestinationEvaluationMapping;
import com.ian.vca.entities.DestinationEvaluationMappingId;
import com.ian.vca.entities.DestinationType;
import com.ian.vca.models.request.EvaluationRequestDto;
import com.ian.vca.models.response.EvaluationResponseDto;
import com.ian.vca.repositories.cached.CachedDestinationEvaluationMappingRepository;
import com.ian.vca.repositories.cached.CachedDestinationTypeRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class EvaluationServiceImpl implements EvaluationService {
	
	private final CachedDestinationEvaluationMappingRepository cachedRepository;
	private final CachedDestinationTypeRepository destinationRepository;
	
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
	
	@Override
	public Mono<Boolean> save(EvaluationRequestDto dto) {
		
		Integer destinationId = dto.getDestinationId();
		Integer evaluationId = dto.getEvaluationId();
		String tag = dto.getTag();
		BigDecimal target = dto.getTarget();
		
		Optional<DestinationType> destinationOpt = destinationRepository.findById(destinationId);
		
		if (destinationOpt.isEmpty()) {
			return Mono.just(Boolean.TRUE);
		}
		DestinationType destinationType = destinationOpt.get();
		DestinationEvaluationMappingId id = new DestinationEvaluationMappingId();
		id.setDestinationType(destinationType);
		id.setEvaluationId(evaluationId);
		
		DestinationEvaluationMapping destinationEvaluationMapping = new DestinationEvaluationMapping();
		destinationEvaluationMapping.setId(id);
		destinationEvaluationMapping.setTarget(target);
		destinationEvaluationMapping.setTag(tag);
		
		cachedRepository.save(destinationEvaluationMapping);
		
		return Mono.just(Boolean.TRUE);
	}
}
