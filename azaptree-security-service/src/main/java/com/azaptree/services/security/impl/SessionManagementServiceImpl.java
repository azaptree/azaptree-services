package com.azaptree.services.security.impl;

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

import java.util.UUID;

import com.azaptree.services.security.ExceededMaxSessionsPerSubjectException;
import com.azaptree.services.security.SessionManagementService;
import com.azaptree.services.security.SessionManagementServiceException;
import com.azaptree.services.security.SubjectNotActivatedException;
import com.azaptree.services.security.UnknownSessionException;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.domain.Session;

public class SessionManagementServiceImpl implements SessionManagementService {

	@Override
	public UUID createSession(UUID subjectId) throws SessionManagementServiceException, UnknownSubjectException, ExceededMaxSessionsPerSubjectException,
	        SubjectNotActivatedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAttributeKeys(UUID sessionId) throws SessionManagementServiceException, UnknownSessionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session getSession(UUID sessionId) throws SessionManagementServiceException, UnknownSessionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID[] getSessionIdsBySubject(UUID subjectId) throws SessionManagementServiceException, UnknownSubjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invalidateSession(UUID sessionId) throws SessionManagementServiceException, UnknownSessionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void invalidateSessionsBySubjectId(UUID subjectId) throws SessionManagementServiceException, UnknownSubjectException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAttribute(UUID sessionId, String key) throws SessionManagementServiceException, UnknownSessionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttribute(UUID sessionId, String key, String jsonValue) throws SessionManagementServiceException, UnknownSessionException {
		// TODO Auto-generated method stub

	}

	@Override
	public void touchSession(UUID sessionId) throws SessionManagementServiceException, UnknownSessionException {
		// TODO Auto-generated method stub

	}

}
