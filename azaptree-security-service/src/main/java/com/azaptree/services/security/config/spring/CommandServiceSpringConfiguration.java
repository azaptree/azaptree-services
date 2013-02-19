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
import org.springframework.context.annotation.Import;

import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.impl.CommandCatalogImpl;
import com.azaptree.services.security.commands.subjectRepository.AddSubjectCredential;
import com.azaptree.services.security.commands.subjectRepository.CreateSubject;
import com.azaptree.services.security.commands.subjectRepository.DeleteSubject;
import com.azaptree.services.security.commands.subjectRepository.DeleteSubjectCredential;
import com.azaptree.services.security.commands.subjectRepository.GetSubject;
import com.azaptree.services.security.config.CommandServiceConfiguration;
import com.azaptree.services.security.config.SecurityServiceConfiguration;

@Configuration
@Import(com.azaptree.services.command.config.spring.CommandServiceSpringConfiguration.class)
public class CommandServiceSpringConfiguration implements CommandServiceConfiguration {

	@Autowired
	private SecurityServiceConfiguration securityServiceConfiguration;

	@Bean(name = "AddSubjectCredential")
	public AddSubjectCredential addSubjectCredential() {
		return new AddSubjectCredential(securityServiceConfiguration.securityService().getHashServiceConfiguration());
	}

	@Bean(name = "GetSubject")
	public CreateSubject createSubject() {
		return new CreateSubject(securityServiceConfiguration.securityService().getHashServiceConfiguration());
	}

	@Bean(name = "DeleteSubject")
	public DeleteSubject deleteSubject() {
		return new DeleteSubject();
	}

	@Bean(name = "DeleteSubjectCredential")
	public DeleteSubjectCredential deleteSubjectCredential() {
		return new DeleteSubjectCredential();
	}

	@Bean(name = "GetSubject")
	public GetSubject getSubject() {
		return new GetSubject();
	}

	@Override
	@Bean
	public CommandCatalog securityServiceCommandCatalog() {
		return new CommandCatalogImpl(COMMAND_CATALOG_NAME,
		        getSubject(),
		        createSubject(),
		        deleteSubject(),
		        addSubjectCredential(),
		        deleteSubjectCredential());
	}
}
