package com.ian.vca.entities;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_user_value_tag_state")
public class UserValueTagState {

	@EmbeddedId
	private UserValueTagStateId id;
	
	@Column(name = "customer_name")
	private String customerName;
	
	@Column(name = "amount")
	private BigDecimal amount;
	
	@Column(name = "count")
	private Long count;

}
