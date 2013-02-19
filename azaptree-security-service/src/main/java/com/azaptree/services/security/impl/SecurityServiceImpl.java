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

import org.apache.shiro.crypto.hash.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.azaptree.services.security.AuthenticationException;
import com.azaptree.services.security.SecurityService;
import com.azaptree.services.security.SecurityServiceException;
import com.azaptree.services.security.SubjectAuthenticationToken;
import com.azaptree.services.security.dao.HashServiceConfigurationDAO;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;

public class SecurityServiceImpl implements SecurityService {

	private final HashServiceConfiguration hashServiceConfiguration;

	@Autowired
	private HashServiceConfigurationDAO hashServiceConfigurationDAO;

	public SecurityServiceImpl(final HashServiceConfiguration hashServiceConfiguration) {
		super();
		this.hashServiceConfiguration = hashServiceConfiguration;
	}

	@Override
	public HashService getHashService() throws SecurityServiceException {
		return hashServiceConfiguration.getHashService();
	}

	@Override
	public HashService getHashService(final String name) throws SecurityServiceException {
		Assert.hasText(name, "name is required");
		final HashServiceConfiguration config = hashServiceConfigurationDAO.findByName(name);
		if (config != null) {
			return config.getHashService();
		}
		return null;
	}

	@Override
	public HashService getHashService(final UUID hashServiceConfiguationId) throws SecurityServiceException {
		Assert.notNull(hashServiceConfiguationId, "hashServiceConfiguationId is required");
		final HashServiceConfiguration config = hashServiceConfigurationDAO.findById(hashServiceConfiguationId);
		if (config != null) {
			return config.getHashService();
		}
		return null;
	}

	@Override
	public HashServiceConfiguration getHashServiceConfiguration() throws SecurityServiceException {
		return hashServiceConfiguration;
	}

	@Override
	public UUID getHashServiceId(final String name) throws SecurityServiceException {
		Assert.hasText(name, "name is required");
		return hashServiceConfigurationDAO.lookupIdByName(name);
	}

	@Override
	public UUID login(final SubjectAuthenticationToken token) throws SecurityServiceException, AuthenticationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logout(final UUID sessionId) throws SecurityServiceException {
		// TODO Auto-generated method stub

	}

}
