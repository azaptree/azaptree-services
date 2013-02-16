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

import com.azaptree.services.security.domain.Subject;

public interface SubjectRepositoryService {
	/**
	 * 
	 * @param subjectId
	 *            REQUIRED
	 * @param credentialName
	 *            REQUIRED
	 * @param credential
	 *            REQUIRED
	 * @throws DuplicateCredentialException
	 *             If a credential with the same name already exists for the specified subject
	 * @throws UnknownSubjectException
	 */
	void addSubjectCredential(UUID subjectId, Credential credential) throws SecurityServiceException, DuplicateCredentialException,
	        UnknownSubjectException, UnsupportedCredentialTypeException;

	/**
	 * 
	 * @param subjectId
	 *            REQUIRED
	 * @param credentialName
	 *            REQUIRED
	 * @param credential
	 *            REQUIRED
	 * @param updatedBySubjectId
	 *            REQUIRED
	 * @throws DuplicateCredentialException
	 *             If a credential with the same name already exists for the specified subject
	 * @throws UnknownSubjectException
	 */
	void addSubjectCredential(UUID subjectId, Credential credential, UUID updatedBySubjectId) throws SecurityServiceException, DuplicateCredentialException,
	        UnknownSubjectException, UnsupportedCredentialTypeException;

	/**
	 * Creates a new Subject with the specified credentials in the database.
	 * 
	 * Credentials will be hashed before storing in the database
	 * 
	 * @param subject
	 *            REQUIRED
	 * @param credentials
	 *            at least one credential is required.
	 * @return
	 * @throws UnknownCredentialException
	 *             if the credential name is invalid
	 * @throws UnsupportedCredentialTypeException
	 *             if the credential type is unsupported for the corresponding credential name
	 * @throws SecurityServiceException
	 */
	Subject createSubject(Subject subject, Credential... credentials) throws SecurityServiceException, UnsupportedCredentialTypeException,
	        UnknownCredentialException;

	/**
	 * 
	 * @param subjectId
	 * @return
	 * @throws SecurityServiceException
	 */
	boolean deleteSubject(UUID subjectId) throws SecurityServiceException;

	/**
	 * 
	 * @param subjectId
	 *            REQUIRED
	 * @param credentialName
	 *            REQUIRED
	 * @param credential
	 * @throws SecurityServiceException
	 * @throws UnknownCredentialException
	 *             if there was not credential with that name found to delete
	 * @throws UnknownSubjectException
	 */
	void deleteSubjectCredential(UUID subjectId, String credentialName) throws SecurityServiceException, UnknownCredentialException, UnknownSubjectException;

	/**
	 * 
	 * @param subjectId
	 *            REQUIRED
	 * @param credentialName
	 *            REQUIRED
	 * @param credential
	 * @param updatedBySubjectId
	 *            REQUIRED
	 * @throws SecurityServiceException
	 * @throws UnknownCredentialException
	 *             if there was not credential with that name found to delete
	 * @throws UnknownSubjectException
	 */
	void deleteSubjectCredential(UUID subjectId, String credentialName, UUID updatedBySubjectId) throws SecurityServiceException, UnknownCredentialException,
	        UnknownSubjectException;

	Subject getSubject(UUID subjectId) throws SecurityServiceException;

}
