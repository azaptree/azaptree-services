package com.azaptree.services.security.config.spring;

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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.azaptree.services.security.CredentialNames;
import com.azaptree.services.security.CredentialToByteSourceConverter;
import com.azaptree.services.security.SecurityCredentialsService;
import com.azaptree.services.security.SecurityService;
import com.azaptree.services.security.SessionManagementService;
import com.azaptree.services.security.SubjectRepositoryService;
import com.azaptree.services.security.config.SecurityServiceConfiguration;
import com.azaptree.services.security.credentialToByteSourceConverters.StringToByteSourceConverter;
import com.azaptree.services.security.dao.HashServiceConfigurationDAO;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.config.impl.HashServiceConfig;
import com.azaptree.services.security.impl.SecurityCredentialsServiceImpl;
import com.azaptree.services.security.impl.SecurityServiceImpl;
import com.azaptree.services.security.impl.SessionManagementServiceImpl;
import com.azaptree.services.security.impl.SubjectRepositoryServiceImpl;

@Configuration
public abstract class SecurityServiceSpringConfiguration implements SecurityServiceConfiguration {

	@Autowired
	private HashServiceConfigurationDAO hashServiceConfigurationDAO;

	protected Map<String, CredentialToByteSourceConverter<?>> credentialToByteSourceConverters() {
		final Map<String, CredentialToByteSourceConverter<?>> converters = new HashMap<>();
		converters.put(CredentialNames.PASSWORD.credentialName, stringToByteSourceConverter());
		converters.put(CredentialNames.FORGOT_PASSWORD_ANSWER_1.credentialName, stringToByteSourceConverter());
		return converters;
	}

	/**
	 * 
	 * @return the name of the HashServiceConfiguration to load from the database
	 */
	protected abstract String getHashServiceConfigurationName();

	/**
	 * Will try to lookup the HashServiceConfiguration using the name specified by {@link #getHashServiceConfigurationName()}
	 * 
	 * @return
	 */
	@Transactional
	protected HashServiceConfiguration hashServiceConfiguration() {
		Exception exception = null;
		for (int i = 0; i < 3; i++) {
			final HashServiceConfiguration config = hashServiceConfigurationDAO.findByName(getHashServiceConfigurationName());
			if (config != null) {
				return config;
			}

			try {
				return hashServiceConfigurationDAO.create(newHashServiceConfiguration());
			} catch (Exception e) {
				exception = e;
				try {
					Thread.sleep((long) (Math.random() * 1000));
				} catch (InterruptedException e2) {
					// ignore
				}
			}
		}

		throw new RuntimeException("hashServiceConfiguration() failed", exception);
	}

	/**
	 * Simply returns a new HashServiceConfig instance :
	 * 
	 * <code>
	 * return new HashServiceConfig(getHashServiceConfigurationName());
	 * </code>
	 * 
	 * Override this method if the application requires a different configuration
	 * 
	 * @return
	 */
	protected HashServiceConfiguration newHashServiceConfiguration() {
		return new HashServiceConfig(getHashServiceConfigurationName());
	}

	@Override
	@Bean
	public SecurityCredentialsService securityCredentialsService() {
		return new SecurityCredentialsServiceImpl(credentialToByteSourceConverters());
	}

	@Override
	@Bean
	public SecurityService securityService() {
		return new SecurityServiceImpl(hashServiceConfiguration());
	}

	@Override
	@Bean
	public SessionManagementService sessionManagementService() {
		return new SessionManagementServiceImpl();
	}

	@Bean
	public StringToByteSourceConverter stringToByteSourceConverter() {
		return new StringToByteSourceConverter();
	}

	@Override
	@Bean
	public SubjectRepositoryService subjectRepositoryService() {
		return new SubjectRepositoryServiceImpl();
	}

}
