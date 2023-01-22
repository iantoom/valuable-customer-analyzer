package com.ian.vca.services;

import java.util.List;

import com.ian.vca.entities.DestinationType;

import reactor.core.publisher.Mono;

public interface DestinationTypeService {

	Mono<List<DestinationType>> getAllDestinationType();

	Mono<DestinationType> saveDestinationType(DestinationType destinationType);

}
