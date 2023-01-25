package com.ian.vca.processors;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ian.vca.configurations.KafkaConfiguration.StateModifier;
import com.ian.vca.configurations.KafkaConfiguration.UserTransaction;
import com.ian.vca.entities.DestinationEvaluationMapping;
import com.ian.vca.entities.DestinationType;
import com.ian.vca.entities.UserValueTagState;
import com.ian.vca.entities.UserValueTagStateId;
import com.ian.vca.entities.UserValueTags;
import com.ian.vca.models.TransactionProcessingError;
import com.ian.vca.repositories.UserValueTagStateRepository;
import com.ian.vca.repositories.UserValueTagsRepository;
import com.ian.vca.repositories.cached.CachedDestinationEvaluationMappingRepository;
import com.ian.vca.repositories.cached.CachedDestinationTypeRepository;
import com.ian.vca.utils.SupportedEvaluation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Service
public class UserTransactionProcessor {

	private final UserValueTagStateRepository userValueTagStateRepository;
	private final UserValueTagsRepository userValueTagsRepository;
	private final CachedDestinationTypeRepository cachedDestinationTypeRepository;
	private final CachedDestinationEvaluationMappingRepository cachedDestinationEvaluationMappingRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final RestClient client;

	@KafkaListener(topics = "userTransaction", groupId = "VCA")
	public void consume(UserTransaction userTransaction) {

		BigInteger accountId = Objects.isNull(userTransaction.getAccountId()) ? BigInteger.ZERO : userTransaction.getAccountId();
		String accountHolderName = userTransaction.getAccountName();
		Integer destinationId = userTransaction.getDestinationId();
		BigDecimal amount = Objects.isNull(userTransaction.getAmount()) ? BigDecimal.ZERO : userTransaction.getAmount();
		Optional<DestinationType> destinationTypeOptional = cachedDestinationTypeRepository.findById(destinationId);
		LocalDateTime transactionDateTime = userTransaction.getTransactionDateTime();
		
		if (accountId.compareTo(BigInteger.ZERO) < 1) {
			
			String errorMessage = "customer account is not valid";
			sendErrorMessageToElastic(accountId.longValue(), accountHolderName, destinationId, amount.doubleValue(), 
					transactionDateTime.atZone(ZoneId.systemDefault()).toInstant(), errorMessage);
			
			return;
		}
		
		if (amount.compareTo(BigDecimal.ZERO) < 1) {
			
			String errorMessage = "transaction amount is missing";
			sendErrorMessageToElastic(accountId.longValue(), accountHolderName, destinationId, amount.doubleValue(), 
					transactionDateTime.atZone(ZoneId.systemDefault()).toInstant(), errorMessage);
			
			return;
		}

		if (destinationTypeOptional.isEmpty()) {
			
			String errorMessage = "destination type not found";
			sendErrorMessageToElastic(accountId.longValue(), accountHolderName, destinationId, amount.doubleValue(), 
					transactionDateTime.atZone(ZoneId.systemDefault()).toInstant(), errorMessage);
			
			return;
		}
		
		// get current user state
		DestinationType destinationType = destinationTypeOptional.get();

		UserValueTagStateId userValueTagStateId = new UserValueTagStateId();
		userValueTagStateId.setCustomerId(accountId);
		userValueTagStateId.setDestinationType(destinationType);
		Optional<UserValueTagState> valueTagOptional = userValueTagStateRepository.findById(userValueTagStateId);

		UserValueTagState userValueTagState;
		BigDecimal newAmount = amount;
		if (valueTagOptional.isPresent()) {

			userValueTagState = valueTagOptional.get();
			BigDecimal oldAmount = Objects.isNull(userValueTagState.getAmount()) ? BigDecimal.ZERO
					: userValueTagState.getAmount();
			newAmount = oldAmount.add(amount);
			userValueTagState.setAmount(newAmount);
			userValueTagState.setCount(userValueTagState.getCount() + 1l);
		} else {
			userValueTagState = new UserValueTagState();
			userValueTagState.setId(userValueTagStateId);
			userValueTagState.setCustomerName(accountHolderName);
			userValueTagState.setAmount(amount);
			userValueTagState.setCount(1l);
		}

		// get current user tags
		UserValueTags userValueTags = userValueTagsRepository.findById(accountId).orElse(new UserValueTags(accountId, accountHolderName));
		if (Objects.isNull(userValueTags)) {
			String errorMessage = "user value is not configured";
			sendErrorMessageToElastic(accountId.longValue(), accountHolderName, destinationId, amount.doubleValue(), 
					transactionDateTime.atZone(ZoneId.systemDefault()).toInstant(), errorMessage);
		}
		
		// evaluate states
		Long transactionCount = userValueTagState.getCount();
		List<DestinationEvaluationMapping> evaluations = cachedDestinationEvaluationMappingRepository
				.findById_DestinationType_DestinationId(destinationId);
		
		if (evaluations.isEmpty()) {
			String errorMessage = "evaluation is not configured";
			sendErrorMessageToElastic(accountId.longValue(), accountHolderName, destinationId, amount.doubleValue(), 
					transactionDateTime.atZone(ZoneId.systemDefault()).toInstant(), errorMessage);
		}
		
		for (var evaluation : evaluations) {

			Integer evaluationId = evaluation.getId().getEvaluationId();
			String newTag = evaluation.getTag();
			BigDecimal target = evaluation.getTarget();

			SupportedEvaluation eval = SupportedEvaluation.resolve(evaluationId);
			switch (eval) {
			case MIN_VALUE:
				if (newAmount.compareTo(target) >= 0) {
					addTags(newTag, userValueTags);
				}
				break;
			case MIN_COUNT:
				if (transactionCount.compareTo(target.longValue()) >= 0) {
					addTags(newTag, userValueTags);
				}
				break;
			default:
				
			}
		}

		userValueTagStateRepository.save(userValueTagState);
		
		// add State Modifier to another topic for future state deduction
		LocalDateTime dateRemoval = transactionDateTime.plusDays(90);
		
		StateModifier modifier = new StateModifier();
		modifier.setCustomerId(accountId);
		modifier.setAmount(amount);
		modifier.setTimestamp(dateRemoval);
		
		kafkaTemplate.send("modifier", modifier);
	}

	private void addTags(String newTag, UserValueTags userValueTags) {

		String currentTags = userValueTags.getTags();
		Set<String> tagsSet = StringUtils.hasText(currentTags) ? new HashSet<>(Arrays.asList(currentTags.split(",")))
				: new HashSet<>();
		tagsSet.add(newTag);

		String newTags = String.join(",", tagsSet);
		userValueTags.setTags(newTags);

		userValueTagsRepository.save(userValueTags);
	}

	private void sendErrorMessageToElastic(Long customerId, String customerName, Integer destinationId, 
			Double amount, Instant transactionTime, String errorMessage) {
		TransactionProcessingError error = new TransactionProcessingError();
		error.setCustomerId(customerId);
		error.setCustomerName(customerName);
		error.setDestinationId(destinationId);
		error.setAmount(amount);
		error.setTransactionTime(transactionTime);
		error.setErrorMessage("");
		
		String errorAsString = "";
		try {
			errorAsString = objectMapper.writeValueAsString(error);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Request request = new Request(RequestMethod.POST.name() , "transaction_processing_error/_doc");
		request.setOptions(RequestOptions.DEFAULT);
		request.setJsonEntity(errorAsString);
		try {
			Response response = client.performRequest(request);
			log.error("send error response: {}", EntityUtils.toString(response.getEntity()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
