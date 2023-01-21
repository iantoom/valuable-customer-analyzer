package com.ian.vca.repositories.cached;

import org.springframework.stereotype.Service;

import com.ian.vca.repositories.UserValueTagStateRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CachedUserValueTagStateRepository {

	private final UserValueTagStateRepository repository;
	
	
}
