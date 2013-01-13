package com.azaptree.services.command;

/*
 * #%L
 * AZAPTREE-COMMAND-SERVICE
 * %%
 * Copyright (C) 2012 AZAPTREE.COM
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

import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public interface CommandService {
	public static final Logger log = LoggerFactory.getLogger(CommandService.class);

	public static final Logger commandMetricLog = LoggerFactory.getLogger(CommandService.class.getName() + ".metrics");
	public static final Logger commandRequestLog = LoggerFactory.getLogger(CommandService.class.getName() + ".commandRequest");
	public static final Logger commandResponseLog = LoggerFactory.getLogger(CommandService.class.getName() + ".commandResponse");

	/**
	 * The CommandService will collect metrics each time the command is executed.
	 * 
	 * 
	 * @param catalogName
	 *            REQUIRED
	 * @param commandName
	 *            REQUIRED
	 * @param ctx
	 *            REQUIRED
	 * @throws CommandException
	 */
	void execute(CommandKey key, Context ctx) throws CommandException;

	Command getCommand(CommandKey key);

	CommandCatalog getCommandCatalog(String catalogName);

	String[] getCommandCatalogNames();

}
