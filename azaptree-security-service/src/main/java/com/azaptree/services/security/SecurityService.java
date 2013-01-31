package com.azaptree.services.security;

/*
 * #%L
 * AZAPTREE SECURITY SERVICE
 * %%
 * Copyright (C) 2012 - 2013 AZAPTREE.COM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

	HashService getHashService(UUID hashServiceConfiguationId);

	HashService getHashService(String name);

	RandomNumberGenerator getRandomNumberGenerator();

	Subject getSubject(UUID subjectId);
}
