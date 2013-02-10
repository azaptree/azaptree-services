package com.azaptree.services.security.config;

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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azaptree.services.security.SecurityCredentialsService;
import com.azaptree.services.security.SecurityService;
import com.azaptree.services.security.SessionManagementService;
import com.azaptree.services.security.SubjectRepositoryService;

@Configuration
public interface SecurityServiceConfiguration {

	@Bean
	SecurityCredentialsService securityCredentialsService();

	@Bean
	SecurityService securityService();

	@Bean
	SessionManagementService sessionManagementService();

	@Bean
	SubjectRepositoryService subjectRepositoryService();

}
