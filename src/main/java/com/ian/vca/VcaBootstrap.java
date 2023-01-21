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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class VcaBootstrap implements CommandLineRunner {

	private final Admin adminClient;
	private final KafkaTemplate<String, UserTransaction> kafkaTemplate;
	private Random random = new Random();
	private List<String> names = Arrays.asList("Julia", "Wijih", "Komar");
	private LocalDateTime now = LocalDateTime.now();
	private long secondsOffset = 0l;
	
	@Override
	public void run(String... args) throws Exception {
		NewTopic newTopic = new NewTopic("userTransaction", 1, (short) 1);
		adminClient.createTopics(Collections.singleton(newTopic));
	}
	
	@Scheduled(fixedRate = 500)
	public void produceNewTransaction() {
		
        int randomId = random.nextInt(20) + 1;
        int randomDestinationId = random.nextInt(20) + 1;
        int randomNameIndex = random.nextInt(3);
        long randomAMount = random.nextLong(1000000000) + 1l;
        
		BigInteger accountId = BigInteger.valueOf(randomId);
        String accountHolderName = names.get(randomNameIndex) + randomId;
        BigInteger destinationAccountId = BigInteger.valueOf(randomDestinationId);
        String destinationAccountHolderName = names.get(randomNameIndex) + randomDestinationId;
        BigDecimal amount = BigDecimal.valueOf(randomAMount);
        
        LocalDateTime transactionDateTime = now.minusDays(30).plusSeconds(secondsOffset);
        secondsOffset++;
		
		UserTransaction userTransaction = new UserTransaction();
		userTransaction.setAccountId(accountId);
		userTransaction.setAccountHolderName(accountHolderName);
		userTransaction.setDestinationAccountId(destinationAccountId);
		userTransaction.setDestinationAccountHolderName(destinationAccountHolderName);
		userTransaction.setAmount(amount);
		userTransaction.setTransactionDateTime(transactionDateTime);
		
		kafkaTemplate.send("userTransaction", userTransaction);
	}
	
	
}
	