package com.ian.vca.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.vca.entities.DestinationEvaluationMapping;
import com.ian.vca.entities.DestinationEvaluationMappingId;

@Repository
public interface DestinationEvaluationMappingRepository
		extends JpaRepository<DestinationEvaluationMapping, DestinationEvaluationMappingId> {

	List<DestinationEvaluationMapping> findById_DestinationType_DestinationId(Integer destinationId);

	
}
