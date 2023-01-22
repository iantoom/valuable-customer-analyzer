package com.ian.vca.processors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ian.vca.configurations.KafkaConfiguration.StateModifier;
import com.ian.vca.configurations.KafkaConfiguration.UserTransaction;
import com.ian.vca.entities.DestinationEvaluationMapping;
import com.ian.vca.entities.DestinationType;
import com.ian.vca.entities.UserValueTagState;
import com.ian.vca.entities.UserValueTagStateId;
import com.ian.vca.entities.UserValueTags;
import com.ian.vca.repositories.UserValueTagStateRepository;
import com.ian.vca.repositories.UserValueTagsRepository;
import com.ian.vca.repositories.cached.CachedDestinationEvaluationMappingRepository;
import com.ian.vca.repositories.cached.CachedDestinationTypeRepository;
import com.ian.vca.utils.SupportedEvaluation;

import lombok.RequiredArgsConstructor;


//@Slf4j
@RequiredArgsConstructor
@Service
public class UserTransactionProcessor {

	private final UserValueTagStateRepository userValueTagStateRepository;
	private final UserValueTagsRepository userValueTagsRepository;
	private final CachedDestinationTypeRepository cachedDestinationTypeRepository;
	private final CachedDestinationEvaluationMappingRepository cachedDestinationEvaluationMappingRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@KafkaListener(topics = "userTransaction", groupId = "VCA")
	public void consume(UserTransaction userTransaction) {

		BigInteger accountId = userTransaction.getAccountId();
		String accountHolderName = userTransaction.getAccountName();
		Integer destinationId = userTransaction.getDestinationId();
		BigDecimal amount = Objects.isNull(userTransaction.getAmount()) ? BigDecimal.ZERO : userTransaction.getAmount();
		Optional<DestinationType> destinationTypeOptional = cachedDestinationTypeRepository.findById(destinationId);
		LocalDateTime transactionDateTime = userTransaction.getTransactionDateTime();
		
		// TODO - validate the parameters and send the Data to elastic
		if (destinationTypeOptional.isEmpty()) {
			
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
		
		// evaluate states
		Long transactionCount = userValueTagState.getCount();
		List<DestinationEvaluationMapping> evaluations = cachedDestinationEvaluationMappingRepository
				.findById_DestinationType_DestinationId(destinationId);

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
				// send unsupported evaluation id
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

}
