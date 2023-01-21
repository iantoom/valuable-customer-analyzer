package com.ian.vca.repositories;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.vca.entities.StateModifierQueue;

@Repository
public interface StateModifierQueueRepository extends JpaRepository<StateModifierQueue, BigInteger>{

	
}
