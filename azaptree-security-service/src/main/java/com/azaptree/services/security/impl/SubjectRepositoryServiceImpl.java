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

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.azaptree.services.command.CommandService;
import com.azaptree.services.security.DuplicateCredentialException;
import com.azaptree.services.security.SecurityServiceException;
import com.azaptree.services.security.SubjectRepositoryService;
import com.azaptree.services.security.UnknownCredentialException;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.domain.Subject;

public class SubjectRepositoryServiceImpl implements SubjectRepositoryService {

	@Autowired
	private CommandService commandService;

	@Override
	public void addSubjectCredential(UUID subjectId, String credentialName, Object credential) throws SecurityServiceException, DuplicateCredentialException,
	        UnknownSubjectException {
		// TODO Auto-generated method stub

	}

	@Override
	public Subject createSubject(Map<String, Object> credentials) throws SecurityServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteSubject(UUID subjectId) throws SecurityServiceException, UnknownSubjectException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteSubjectCredential(UUID subjectId, String credentialName) throws SecurityServiceException, UnknownCredentialException,
	        UnknownSubjectException {
		// TODO Auto-generated method stub

	}

	@Override
	public Subject getSubject(UUID subjectId) throws SecurityServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
