package com.ian.vca.configurations;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EnableKafka
@Configuration
public class KafkaConfiguration {
	
	@Bean
    public Admin adminClient() {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return Admin.create(config);
    }

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {
	    return new KafkaTemplate<>(producerFactory());
	}
	
	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate2() {
	    return new KafkaTemplate<>(producerFactory());
	}
	
	@Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

	@Bean
	public ConsumerFactory<String, UserTransaction> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "VCA");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ian.vca.*");
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, UserTransaction> kafkaListenerContainerFactory() {

		ConcurrentKafkaListenerContainerFactory<String, UserTransaction> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}
    
	@Data
	@NoArgsConstructor
	public static class BaseKafkaObject implements Serializable {
		private static final long serialVersionUID = 1L;
		
	}
	
	@Data
	@NoArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class UserTransaction extends BaseKafkaObject implements Serializable {
		
		private static final long serialVersionUID = 6682027702626345399L;
		private BigInteger accountId;
		private String accountName;
		private Integer destinationId;
		private BigDecimal amount;
		private LocalDateTime transactionDateTime;
	}
	
	@Data
	@NoArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class StateModifier extends BaseKafkaObject implements Serializable {
		
		private static final long serialVersionUID = 6682027702626345399L;
		private BigInteger customerId;
		private LocalDateTime timestamp;
		private BigDecimal amount;
		
	}
}
