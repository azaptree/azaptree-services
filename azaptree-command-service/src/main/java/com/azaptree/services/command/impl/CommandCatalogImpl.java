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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.CommandCatalog;
import com.google.common.collect.ImmutableMap;

@Component
public class CommandCatalogImpl implements CommandCatalog, BeanNameAware {
	private String name;
	private String description;

	private Map<String, org.apache.commons.chain.Command> commands;

	public CommandCatalogImpl(final String name, final String description, final Map<String, org.apache.commons.chain.Command> commands) {
		Assert.hasText(name, "name is required");
		Assert.hasText(description, "description is required");
		this.name = name;
		this.description = description;
		this.commands = ImmutableMap.<String, org.apache.commons.chain.Command> builder().putAll(commands).build();
	}

	public void addCommand(final Command command) {
		Assert.notNull(command);
		if (getCommand(command.getName()) != null) {
			throw new IllegalArgumentException(String.format("Command names must be unique within a catalog: %s", command.getName()));
		}
		addCommand(command.getName(), command);
	}

	@Override
	public synchronized void addCommand(final String name, final org.apache.commons.chain.Command command) {
		Assert.hasText(name);
		Assert.notNull(command);
		if (getCommand(name) != null) {
			throw new IllegalArgumentException(String.format("Command names must be unique within a catalog: %s", name));
		}
		this.commands = ImmutableMap.<String, org.apache.commons.chain.Command> builder().putAll(commands).put(name, command).build();
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
		return commands.get(name);
	}

	@Override
	public String[] getCommandNames() {
		final List<String> names = new ArrayList<>(commands.keySet());
		Collections.sort(names);
		return names.toArray(new String[names.size()]);
	}

	@Override
	public String getDescription() {
		if (StringUtils.isBlank(description)) {
			return getName();
		}

		return String.format("%s\n%%s", description, toString());
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
	public void setBeanName(final String name) {
		Assert.hasText(name);
		this.name = name;
	}

	@Override
	public String toString() {
		final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
		sb.append("name", name);
		sb.append("description", description);
		return sb.toString();
	}

}
