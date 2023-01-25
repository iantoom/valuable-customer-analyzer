package com.ian.vca.repositories.cached;

import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.ian.vca.configurations.RedisConfiguration.DestinationTypeWrapper;
import com.ian.vca.entities.DestinationType;
import com.ian.vca.repositories.DestinationTypeRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CachedDestinationTypeRepository {

	private final DestinationTypeRepository destinationTypeRepository;
	private final ReactiveRedisTemplate<String, DestinationTypeWrapper> redisTemplate;
	private String findAllKeyReactive = "findAllDestinationType";
	
	public Optional<DestinationType> findById(Integer destinationId) {
		return destinationTypeRepository.findById(destinationId);
	}

	public Mono<List<DestinationType>> findAllReactive() {
		
		return redisTemplate.opsForValue().setIfAbsent(findAllKeyReactive, new DestinationTypeWrapper())
				.flatMap(keyAbsent -> {

					if (Boolean.FALSE.equals(keyAbsent)) {
						return redisTemplate.opsForValue().get(findAllKeyReactive)
								.map(DestinationTypeWrapper::getDestinationTypeList);
					} else {
						List<DestinationType> dbValue = destinationTypeRepository.findAll();
						
						DestinationTypeWrapper wrapper = new DestinationTypeWrapper();
						wrapper.setDestinationTypeList(dbValue);
						return redisTemplate.opsForValue().set(findAllKeyReactive, wrapper)
								.map(ok -> dbValue);
					}
				});
	}

	public Mono<DestinationType> save(DestinationType destinationType) {
		
		DestinationTypeWrapper wrapper = new DestinationTypeWrapper();
		wrapper.setDestinationType(destinationType);
		
		return redisTemplate.opsForValue().delete(findAllKeyReactive)
				.map(ok -> destinationType)
				.map(destination -> destinationTypeRepository.save(destinationType));
	}
	
	
}
