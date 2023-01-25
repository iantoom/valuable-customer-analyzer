package com.ian.vca.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Embeddable
public class DestinationEvaluationMappingId implements Serializable {

	private static final long serialVersionUID = 8095952584040254773L;

	@Column(name = "evaluation_id")
	private Integer evaluationId;
	
	@ManyToOne
	@JoinColumn(name = "destination_id", referencedColumnName = "destination_id")
	private DestinationType destinationType;
}
