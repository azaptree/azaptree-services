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
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.util.ByteSource;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.impl.DomainVersionedEntity;
import com.azaptree.services.security.domain.HashedCredential;

public class HashedCredentialImpl extends DomainVersionedEntity implements HashedCredential {

	private final String name;
	protected final UUID subjectId;
	protected final byte[] hash;
	private final String hashAlgorithm;
	private final int hashIterations;
	private final byte[] salt;

	public HashedCredentialImpl(final UUID subjectId, final String name, final byte[] hash, final String hashAlgorithm, final int hashIterations,
	        final byte[] salt) {
		Assert.notNull(subjectId, "subjectId is required");
		Assert.hasText(name);
		Assert.isTrue(ArrayUtils.isNotEmpty(salt), "salt is required");
		Assert.hasText(hashAlgorithm, "hashAlgorithm is required");
		try {
			MessageDigest.getInstance(hashAlgorithm);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Unknown algorithm : " + hashAlgorithm, e);
		}
		Assert.isTrue(hashIterations > 0, "contraint failed: hashIterations > 0");

		this.subjectId = subjectId;
		this.name = name;
		this.hash = hash;
		this.hashAlgorithm = hashAlgorithm;
		this.hashIterations = hashIterations;
		this.salt = salt;
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
	public String getName() {
		return name;
	}

	@Override
	public byte[] getSalt() {
		return salt;
	}

	@Override
	public UUID getSubjecId() {
		return subjectId;
	}

	@Override
	public int hashCode() {
		return ByteSource.Util.bytes(hash).toBase64().hashCode();
	}

}
