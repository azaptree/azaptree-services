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
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.CommandKey;
import com.azaptree.services.command.CommandService;

@Configuration
public interface CommandServiceConfiguration {
	public static final String COMMAND_CATALOG_NAME = "azaptree-security-service";

	public static final CommandKey CREATE_SUBJECT = new CommandKey(COMMAND_CATALOG_NAME, "CreateSubject");
	public static final CommandKey DELETE_SUBJECT = new CommandKey(COMMAND_CATALOG_NAME, "DeleteSubject");

	public static final CommandKey ADD_SUBJECT_CREDENTIAL = new CommandKey(COMMAND_CATALOG_NAME, "AddSubjectCredential");
	public static final CommandKey DELETE_SUBJECT_CREDENTIAL = new CommandKey(COMMAND_CATALOG_NAME, "DELETE_SUBJECT_CREDENTIAL");

	@Bean
	CommandService commandService();

	@Bean
	CommandCatalog securityServiceCommandCatalog();

}
