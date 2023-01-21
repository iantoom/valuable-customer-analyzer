package com.ian.vca.entities;

import java.io.Serializable;
import java.math.BigInteger;

import com.ian.vca.utils.DestinationType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class UserValueTagStateId implements Serializable {

	private static final long serialVersionUID = 291265253507484263L;

	@Column(name = "customer_id")
	private BigInteger customerId;
	
	@Column(name = "destination_type")
	private DestinationType destinationType;
}
