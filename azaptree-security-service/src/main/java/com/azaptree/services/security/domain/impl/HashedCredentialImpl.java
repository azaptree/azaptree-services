package com.azaptree.services.security.domain.impl;

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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.util.ByteSource;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.impl.DomainVersionedEntity;
import com.azaptree.services.security.domain.HashedCredential;
import com.google.common.base.Optional;

public class HashedCredentialImpl extends DomainVersionedEntity implements HashedCredential {

	protected final String name;
	protected final UUID subjectId;
	protected final UUID hashServiceConfigurationId;
	protected final byte[] hash;
	protected final String hashAlgorithm;
	protected final int hashIterations;
	protected final byte[] salt;
	protected final Date expiresOn;

	/**
	 * Copy constructor
	 * 
	 * @param entity
	 */
	public HashedCredentialImpl(final HashedCredential entity) {
		super(entity);
		validate(entity.getSubjectId(), entity.getName(), entity.getHashServiceConfigurationId(), entity.getHash(), entity.getHashAlgorithm(),
		        entity.getHashIterations(), entity.getSalt());
		subjectId = entity.getSubjectId();
		hashServiceConfigurationId = entity.getHashServiceConfigurationId();
		name = entity.getName();
		hash = entity.getHash();
		hashAlgorithm = entity.getHashAlgorithm();
		hashIterations = entity.getHashIterations();
		salt = entity.getSalt();
		final Optional<Date> expiration = entity.getExpiresOn();
		expiresOn = expiration.isPresent() ? expiration.get() : null;
	}

	/**
	 * Use case: Purpose is to update an existing HashedCredential. Because all fields are final, we need to create a new instance that copies over the
	 * VersionEntity fields and updates the HashedCredential specific fields
	 * 
	 * 
	 * @param entity
	 *            only used to copy over the VersionedEntity fields and the HashedCredential subject idendifying unique key fields : subjectId, name
	 * @param subjectId
	 * @param name
	 * @param hash
	 * @param hashAlgorithm
	 * @param hashIterations
	 * @param salt
	 */
	public HashedCredentialImpl(final HashedCredential entity, final byte[] hash, final String hashAlgorithm,
	        final int hashIterations, final byte[] salt, final Date expiresOn) {
		super(entity);
		validate(entity.getSubjectId(), entity.getName(), entity.getHashServiceConfigurationId(), hash, hashAlgorithm, hashIterations, salt);
		subjectId = entity.getSubjectId();
		hashServiceConfigurationId = entity.getHashServiceConfigurationId();
		name = entity.getName();
		this.hash = hash;
		this.hashAlgorithm = hashAlgorithm;
		this.hashIterations = hashIterations;
		this.salt = salt;
		this.expiresOn = expiresOn;
	}

	/**
	 * Purpose is to create new HashedCredentials
	 * 
	 * @param subjectId
	 * @param name
	 * @param hash
	 * @param hashAlgorithm
	 * @param hashIterations
	 * @param salt
	 */
	public HashedCredentialImpl(final UUID subjectId, final String name, final UUID hashServiceConfigurationId, final byte[] hash, final String hashAlgorithm,
	        final int hashIterations, final byte[] salt, final Date expiresOn) {
		validate(subjectId, name, hashServiceConfigurationId, hash, hashAlgorithm, hashIterations, salt);
		this.hashServiceConfigurationId = hashServiceConfigurationId;
		this.subjectId = subjectId;
		this.name = name;
		this.hash = hash;
		this.hashAlgorithm = hashAlgorithm;
		this.hashIterations = hashIterations;
		this.salt = salt;
		this.expiresOn = expiresOn;
	}

	public HashedCredentialImpl(final UUID subjectId, final String name, final UUID hashServiceConfigurationId, final Hash hash, final Date expiresOn) {
		this(subjectId, name, hashServiceConfigurationId, hash.getBytes(), hash.getAlgorithmName(), hash.getIterations(), hash.getSalt().getBytes(), expiresOn);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final HashedCredentialImpl other = (HashedCredentialImpl) obj;
		return Arrays.equals(hash, other.hash);
	}

	@Override
	public Optional<Date> getExpiresOn() {
		if (expiresOn == null) {
			return Optional.absent();
		}
		return Optional.of(expiresOn);
	}

	@Override
	public byte[] getHash() {
		return hash;
	}

	@Override
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	@Override
	public int getHashIterations() {
		return hashIterations;
	}

	@Override
	public UUID getHashServiceConfigurationId() {
		return hashServiceConfigurationId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getSalt() {
		return salt;
	}

	@Override
	public UUID getSubjectId() {
		return subjectId;
	}

	@Override
	public int hashCode() {
		return ByteSource.Util.bytes(hash).toBase64().hashCode();
	}

	private void validate(final UUID subjectId, final String name, final UUID hashServiceConfigurationId, final byte[] hash, final String hashAlgorithm,
	        final int hashIterations,
	        final byte[] salt) {
		Assert.notNull(subjectId, "subjectId is required");
		Assert.hasText(name);
		Assert.notNull(hashServiceConfigurationId, "hashServiceConfigurationId is required");
		Assert.isTrue(ArrayUtils.isNotEmpty(hash), "hash is required");
		Assert.isTrue(ArrayUtils.isNotEmpty(salt), "salt is required");
		Assert.hasText(hashAlgorithm, "hashAlgorithm is required");
		try {
			MessageDigest.getInstance(hashAlgorithm);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Unknown algorithm : " + hashAlgorithm, e);
		}
		Assert.isTrue(hashIterations > 0, "contraint failed: hashIterations > 0");

	}

}
