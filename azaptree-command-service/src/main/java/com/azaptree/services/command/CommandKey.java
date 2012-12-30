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

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.chain.CatalogFactory;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.Assert;

import com.google.common.collect.ComparisonChain;

public class CommandKey implements Serializable, Comparable<CommandKey> {
	private static final long serialVersionUID = 1L;

	private final String catalogName;

	private final String commandName;

	public CommandKey(final String catalogName, final String commandName) {
		super();
		Assert.hasText(catalogName);
		Assert.hasText(commandName);
		this.catalogName = catalogName;
		this.commandName = commandName;
	}

	@Override
	public int compareTo(final CommandKey o) {
		if (o == null) {
			return 1;
		}
		return ComparisonChain.start()
		        .compare(catalogName, o.commandName)
		        .compare(commandName, commandName)
		        .result();

	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CommandKey)) {
			return false;
		}
		final CommandKey other = (CommandKey) obj;
		if (catalogName == null) {
			if (other.catalogName != null) {
				return false;
			}
		} else if (!catalogName.equals(other.catalogName)) {
			return false;
		}
		if (commandName == null) {
			if (other.commandName != null) {
				return false;
			}
		} else if (!commandName.equals(other.commandName)) {
			return false;
		}
		return true;
	}

	@NotBlank
	public String getCatalogName() {
		return catalogName;
	}

	@NotBlank
	public String getCommandName() {
		return commandName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(catalogName, commandName);
	}

	@Override
	public String toString() {
		return new StringBuilder(catalogName).append(CatalogFactory.DELIMITER).append(CatalogFactory.DELIMITER).toString();
	}

}
