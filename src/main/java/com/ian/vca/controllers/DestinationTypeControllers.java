package com.ian.vca.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ian.vca.entities.DestinationType;
import com.ian.vca.services.DestinationTypeService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/destination-type")
public class DestinationTypeControllers {

	private final DestinationTypeService destinationTypeService;
	
	@GetMapping("/get-all")
	public Mono<ResponseEntity<List<DestinationType>>> getAllDestinationType() {
		return destinationTypeService
				.getAllDestinationType()
				.map(ResponseEntity::ok);
	}
	
	@GetMapping("/save")
	public Mono<ResponseEntity<DestinationType>> saveDestinationType(@RequestBody DestinationType destinationType) {
		return destinationTypeService
				.saveDestinationType(destinationType)
				.map(ResponseEntity::ok);
	}
}
