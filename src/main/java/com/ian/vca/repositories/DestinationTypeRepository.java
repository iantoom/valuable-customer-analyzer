package com.ian.vca.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.vca.entities.DestinationType;

@Repository
public interface DestinationTypeRepository extends JpaRepository<DestinationType, Integer>{

	
}
