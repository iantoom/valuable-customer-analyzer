package com.ian.vca.repositories.cached;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ian.vca.entities.DestinationType;
import com.ian.vca.repositories.DestinationTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CachedDestinationTypeRepository {

	private final DestinationTypeRepository destinationTypeRepository;
	
	public Optional<DestinationType> findById(Integer destinationId) {
		
		
		return destinationTypeRepository.findById(destinationId);
	}
	
}
