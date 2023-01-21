package com.ian.vca.entities;

import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_user_value_tags")
public class UserValueTags {

	@Id
	@Column(name = "customer_id")
	private BigInteger customerId;
	
	@Column(name = "tags")
	private String tags;
}
