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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.CommandChain;
import com.azaptree.services.command.CommandException;

public abstract class CommandChainSupport extends CommandSupport implements CommandChain {
	private final ChainBase chain = new ChainBase();

	private Command[] commands;

	@Override
	protected boolean executeCommand(final Context ctx) {
		try {
			return chain.execute(ctx);
		} catch (final Exception e) {
			throw new CommandException(e);
		}
	}

	@Override
	@Required
	public Command[] getCommands() {
		return commands;
	}

	@Required
	public void setCommands(final Command[] commands) {
		Assert.notEmpty(commands);
		this.commands = commands;
		for (final Command c : commands) {
			chain.addCommand(c);
		}
	}

}
