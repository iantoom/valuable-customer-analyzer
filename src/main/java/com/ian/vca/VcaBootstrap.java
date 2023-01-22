package com.ian.vca;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ian.vca.configurations.KafkaConfiguration.UserTransaction;
import com.ian.vca.entities.DestinationEvaluationMapping;
import com.ian.vca.entities.DestinationEvaluationMappingId;
import com.ian.vca.entities.DestinationType;
import com.ian.vca.repositories.DestinationEvaluationMappingRepository;
import com.ian.vca.repositories.DestinationTypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class VcaBootstrap implements CommandLineRunner {

	private final Admin adminClient;
	private final KafkaTemplate<String, UserTransaction> kafkaTemplate;
	private final DestinationTypeRepository destinationTypeRepository;
	private final DestinationEvaluationMappingRepository destinationEvaluationMappingRepository;
	
	private Random random = new Random();
	private List<String> names = Arrays.asList("Julia", "Wijih", "Komar", "Arum", "Lestari", "Bagus", "Cantika", "Surendra", "Amit", "Dini");
	
	@Override
	public void run(String... args) throws Exception {
		NewTopic newTopic = new NewTopic("userTransaction", 1, (short) 1);
		adminClient.createTopics(Collections.singleton(newTopic));
		
		// create dummy destinations
		String[] destinationNames = {"MarketPlace", "Time Deposit", "Mutual Funds", "Stock Market"};
		String[] evalTags = {"Shopper", "Saver", "Investor", "Trader"};
		
		for (int i = 0; i < 4; i++) {
			DestinationType destinationType = new DestinationType();
			destinationType.setDestinationId(i + 1);
			destinationType.setDestinationName(destinationNames[i]);
			
			destinationTypeRepository.save(destinationType);
			
			for (int j = 0; j < 1; j++) {
				
				DestinationEvaluationMapping evaluationMapping = new DestinationEvaluationMapping();
				DestinationEvaluationMappingId evaluationMappingId = new DestinationEvaluationMappingId();
				evaluationMappingId.setDestinationType(destinationType);
				evaluationMappingId.setEvaluationId(1);
				
				evaluationMapping.setId(evaluationMappingId);
				evaluationMapping.setTag(evalTags[i]);
				evaluationMapping.setTarget(BigDecimal.valueOf(2000000000l));
				if (i > 2) {
					evaluationMappingId.setEvaluationId(2);
					evaluationMapping.setTarget(BigDecimal.valueOf(10));
				};
				
				destinationEvaluationMappingRepository.save(evaluationMapping);
			}
		}
	}
	
	@Scheduled(fixedRate = 500)
	public void produceNewTransaction() {
		
        int randomId = random.nextInt(10) + 1;
        int randomDestinationId = random.nextInt(4) + 1;
        long randomAMount = random.nextLong(1000000000) + 1l;
        
		BigInteger accountId = BigInteger.valueOf(randomId);
        String accountHolderName = names.get(randomId - 1);
        Integer destinationId = randomDestinationId;
        BigDecimal amount = BigDecimal.valueOf(randomAMount);
        
        LocalDateTime transactionDateTime = LocalDateTime.now();
		
		UserTransaction userTransaction = new UserTransaction();
		userTransaction.setAccountId(accountId);
		userTransaction.setAccountName(accountHolderName);
		userTransaction.setDestinationId(destinationId);
		userTransaction.setAmount(amount);
		userTransaction.setTransactionDateTime(transactionDateTime);
		
		kafkaTemplate.send("userTransaction", userTransaction);
	}
	
	
}
	