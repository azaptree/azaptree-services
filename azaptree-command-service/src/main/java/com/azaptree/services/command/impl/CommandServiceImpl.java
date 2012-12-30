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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.CommandException;
import com.azaptree.services.command.CommandKey;
import com.azaptree.services.command.CommandService;
import com.azaptree.services.command.CommandServiceJmxApi;
import com.azaptree.services.commons.validation.ValidationException;

/**
 * 
 * 
 * @author Alfio Zappala
 * 
 */
@Component
@ManagedResource
public class CommandServiceImpl implements CommandService, CommandServiceJmxApi {

	@Autowired
	private List<CommandCatalog> catalogs;

	private CatalogFactory catalogFactory;

	@PreDestroy
	public void destroy() {
		CatalogFactory.clear();
	}

	@Override
	public void execute(final CommandKey key, final Context ctx) throws CommandException {
		Assert.notNull(key, "key is required");
		Assert.notNull(ctx, "ctx is required");

		final org.apache.commons.chain.Command command = getCommand(key);
		final CommandExcecutionMetricImpl metric = new CommandExcecutionMetricImpl();
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
		final CommandCatalog catalog = getCommandCatalog(key.getCatalogName());
		if (catalog == null) {
			throw new IllegalArgumentException(String.format("Unknown catalog: %s", key.getCatalogName()));
		}
		final org.apache.commons.chain.Command command = catalog.getCommand(key.getCommandName());
		if (command == null) {
			throw new IllegalArgumentException(String.format("Unknown command: %s -> %s", key.getCatalogName(), key.getCommandName()));
		}

		return command;
	}

	@Override
	public CommandCatalog getCommandCatalog(final String catalogName) {
		Assert.hasText(catalogName);
		return (CommandCatalog) catalogFactory.getCatalog(catalogName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.azaptree.services.command.impl.CommandServiceJmxApi#getCommandCatalogNames()
	 */
	@ManagedAttribute
	@Override
	public String[] getCommandCatalogNames() {
		final Set<String> catalogNames = new TreeSet<>();
		for (final Iterator<String> it = catalogFactory.getNames(); it.hasNext();) {
			catalogNames.add(it.next());
		}
		return catalogNames.toArray(new String[catalogNames.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.azaptree.services.command.impl.CommandServiceJmxApi#getCommandKeys()
	 */
	@Override
	@ManagedAttribute
	public String[] getCommandKeys() {
		final TreeSet<String> commandKeys = new TreeSet<>();
		for (final Iterator<String> catalogNames = catalogFactory.getNames(); catalogNames.hasNext();) {
			final String catalogName = catalogNames.next();
			final Catalog catalog = catalogFactory.getCatalog(catalogName);
			for (final Iterator<String> commandNames = catalog.getNames(); commandNames.hasNext();) {
				final String commandName = commandNames.next();
				commandKeys.add(String.format("%s%s%s", catalogName, CatalogFactory.DELIMITER, commandName));
			}
		}

		return commandKeys.toArray(new String[commandKeys.size()]);
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

		catalogFactory = CatalogFactory.getInstance();
		for (final CommandCatalog catalog : catalogs) {
			log.info("registering catalog : {}", catalog.getName());
			if (catalogFactory.getCatalog(catalog.getName()) != null) {
				throw new IllegalArgumentException("Duplicate Catalog name found: " + catalog.getName());
			}
			catalogFactory.addCatalog(catalog.getName(), catalog);
		}

		catalogs = null; // no longer needed
	}
}
