package com.ian.vca.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "t_destination_evaluation_mapping")
public class DestinationEvaluationMapping implements Serializable {

	private static final long serialVersionUID = 6190021026676856845L;
	
	@EmbeddedId
	private DestinationEvaluationMappingId id;
	
	@Column(name = "target")
	private BigDecimal target;
	
	@Column(name = "tag")
	private String tag;
}
