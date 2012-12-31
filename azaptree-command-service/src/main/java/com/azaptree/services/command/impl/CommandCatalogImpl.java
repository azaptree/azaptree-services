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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.CommandCatalog;
import com.google.common.collect.ImmutableMap;

@Component
public class CommandCatalogImpl implements CommandCatalog {
	private final String name;

	private Map<String, org.apache.commons.chain.Command> commands;

	public CommandCatalogImpl(final String name, final Command... commands) {
		Assert.hasText(name, "name is required");
		Assert.notEmpty(commands, "commands are required");
		checkCommandNamesAreUnique(commands);

		this.name = name;

		final ImmutableMap.Builder<String, org.apache.commons.chain.Command> mapBuilder = ImmutableMap.<String, org.apache.commons.chain.Command> builder();
		for (final Command command : commands) {
			Assert.hasText(command.getName(), "command.name is required");
			mapBuilder.put(command.getName(), command);
		}
		this.commands = mapBuilder.build();
	}

	@Override
	public synchronized void addCommand(final Command command) {
		Assert.notNull(command, "command is required");
		Assert.hasText(command.getName(), "command.name is required");
		if (getCommand(command.getName()) != null) {
			throw new IllegalArgumentException(String.format("Command names must be unique within a catalog: %s", command.getName()));
		}
		commands = ImmutableMap.<String, org.apache.commons.chain.Command> builder().putAll(commands).put(command.getName(), command).build();
	}

	@Override
	public synchronized void addCommand(final String name, final org.apache.commons.chain.Command command) {
		Assert.hasText(name, "name is required");
		Assert.notNull(command, "command is required");
		if (getCommand(name) != null) {
			throw new IllegalArgumentException(String.format("Command names must be unique within a catalog: %s", name));
		}
		commands = ImmutableMap.<String, org.apache.commons.chain.Command> builder().putAll(commands).put(name, command).build();
	}

	private void checkCommandNamesAreUnique(final Command[] commands) {
		final Set<String> names = new HashSet<>(commands.length);
		for (final Command command : commands) {
			if (!names.add(command.getName())) {
				throw new IllegalArgumentException(String.format("Command names must be unique within a catalog: %s", command.getName()));
			}
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CommandCatalogImpl)) {
			return false;
		}
		final CommandCatalogImpl other = (CommandCatalogImpl) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public org.apache.commons.chain.Command getCommand(final String name) {
		Assert.hasText(name, "name is required");
		return commands.get(name);
	}

	@Override
	public String[] getCommandNames() {
		final List<String> names = new ArrayList<>(commands.keySet());
		Collections.sort(names);
		return names.toArray(new String[names.size()]);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Iterator getNames() {
		return commands.keySet().iterator();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public String toString() {
		final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
		sb.append("name", name);
		sb.append("commandNames", Arrays.toString(getCommandNames()));
		return sb.toString();
	}

}
