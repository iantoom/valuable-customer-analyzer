package com.ian.vca.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "t_destination_type")
public class DestinationType implements Serializable {

	private static final long serialVersionUID = 1683005952583373370L;

	@Id
	@Column(name = "destination_id")
	private Integer destinationId;
	
	@Column(name = "destination_name")
	private String destinationName;
}
