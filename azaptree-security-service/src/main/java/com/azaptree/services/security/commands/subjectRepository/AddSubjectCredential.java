package com.azaptree.services.security.commands.subjectRepository;

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

import org.apache.commons.chain.Context;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.impl.CommandContextValidatorSupport;
import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.commons.TypeReferenceKey;
import com.azaptree.services.security.Credential;
import com.azaptree.services.security.DuplicateCredentialException;
import com.azaptree.services.security.SecurityCredentialsService;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.UnsupportedCredentialTypeException;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.HashedCredential;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.impl.HashedCredentialImpl;

public class AddSubjectCredential extends CommandSupport {

	@Autowired
	private HashedCredentialDAO hashedCredentialDAO;

	@Autowired
	private SubjectDAO subjectDAO;

	private final HashService hashService;
	private final UUID hashServiceId;

	@Autowired
	private SecurityCredentialsService securityCredentialsService;

	public static final TypeReferenceKey<UUID> SUBJECT_ID = CommandContextKeys.SUBJECT_ID;

	public static final TypeReferenceKey<UUID> UPDATED_BY_SUBJECT_ID = CommandContextKeys.UPDATED_BY_SUBJECT_ID;

	public static final TypeReferenceKey<Credential> CREDENTIAL = new TypeReferenceKey<Credential>("CREDENTIAL", true) {
		// intentionally empty
	};

	public static final TypeReferenceKey<HashedCredential> HASHED_CREDENTIAL = new TypeReferenceKey<HashedCredential>("HASHED_CREDENTIAL", true) {
		// intentionally empty
	};

	public AddSubjectCredential(final HashServiceConfiguration hashServiceConfig) {
		Assert.notNull(hashServiceConfig, "hashServiceConfig is required");

		hashService = hashServiceConfig.getHashService();
		hashServiceId = hashServiceConfig.getEntityId();

		setValidator(new CommandContextValidatorSupport() {

			@Override
			protected void checkInput(final Command command, final Context ctx) {
				final UUID subjectId = get(ctx, SUBJECT_ID);
				if (!subjectDAO.exists(subjectId)) {
					throw new UnknownSubjectException(subjectId.toString());
				}

				final Credential credential = get(ctx, CREDENTIAL);
				if (!securityCredentialsService.isCredentialSupported(credential.getName(), credential.getCredential())) {
					throw new UnsupportedCredentialTypeException();
				}

				if (hashedCredentialDAO.existsForSubjectIdAndName(subjectId, credential.getName())) {
					throw new DuplicateCredentialException(credential.getName());
				}

				final UUID updatedBySubjectId = get(ctx, UPDATED_BY_SUBJECT_ID);
				if (updatedBySubjectId != null) {
					Assert.isTrue(!subjectId.equals(updatedBySubjectId),
					        String.format("The updatedBy subject (%s) cannot be the same as the subject (%s) which we are adding the credential to",
					                subjectId,
					                updatedBySubjectId));
					if (!subjectDAO.exists(updatedBySubjectId)) {
						throw new UnknownSubjectException(String.format("Subject for updatedBy does not exist: %s", updatedBySubjectId));
					}
				}

			}

			@Override
			protected void checkOutput(final Command command, final Context ctx) {
				// none required
			}

		});

		setInputKeys(SUBJECT_ID, CREDENTIAL, UPDATED_BY_SUBJECT_ID);
		setOutputKeys(HASHED_CREDENTIAL);
	}

	@Transactional
	@Override
	protected boolean executeCommand(final Context ctx) {
		final UUID subjectId = get(ctx, SUBJECT_ID);
		final UUID updatedBySubjectId = get(ctx, UPDATED_BY_SUBJECT_ID);
		final Credential credential = get(ctx, CREDENTIAL);

		final byte[] credentialBytes = securityCredentialsService.convertCredentialToBytes(credential.getName(), credential.getCredential());
		final HashRequest hashRequest = new HashRequest.Builder().setSource(ByteSource.Util.bytes(credentialBytes)).build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredentialImpl hashedCred = new HashedCredentialImpl(
		        subjectId,
		        credential.getName(),
		        hashServiceId,
		        hash,
		        credential.getExpiresOn());
		if (updatedBySubjectId != null) {
			put(ctx, HASHED_CREDENTIAL, hashedCredentialDAO.create(hashedCred, updatedBySubjectId));
			subjectDAO.touch(subjectId, updatedBySubjectId);
		} else {
			put(ctx, HASHED_CREDENTIAL, hashedCredentialDAO.create(hashedCred));
			subjectDAO.touch(subjectId);
		}

		return CONTINUE_PROCESSING;
	}
}
