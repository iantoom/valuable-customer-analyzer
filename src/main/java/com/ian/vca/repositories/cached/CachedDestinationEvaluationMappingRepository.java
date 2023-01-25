package com.ian.vca.repositories.cached;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.ian.vca.configurations.RedisConfiguration.DestinationEvaluationWrapper;
import com.ian.vca.entities.DestinationEvaluationMapping;
import com.ian.vca.repositories.DestinationEvaluationMappingRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CachedDestinationEvaluationMappingRepository {

	private final DestinationEvaluationMappingRepository repository;
	private final ReactiveRedisTemplate<String, DestinationEvaluationWrapper> redisTemplate;
	private String findAllKeyReactive = "findAllEvaluationMapping";
	private String destinationEvaluationPrefix = DestinationEvaluationMapping.class.getName();
	
	public CachedDestinationEvaluationMappingRepository(DestinationEvaluationMappingRepository repository,
			@Qualifier(value = "evaluationRedisTemplate") ReactiveRedisTemplate<String, DestinationEvaluationWrapper> redisTemplate) {
		this.repository = repository;
		this.redisTemplate = redisTemplate;
	}
	
	public Mono<List<DestinationEvaluationMapping>> findAllReactive() {
		
		return redisTemplate.opsForValue().setIfAbsent(findAllKeyReactive, new DestinationEvaluationWrapper())
				.flatMap(keyAbsent -> {

					if (Boolean.FALSE.equals(keyAbsent)) {
						return redisTemplate.opsForValue().get(findAllKeyReactive)
								.map(DestinationEvaluationWrapper::getDestinationEvaluationMappingList);
					} else {
						List<DestinationEvaluationMapping> dbValue = repository.findAll();
						
						DestinationEvaluationWrapper wrapper = new DestinationEvaluationWrapper();
						wrapper.setDestinationEvaluationMappingList(dbValue);
						return redisTemplate.opsForValue().set(findAllKeyReactive, wrapper)
								.map(ok -> dbValue);
					}
				});
	}
	
	public List<DestinationEvaluationMapping> findById_DestinationType_DestinationId(Integer destinationId) {
		String redisKey = destinationEvaluationPrefix + destinationId;
		
		return redisTemplate.opsForValue().setIfAbsent(redisKey, new DestinationEvaluationWrapper())
				.flatMap(keyAbsent -> {

					if (Boolean.FALSE.equals(keyAbsent)) {
						return redisTemplate.opsForValue().get(redisKey)
								.map(DestinationEvaluationWrapper::getDestinationEvaluationMappingList);
					} else {
						List<DestinationEvaluationMapping> dbValue = 
								repository.findById_DestinationType_DestinationId(destinationId);
						
						DestinationEvaluationWrapper wrapper = new DestinationEvaluationWrapper();
						wrapper.setDestinationEvaluationMappingList(dbValue);
						log.info("dbValue: {}", dbValue);
						
						return redisTemplate.opsForValue().set(redisKey, wrapper)
								.map(ok -> dbValue);
					}
				}).block();
	}
	
	@Data
	public static class EvaluationWrapperConsumer implements Consumer<List<DestinationEvaluationMapping>>{

		private List<DestinationEvaluationMapping> dataList = new ArrayList<>();
		
		@Override
		public void accept(List<DestinationEvaluationMapping> dataList) {
			this.dataList = dataList;
		}
		
	}
}
