package com.ian.vca.entities;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_state_modifier_queue")
public class StateModifierQueue {

	@Id
	@Column(name = "customer_id")
	private BigInteger customerId;
	
	@Column(name = "timestamp")
	private LocalDate timestamp;
	
	@Column(name = "amount")
	private BigDecimal amount;
	
	@Column(name = "is_deducted")
	private boolean deducted;
}
