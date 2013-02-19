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

import org.apache.shiro.crypto.hash.HashService;

import com.azaptree.services.security.domain.config.HashServiceConfiguration;

public interface SecurityService {

	HashService getHashService() throws SecurityServiceException;

	HashService getHashService(String name) throws SecurityServiceException;

	HashService getHashService(UUID hashServiceConfiguationId) throws SecurityServiceException;

	/**
	 * Returns the HashServiceConfiguration used to create the HashService returned by getHashService() .
	 * 
	 * @return
	 * @throws SecurityServiceException
	 */
	HashServiceConfiguration getHashServiceConfiguration() throws SecurityServiceException;

	UUID getHashServiceId(String name) throws SecurityServiceException;

	/**
	 * In order for the subject to be authenticated, there must exist a Subject with the same UUID and matching credentials - all credentials must match.
	 * 
	 * A new session will be created for the authenticated subject.
	 * 
	 * @param token
	 * @return UUID - session id
	 */
	UUID login(SubjectAuthenticationToken token) throws SecurityServiceException, AuthenticationException;

	/**
	 * Invalidates the subject's session. If the subject logged in with
	 * 
	 * @param sessionId
	 * @throws SecurityServiceException
	 */
	void logout(UUID sessionId) throws SecurityServiceException;
}
