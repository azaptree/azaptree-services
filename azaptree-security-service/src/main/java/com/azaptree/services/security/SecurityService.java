package com.azaptree.services.security;

import java.util.UUID;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.hash.HashService;

import com.azaptree.services.security.domain.Hash;
import com.azaptree.services.security.domain.Subject;

public interface SecurityService {

	/**
	 * In order for the subject to be authenticated, there must exist a Subject with the same UUID and matching credentials - all credentials must match.
	 * 
	 * @param token
	 */
	void authenticate(SubjectAuthenticationToken token);

	/**
	 * Creates a new Subject with the specified credentials in the database.
	 * 
	 * @param credentials
	 *            at least one credential is required.
	 * @return
	 */
	Subject createSubject(Hash... credentials);

	HashService getHashService();

	RandomNumberGenerator getRandomNumberGenerator();

	Subject getSubject(UUID subjectId);
}
