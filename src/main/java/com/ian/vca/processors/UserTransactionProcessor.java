package com.ian.vca.processors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.ian.vca.configurations.KafkaConfiguration.UserTransaction;
import com.ian.vca.entities.UserValueTagState;
import com.ian.vca.entities.UserValueTagStateId;
import com.ian.vca.repositories.StateModifierQueueRepository;
import com.ian.vca.repositories.UserValueTagStateRepository;
import com.ian.vca.utils.DestinationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserTransactionProcessor {
	
	private final StateModifierQueueRepository stateModifierQueueRepository;
	private final UserValueTagStateRepository userValueTagStateRepository;

	@KafkaListener(topics = "userTransaction", groupId = "VCA")
    public void consume(UserTransaction userTransaction) {
		
		BigInteger accountId = userTransaction.getAccountId();
		String accountHolderName = userTransaction.getAccountHolderName();
		BigInteger destinationId = userTransaction.getDestinationAccountId();
		BigDecimal amount = Objects.isNull(userTransaction.getAmount()) ? BigDecimal.ZERO : userTransaction.getAmount();
		DestinationType destinationType = DestinationType.resolve(destinationId);
		
		UserValueTagStateId userValueTagStateId = new UserValueTagStateId();
		userValueTagStateId.setCustomerId(accountId);
		userValueTagStateId.setDestinationType(destinationType);
		Optional<UserValueTagState> valueTagOptional = userValueTagStateRepository.findById(userValueTagStateId);
		
		// get current user state
		UserValueTagState userValueTagState;
		BigDecimal newAmount = amount;
		if (valueTagOptional.isPresent()) {
			
			userValueTagState = valueTagOptional.get();
			BigDecimal oldAmount = Objects.isNull(userValueTagState.getAmount()) ? BigDecimal.ZERO : userValueTagState.getAmount();
			newAmount = oldAmount.add(amount);
			userValueTagState.setAmount(newAmount);
			userValueTagState.setCount(userValueTagState.getCount() + 1l);
		} else {
			
			userValueTagState = new UserValueTagState();
			userValueTagState.setId(userValueTagStateId);
			userValueTagState.setCustomerName(accountHolderName);
			userValueTagState.setAmount(amount);
			userValueTagState.setCount(BigInteger.ONE);
		}
		
		
		Long transactionCount = userValueTagState.getCount();
		// evaluate states
		switch (destinationType) {
		case MARKET_PLACE:
			BigDecimal shopperMinimumValue = BigDecimal.valueOf(500000000);
			if (amount.compareTo(shopperMinimumValue) >= 0) {
				// save the tags here
			}
			
			break;
		case TIME_DEPOSIT:
			BigDecimal timeDepositMinimumValue = BigDecimal.valueOf(500000000);
			if (amount.compareTo(timeDepositMinimumValue) >= 0) {
				// save the tags here
			}
			break;
		case MUTUAL_FUNDS:
			BigDecimal mutualFundsMinimumValue = BigDecimal.valueOf(500000000);
			if (amount.compareTo(mutualFundsMinimumValue) >= 0) {
				// save the tags here
			}
			break;
		case STOCK_MARKET:
			BigDecimal stockMarketMinimumValue = BigDecimal.valueOf(500000000);
			if (amount.compareTo(stockMarketMinimumValue) >= 0) {
				// save the tags here
			}
			break;
		case OTHER_ACCOUNT:
			Long transactionHubMinimumCount = 1000l;
			BigDecimal transactionHubMinimumValue = BigDecimal.valueOf(500000000);
			if (transactionCount.compareTo(transactionHubMinimumCount) >= 0 
					&& amount.compareTo(transactionHubMinimumValue) >= 0) {
				// save the tags here
			}
			break;
		default:
			break;
		}
		
		userValueTagStateRepository.save(userValueTagState);
    }
}
