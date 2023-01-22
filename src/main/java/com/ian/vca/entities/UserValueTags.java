package com.ian.vca.entities;

import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "t_user_value_tags")
public class UserValueTags {

	@Id
	@Column(name = "customer_id")
	private BigInteger customerId;
	
	@Column(name = "customer_name")
	private String customerName;
	
	@Column(name = "tags")
	private String tags;
	
	public UserValueTags(BigInteger customerId, String customerName) {
		this.customerId = customerId;
		this.customerName = customerName;
	}
}
