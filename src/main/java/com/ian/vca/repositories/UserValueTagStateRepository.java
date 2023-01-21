package com.ian.vca.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.vca.entities.UserValueTagState;
import com.ian.vca.entities.UserValueTagStateId;

@Repository
public interface UserValueTagStateRepository extends JpaRepository<UserValueTagState, UserValueTagStateId> {

}
