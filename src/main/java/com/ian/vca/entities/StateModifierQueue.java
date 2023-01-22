package com.ian.vca.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "t_state_modifier_queue")
public class StateModifierQueue implements Serializable {

	private static final long serialVersionUID = -4168980305781621433L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "customer_id")
	private BigInteger customerId;
	
	@Column(name = "timestamp")
	private LocalDateTime timestamp;
	
	@Column(name = "amount")
	private BigDecimal amount;
}
