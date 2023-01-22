package com.ian.vca.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ian.vca.entities.DestinationType;
import com.ian.vca.repositories.cached.CachedDestinationTypeRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class DestinationTypeServiceImpl implements DestinationTypeService{

	private final CachedDestinationTypeRepository destinationTypeRepository;
	
	@Override
	public Mono<List<DestinationType>> getAllDestinationType() {
		return destinationTypeRepository.findAllReactive();
	}
	
	@Override
	public Mono<DestinationType> saveDestinationType(DestinationType destinationType) {
		return destinationTypeRepository.save(destinationType);
	}
}
