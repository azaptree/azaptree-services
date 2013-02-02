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

import com.azaptree.services.security.domain.Session;

public interface SessionManagementService {

	/**
	 * 
	 * @param subjectId
	 * @return
	 * @throws SessionManagementServiceException
	 * @throws UnknownSubjectException
	 * @throws ExceededMaxSessionsPerSubjectException
	 * @throws SubjectNotActivatedException
	 */
	UUID createSession(UUID subjectId) throws SessionManagementServiceException, UnknownSubjectException, ExceededMaxSessionsPerSubjectException,
	        SubjectNotActivatedException;

	/**
	 * 
	 * @param sessionId
	 * @return
	 * @throws SessionManagementServiceException
	 * @throws UnknownSessionException
	 */
	String[] getAttributeKeys(UUID sessionId) throws SessionManagementServiceException, UnknownSessionException;

	/**
	 * 
	 * @param sessionId
	 * @return
	 * @throws SessionManagementServiceException
	 * @throws UnknownSessionException
	 */
	Session getSession(UUID sessionId) throws SessionManagementServiceException, UnknownSessionException;

	/**
	 * 
	 * @param subjectId
	 * @return
	 * @throws SessionManagementServiceException
	 * @throws UnknownSubjectException
	 */
	UUID[] getSessionIdsBySubject(UUID subjectId) throws SessionManagementServiceException, UnknownSubjectException;

	/**
	 * Explicitly invalidates the session and releases all associated resources.
	 * 
	 * @param sessionId
	 * @throws SessionManagementServiceException
	 * @throws UnknownSessionException
	 */
	void invalidateSession(UUID sessionId) throws SessionManagementServiceException, UnknownSessionException;

	/**
	 * Explicitly invalidates all active sessions for the specified subject and releases all associated resources.
	 * 
	 * @param subjectId
	 * @throws SessionManagementServiceException
	 * @throws UnknownSubjectException
	 */
	void invalidateSessionsBySubjectId(UUID subjectId) throws SessionManagementServiceException, UnknownSubjectException;

	/**
	 * 
	 * @param sessionId
	 * @param key
	 * @throws SessionManagementServiceException
	 * @throws UnknownSessionException
	 */
	void removeAttribute(UUID sessionId, String key) throws SessionManagementServiceException, UnknownSessionException;

	/**
	 * 
	 * @param sessionId
	 * @param key
	 * @param jsonValue
	 * @throws SessionManagementServiceException
	 * @throws UnknownSessionException
	 */
	void setAttribute(UUID sessionId, String key, String jsonValue) throws SessionManagementServiceException, UnknownSessionException;

	/**
	 * Explicitly updates the lastAccessTime of the session to the current time when this method is invoked.
	 * 
	 * @param sessionId
	 * @throws SessionManagementServiceException
	 * @throws UnknownSessionException
	 */
	void touchSession(UUID sessionId) throws SessionManagementServiceException, UnknownSessionException;

}
