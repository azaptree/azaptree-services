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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azaptree.services.security.config.DaoConfiguration;
import com.azaptree.services.security.dao.HashServiceConfigurationDAO;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SessionAttributeDAO;
import com.azaptree.services.security.dao.SessionDAO;
import com.azaptree.services.security.dao.SubjectDAO;

@Configuration
public class DaoSpringConfiguration implements DaoConfiguration {
	@Autowired
	private DatabaseSpringConfiguration databaseSpringConfiguration;

	@Override
	@Bean
	public HashedCredentialDAO hashedCredentialDAO() {
		return new HashedCredentialDAO(databaseSpringConfiguration.securityServiceJdbcTemplate());
	}

	@Override
	@Bean
	public HashServiceConfigurationDAO hashServiceConfigurationDAO() {
		return new HashServiceConfigurationDAO(databaseSpringConfiguration.securityServiceJdbcTemplate());
	}

	@Override
	@Bean
	public SessionAttributeDAO sessionAttributeDAO() {
		return new SessionAttributeDAO(databaseSpringConfiguration.securityServiceJdbcTemplate());
	}

	@Override
	@Bean
	public SessionDAO sessionDAO() {
		return new SessionDAO(databaseSpringConfiguration.securityServiceJdbcTemplate());
	}

	@Override
	@Bean
	public SubjectDAO subjectDAO() {
		return new SubjectDAO(databaseSpringConfiguration.securityServiceJdbcTemplate());
	}

}
