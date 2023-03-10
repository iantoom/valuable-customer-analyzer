package com.ian.vca.entities;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Embeddable
public class UserValueTagStateId implements Serializable {

	private static final long serialVersionUID = 291265253507484263L;

	@Column(name = "customer_id")
	private BigInteger customerId;
	
	@ManyToOne
	@JoinColumn(name = "destination_id", referencedColumnName = "destination_id")
	private DestinationType destinationType;
}
