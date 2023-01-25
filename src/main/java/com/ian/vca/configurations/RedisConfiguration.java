package com.ian.vca.configurations;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ian.vca.entities.DestinationEvaluationMapping;
import com.ian.vca.entities.DestinationType;

import lombok.Data;

@Configuration
public class RedisConfiguration {
	
	@Bean
	public ReactiveRedisTemplate<String, DestinationTypeWrapper> reactiveRedisTemplate(
	  ReactiveRedisConnectionFactory factory) {
	    StringRedisSerializer keySerializer = new StringRedisSerializer();
	    Jackson2JsonRedisSerializer<DestinationTypeWrapper> valueSerializer =
	      new Jackson2JsonRedisSerializer<>(DestinationTypeWrapper.class);
	    RedisSerializationContext.RedisSerializationContextBuilder<String, DestinationTypeWrapper> builder =
	      RedisSerializationContext.newSerializationContext(keySerializer);
	    RedisSerializationContext<String, DestinationTypeWrapper> context = 
	      builder.value(valueSerializer).build();
	    return new ReactiveRedisTemplate<>(factory, context);
	}
	
	@Bean(name = "evaluationRedisTemplate")
	public ReactiveRedisTemplate<String, DestinationEvaluationWrapper> reactiveEvaluationRedisTemplate(
	  ReactiveRedisConnectionFactory factory) {
	    StringRedisSerializer keySerializer = new StringRedisSerializer();
	    Jackson2JsonRedisSerializer<DestinationEvaluationWrapper> valueSerializer =
	      new Jackson2JsonRedisSerializer<>(DestinationEvaluationWrapper.class);
	    RedisSerializationContext.RedisSerializationContextBuilder<String, DestinationEvaluationWrapper> builder =
	      RedisSerializationContext.newSerializationContext(keySerializer);
	    RedisSerializationContext<String, DestinationEvaluationWrapper> context = 
	      builder.value(valueSerializer).build();
	    return new ReactiveRedisTemplate<>(factory, context);
	}
	
	@Data
	public static class DestinationTypeWrapper {
		
		private List<DestinationType> destinationTypeList;
		private DestinationType destinationType;
	}
	
	@Data
	public static class DestinationEvaluationWrapper {
		
		private List<DestinationEvaluationMapping> destinationEvaluationMappingList;
		private DestinationEvaluationMapping destinationEvaluationMapping;
	}
}
