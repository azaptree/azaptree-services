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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.util.ByteSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.impl.CommandContextValidatorSupport;
import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.commons.TypeReferenceKey;
import com.azaptree.services.security.Credential;
import com.azaptree.services.security.SecurityCredentialsService;
import com.azaptree.services.security.UnknownCredentialException;
import com.azaptree.services.security.UnsupportedCredentialTypeException;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.impl.HashedCredentialImpl;
import com.google.common.base.Optional;

/**
 * If the command is successful, then the subject within the context will be updated, i.e., it will be replaced with the newly created instance
 * 
 * @author alfio
 * 
 */
public class CreateSubject extends CommandSupport {

	private final SubjectDAO subjectDAO;
	private final HashedCredentialDAO hashedCredentialDAO;

	private final HashService hashService;
	private final UUID hashServiceId;

	private final SecurityCredentialsService securityCredentialsService;

	public static final TypeReferenceKey<Credential[]> CREDENTIALS = new TypeReferenceKey<Credential[]>("CREDENTIALS", true) {
	};

	public static final TypeReferenceKey<Subject> SUBJECT = new TypeReferenceKey<Subject>("SUBJECT", true) {
	};

	public CreateSubject(final SubjectDAO subjectDAO, final HashedCredentialDAO hashCredentialDAO, final HashServiceConfiguration hashServiceConfig,
	        final SecurityCredentialsService securityCredentialsService) {
		Assert.notNull(subjectDAO, "subjectDAO is required");
		Assert.notNull(hashCredentialDAO, "hashCredentialDAO is required");
		Assert.notNull(hashServiceConfig, "hashServiceConfig is required");
		Assert.notNull(securityCredentialsService, "securityCredentialsService is required");
		this.subjectDAO = subjectDAO;
		this.hashedCredentialDAO = hashCredentialDAO;
		this.hashService = hashServiceConfig.getHashService();
		this.hashServiceId = hashServiceConfig.getEntityId();
		this.securityCredentialsService = securityCredentialsService;

		this.setValidator(new CommandContextValidatorSupport() {

			@Override
			protected void checkOutput(Command command, Context ctx) {
				// none required
			}

			@Override
			protected void checkInput(Command command, Context ctx) {
				// check that the subject does not have an entity id, which would imply the subject already exists in the database
				final Subject subject = get(ctx, SUBJECT);
				Assert.isNull(subject.getEntityId());

				final Credential[] credentials = get(ctx, CREDENTIALS);
				Assert.isTrue(ArrayUtils.isNotEmpty(credentials), "credentials are required");

				final Set<String> names = new HashSet<>();
				for (Credential credential : credentials) {
					if (!names.add(credential.getName())) {
						throw new IllegalArgumentException("Duplicate credential name: " + credential.getName());
					}

					if (!securityCredentialsService.isCredentialSupported(credential.getName(), credential.getCredential())) {
						if (securityCredentialsService.getSupportedCredentials().containsKey(credential.getName())) {
							throw new UnknownCredentialException(credential.getName());
						}

						throw new UnsupportedCredentialTypeException(String.format("credential is not supported : %s -> %s", credential.getName(), credential
						        .getCredential().getClass().getName()));
					}
				}
			}
		});
	}

	@Transactional
	@Override
	protected boolean executeCommand(final Context ctx) {
		final Subject subject = get(ctx, SUBJECT);
		final Optional<UUID> createdBy = subject.getCreatedByEntityId();
		final UUID createdByEntityId = createdBy.isPresent() ? createdBy.get() : null;

		final Subject createdSubject = createdByEntityId != null ? subjectDAO.create(subject, createdByEntityId) : subjectDAO.create(subject);

		final Credential[] credentials = get(ctx, CREDENTIALS);
		for (Credential credential : credentials) {
			final byte[] credentialBytes = securityCredentialsService.convertCredentialToBytes(credential.getName(), credential.getCredential());
			final HashRequest hashRequest = new HashRequest.Builder().setSource(ByteSource.Util.bytes(credentialBytes)).build();
			final Hash hash = hashService.computeHash(hashRequest);
			final HashedCredentialImpl hashedCred = new HashedCredentialImpl(
			        createdSubject.getEntityId(),
			        credential.getName(),
			        hashServiceId,
			        hash,
			        credential.getExpiresOn());
			if (createdByEntityId != null) {
				hashedCredentialDAO.create(hashedCred, createdByEntityId);
			} else {
				hashedCredentialDAO.create(hashedCred);
			}
		}

		put(ctx, SUBJECT, createdSubject);

		return CONTINUE_PROCESSING;
	}

	@Override
	public Optional<TypeReferenceKey<?>[]> getInputKeys() {
		return Optional.of(new TypeReferenceKey<?>[] { SUBJECT, CREDENTIALS });
	}

	@Override
	public Optional<TypeReferenceKey<?>[]> getOutputKeys() {
		return Optional.of(new TypeReferenceKey<?>[] { SUBJECT });
	}

}
