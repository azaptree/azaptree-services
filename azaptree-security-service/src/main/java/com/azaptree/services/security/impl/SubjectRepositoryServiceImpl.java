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

import org.springframework.beans.factory.annotation.Autowired;

import com.azaptree.services.command.CommandContext;
import com.azaptree.services.command.CommandKey;
import com.azaptree.services.command.CommandService;
import com.azaptree.services.security.Credential;
import com.azaptree.services.security.DuplicateCredentialException;
import com.azaptree.services.security.SecurityServiceException;
import com.azaptree.services.security.SubjectRepositoryService;
import com.azaptree.services.security.UnknownCredentialException;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.UnsupportedCredentialTypeException;
import com.azaptree.services.security.commands.subjectRepository.AddSubjectCredential;
import com.azaptree.services.security.commands.subjectRepository.CreateSubject;
import com.azaptree.services.security.commands.subjectRepository.DeleteSubject;
import com.azaptree.services.security.commands.subjectRepository.DeleteSubjectCredential;
import com.azaptree.services.security.config.CommandServiceConfiguration;
import com.azaptree.services.security.domain.Subject;

public class SubjectRepositoryServiceImpl implements SubjectRepositoryService {

	@Autowired
	private CommandService commandService;

	@Override
	public void addSubjectCredential(final UUID subjectId, final Credential credential) throws SecurityServiceException, DuplicateCredentialException,
	        UnknownSubjectException {
		final CommandContext ctx = new CommandContext();
		ctx.put(AddSubjectCredential.SUBJECT_ID, subjectId);
		ctx.put(AddSubjectCredential.CREDENTIAL, credential);
		executeCommand(CommandServiceConfiguration.ADD_SUBJECT_CREDENTIAL, ctx);
	}

	@Override
	public void addSubjectCredential(final UUID subjectId, final Credential credential, final UUID updatedBySubjectId) throws SecurityServiceException,
	        DuplicateCredentialException, UnknownSubjectException, UnsupportedCredentialTypeException {
		final CommandContext ctx = new CommandContext();
		ctx.put(AddSubjectCredential.SUBJECT_ID, subjectId);
		ctx.put(AddSubjectCredential.CREDENTIAL, credential);
		ctx.put(AddSubjectCredential.UPDATED_BY_SUBJECT_ID, updatedBySubjectId);
		executeCommand(CommandServiceConfiguration.ADD_SUBJECT_CREDENTIAL, ctx);
	}

	@Override
	public Subject createSubject(final Subject subject, final Credential... credentials) throws SecurityServiceException, UnsupportedCredentialTypeException,
	        UnknownCredentialException {
		final CommandContext ctx = new CommandContext();
		ctx.put(CreateSubject.SUBJECT, subject);
		ctx.put(CreateSubject.CREDENTIALS, credentials);
		executeCommand(CommandServiceConfiguration.CREATE_SUBJECT, ctx);
		return ctx.get(CreateSubject.SUBJECT);
	}

	@Override
	public boolean deleteSubject(final UUID subjectId) throws SecurityServiceException, UnknownSubjectException {
		final CommandContext ctx = new CommandContext();
		ctx.put(DeleteSubject.SUBJECT_ID, subjectId);
		try {
			executeCommand(CommandServiceConfiguration.DELETE_SUBJECT, ctx);
			return true;
		} catch (final UnknownSubjectException e) {
			return false;
		} catch (final SecurityServiceException e) {
			throw e;
		}
	}

	@Override
	public boolean deleteSubjectCredential(final UUID subjectId, final String credentialName) throws SecurityServiceException, UnknownSubjectException {
		final CommandContext ctx = new CommandContext();
		ctx.put(DeleteSubjectCredential.SUBJECT_ID, subjectId);
		ctx.put(DeleteSubjectCredential.CREDENTIAL_NAME, credentialName);
		try {
			executeCommand(CommandServiceConfiguration.DELETE_SUBJECT_CREDENTIAL, ctx);
			return true;
		} catch (final UnknownCredentialException e) {
			return false;
		} catch (final SecurityServiceException e) {
			throw e;
		}
	}

	@Override
	public boolean deleteSubjectCredential(final UUID subjectId, final String credentialName, final UUID updatedBySubjectId) throws SecurityServiceException,
	        UnknownCredentialException, UnknownSubjectException {
		final CommandContext ctx = new CommandContext();
		ctx.put(DeleteSubjectCredential.SUBJECT_ID, subjectId);
		ctx.put(DeleteSubjectCredential.CREDENTIAL_NAME, credentialName);
		ctx.put(DeleteSubjectCredential.UPDATED_BY_SUBJECT_ID, updatedBySubjectId);
		try {
			executeCommand(CommandServiceConfiguration.DELETE_SUBJECT_CREDENTIAL, ctx);
			return true;
		} catch (final UnknownCredentialException e) {
			return false;
		} catch (final SecurityServiceException e) {
			throw e;
		}

	}

	private void executeCommand(final CommandKey commandKey, final CommandContext ctx) {
		try {
			commandService.execute(commandKey, ctx);
		} catch (final SecurityException e) {
			throw e;
		} catch (final Exception e) {
			throw new SecurityServiceException(e);
		}
	}

	@Override
	public Subject getSubject(final UUID subjectId) throws SecurityServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
