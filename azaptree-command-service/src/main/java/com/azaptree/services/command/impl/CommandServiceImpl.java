package com.azaptree.services.command.impl;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.CommandException;
import com.azaptree.services.command.CommandKey;
import com.azaptree.services.command.CommandService;
import com.azaptree.services.command.CommandServiceJmxApi;
import com.azaptree.services.commons.validation.ValidationException;
import com.google.common.collect.ImmutableMap;

/**
 * 
 * 
 * @author Alfio Zappala
 * 
 */
@Service
@ManagedResource
public class CommandServiceImpl implements CommandService, CommandServiceJmxApi {

	@Autowired
	private List<CommandCatalog> catalogs;

	private Map<String, CommandCatalog> commandCatalogs;
	private Map<CommandKey, Command> commands;

	@Override
	public void execute(final CommandKey key, final Context ctx) throws CommandException {
		Assert.notNull(key, "key is required");
		Assert.notNull(ctx, "ctx is required");

		final org.apache.commons.chain.Command command = getCommand(key);
		final CommandExcecutionMetric metric = new CommandExcecutionMetric();
		try {
			command.execute(ctx);
			metric.succeeded();
		} catch (final CommandException | ValidationException | IllegalArgumentException e) {
			metric.failed(e);
			throw e;
		} catch (final Throwable t) {
			metric.failed(t);
			throw new CommandException(t);
		} finally {
			commandMetricLog.info("{} : {}", key, metric);
		}
	}

	private org.apache.commons.chain.Command getCommand(final CommandKey key) {
		Assert.notNull(key, "key is required");
		final org.apache.commons.chain.Command command = commands.get(key);
		if (command == null) {
			throw new IllegalArgumentException(String.format("Unknown command: %s -> %s", key.getCatalogName(), key.getCommandName()));
		}

		return command;
	}

	@Override
	public com.azaptree.services.command.Command getCommandCatalog(final CommandKey key) {
		return (com.azaptree.services.command.Command) commands.get(key);
	}

	@Override
	public CommandCatalog getCommandCatalog(final String catalogName) {
		Assert.hasText(catalogName);
		return commandCatalogs.get(catalogName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.azaptree.services.command.impl.CommandServiceJmxApi#getCommandCatalogNames()
	 */
	@ManagedAttribute
	@Override
	public String[] getCommandCatalogNames() {
		final Set<String> catalogNames = new TreeSet<>(commandCatalogs.keySet());
		return catalogNames.toArray(new String[catalogNames.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.azaptree.services.command.impl.CommandServiceJmxApi#getCommandNames(java.lang.String)
	 */
	@Override
	@ManagedOperation
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "catalogName", description = "Catalog name") })
	public String[] getCommandNames(final String catalogName) {
		Assert.hasText(catalogName, "catalogName is required");
		final CommandCatalog catalog = getCommandCatalog(catalogName);
		return catalog.getCommandNames();
	}

	@PostConstruct
	public void init() {
		Assert.notEmpty(catalogs, "No catalogs were found");

		final Map<String, CommandCatalog> catMap = new HashMap<>();
		final Map<CommandKey, Command> cmdMap = new HashMap<>();
		for (final CommandCatalog catalog : catalogs) {
			log.info("registering catalog : {}", catalog.getName());
			if (catMap.get(catalog.getName()) != null) {
				throw new IllegalArgumentException("Duplicate Catalog name found: " + catalog.getName());
			}
			catMap.put(catalog.getName(), catalog);

			for (final String commandName : catalog.getCommandNames()) {
				final Command command = catalog.getCommand(commandName);
				Assert.notNull(command);
				final CommandKey commandKey = new CommandKey(catalog.getName(), commandName);
				cmdMap.put(commandKey, command);
				log.info("registered CommandKey: {}", commandKey);
			}
		}
		catalogs = null; // no longer needed

		commandCatalogs = ImmutableMap.<String, CommandCatalog> builder().putAll(catMap).build();
		commands = ImmutableMap.<CommandKey, Command> builder().putAll(cmdMap).build();

	}
}
