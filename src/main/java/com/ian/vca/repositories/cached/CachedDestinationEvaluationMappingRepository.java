package com.ian.vca.repositories.cached;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ian.vca.entities.DestinationEvaluationMapping;
import com.ian.vca.repositories.DestinationEvaluationMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CachedDestinationEvaluationMappingRepository {

	private final DestinationEvaluationMappingRepository repository;
	
	public List<DestinationEvaluationMapping> findById_DestinationType_DestinationId(Integer destinationId) {
		
		return repository.findById_DestinationType_DestinationId(destinationId);
	}
}
