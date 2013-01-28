package com.azaptree.services.security.domain.config.impl;

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
import java.security.SecureRandom;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.util.ByteSource;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.impl.DomainVersionedEntity;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;

public class HashServiceConfig extends DomainVersionedEntity implements HashServiceConfiguration {
	private final String name;

	private final byte[] privateSalt;

	private final int hashIterations;

	private final String hashAlgorithmName;

	private final int secureRandomNumberGeneratorNextBytesSize;

	public HashServiceConfig(final HashServiceConfiguration config) {
		super(config);
		name = config.getName();
		privateSalt = config.getPrivateSalt();
		hashIterations = config.getHashIterations();
		hashAlgorithmName = config.getHashAlgorithmName();
		secureRandomNumberGeneratorNextBytesSize = config.getSecureRandomNumberGeneratorNextBytesSize();
		validate();
	}

	public HashServiceConfig(final String name) {
		Assert.hasText(name, "name is required");
		this.name = name;
		hashAlgorithmName = "SHA-256";
		final SecureRandomNumberGenerator rng = new SecureRandomNumberGenerator();
		privateSalt = rng.nextBytes(32).getBytes();
		hashIterations = 1024 * 128;
		secureRandomNumberGeneratorNextBytesSize = 32;
		validate();
	}

	public HashServiceConfig(final String name, final byte[] privateSalt, final int hashIterations, final String hashAlgorithmName,
	        final int secureRandomNumberGeneratorNextBytesSize) {
		this.name = name;
		this.privateSalt = privateSalt;
		this.hashIterations = hashIterations;
		this.hashAlgorithmName = hashAlgorithmName;
		this.secureRandomNumberGeneratorNextBytesSize = secureRandomNumberGeneratorNextBytesSize;
		validate();
	}

	@Override
	public HashService createHashService() {
		final DefaultHashService service = new DefaultHashService();
		service.setGeneratePublicSalt(true);
		service.setPrivateSalt(ByteSource.Util.bytes(privateSalt));
		service.setHashAlgorithmName(hashAlgorithmName);
		service.setHashIterations(hashIterations);

		final SecureRandomNumberGenerator rng = new SecureRandomNumberGenerator();
		rng.setDefaultNextBytesSize(secureRandomNumberGeneratorNextBytesSize);
		final SecureRandom random = new SecureRandom();
		final byte rngSeed[] = new byte[20];
		random.nextBytes(rngSeed);
		rng.setSeed(rngSeed);

		service.setRandomNumberGenerator(rng);
		return service;
	}

	@Override
	public String getHashAlgorithmName() {
		return hashAlgorithmName;
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
	public byte[] getPrivateSalt() {
		return privateSalt;
	}

	@Override
	public int getSecureRandomNumberGeneratorNextBytesSize() {
		return secureRandomNumberGeneratorNextBytesSize;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
		        .append("name", name)
		        .append("hashAlgorithmName", hashAlgorithmName)
		        .toString();
	}

	private void validate() {
		Assert.hasText(name, "name is required");
		Assert.isTrue(ArrayUtils.isNotEmpty(privateSalt), "privateSalt is required");
		Assert.hasText(hashAlgorithmName, "hashAlgorithmName is required");
		try {
			MessageDigest.getInstance(hashAlgorithmName);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Unknown algorithm : " + hashAlgorithmName, e);
		}
		Assert.isTrue(hashIterations > 0, "contraint failed: hashIterations > 0");
		Assert.isTrue(secureRandomNumberGeneratorNextBytesSize > 1, "contraint failed: secureRandomNumberGeneratorNextBytesSize > 1");
	}

}
