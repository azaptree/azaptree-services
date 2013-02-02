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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Map;
import java.util.UUID;

import com.azaptree.services.security.domain.Subject;

public interface SubjectRepositoryService {
	/**
	 * If a credential with the same name already exists, then
	 * 
	 * @param subjectId
	 * @param credentialName
	 * @param credential
	 * @throws DuplicateCredentialException
	 * @throws UnknownSubjectException
	 */
	void addSubjectCredential(UUID subjectId, String credentialName, Object credential) throws SecurityServiceException, DuplicateCredentialException,
	        UnknownSubjectException;

	/**
	 * Creates a new Subject with the specified credentials in the database.
	 * 
	 * Credentials will be hashed before storing in the database
	 * 
	 * @param credentials
	 *            at least one credential is required.
	 * @return
	 */
	Subject createSubject(Map<String, Object> credentials) throws SecurityServiceException;

	/**
	 * 
	 * @param subjectId
	 * @return
	 * @throws SecurityServiceException
	 */
	boolean deleteSubject(UUID subjectId) throws SecurityServiceException, UnknownSubjectException;

	/**
	 * 
	 * @param subjectId
	 * @param credentialName
	 * @param credential
	 * @throws SecurityServiceException
	 * @throws UnknownCredentialException
	 *             if there was not credential with that name found to delete
	 * @throws UnknownSubjectException
	 */
	void deleteSubjectCredential(UUID subjectId, String credentialName) throws SecurityServiceException, UnknownCredentialException, UnknownSubjectException;

	Subject getSubject(UUID subjectId) throws SecurityServiceException;

}
