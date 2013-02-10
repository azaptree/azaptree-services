package com.azaptree.services.security.impl;

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
