package test.com.azaptree.services.command;

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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.CommandChain;
import com.azaptree.services.command.impl.CommandCatalogImpl;
import com.azaptree.services.command.impl.CommandChainSupport;
import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.commons.TypeReferenceKey;
import com.google.common.base.Optional;

@ContextConfiguration(classes = { CommandCatalogTest.Config.class })
public class CommandCatalogTest extends AbstractTestNGSpringContextTests {
	@Configuration
	public static class Config {

		@Bean
		public Command logCommand() {
			return new CommandSupport() {

				@Override
				protected boolean executeCommand(final Context ctx) {
					log.info("msg: {}", ctx.get(MSG));
					return org.apache.commons.chain.Command.CONTINUE_PROCESSING;
				}

				@Override
				public Optional<TypeReferenceKey<?>[]> getInputKeys() {
					final TypeReferenceKey<?>[] keys = { MSG };
					return Optional.of(keys);
				}
			};
		}

		@Bean
		public CommandChain logTimeCommandChain() {
			return new CommandChainSupport(logCommand(), timeCommand()) {
			};
		}

		@Bean
		public CommandCatalog testCatalog() {
			return new CommandCatalogImpl("test-command-catalog",
			        timeCommand(),
			        logCommand(),
			        logTimeCommandChain());
		}

		@Bean
		public Command timeCommand() {
			return new CommandSupport() {

				@Override
				protected boolean executeCommand(final Context ctx) {
					ctx.put(TIME, System.currentTimeMillis());
					return org.apache.commons.chain.Command.CONTINUE_PROCESSING;
				}

				@Override
				public Optional<TypeReferenceKey<?>[]> getOutputKeys() {
					final TypeReferenceKey<?>[] keys = { TIME };
					return Optional.of(keys);
				}
			};
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final TypeReferenceKey<Long> TIME = new TypeReferenceKey<Long>("TIME", true) {
	};

	public static final TypeReferenceKey<String> MSG = new TypeReferenceKey<String>("MSG", true) {
	};

	@Resource(name = "testCatalog")
	private CommandCatalog catalog;

	@Test
	public void test_addCommand() {
		final String[] commandNames = catalog.getCommandNames();
		Assert.assertNotNull(commandNames);
		Assert.assertTrue(ArrayUtils.isNotEmpty(commandNames));
		int commandCount = commandNames.length;

		for (final String command : commandNames) {
			final Command c = (Command) catalog.getCommand(command);
			Assert.assertEquals(c.getName(), command);
		}

		catalog.addCommand("test_addCommand", new CommandSupport("test_addCommand") {

			@Override
			protected boolean executeCommand(final Context ctx) {
				return false;
			}
		});
		commandCount++;

		Assert.assertEquals(catalog.getCommandNames().length, commandCount);
		Assert.assertNotNull(catalog.getCommand("test_addCommand"));

		catalog.addCommand(new CommandSupport("test_addCommand2") {

			@Override
			protected boolean executeCommand(final Context ctx) {
				return false;
			}
		});
		commandCount++;

		Assert.assertEquals(catalog.getCommandNames().length, commandCount);
		Assert.assertNotNull(catalog.getCommand("test_addCommand"));
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void test_addCommand_dup() {
		final String commandName = UUID.randomUUID().toString();
		catalog.addCommand(new CommandSupport(commandName) {

			@Override
			protected boolean executeCommand(final Context ctx) {
				return false;
			}
		});

		catalog.addCommand(new CommandSupport(commandName) {

			@Override
			protected boolean executeCommand(final Context ctx) {
				return false;
			}
		});

	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void test_addCommand_dup2() {
		final String commandName = UUID.randomUUID().toString();
		catalog.addCommand(commandName, new CommandSupport(commandName) {

			@Override
			protected boolean executeCommand(final Context ctx) {
				return false;
			}
		});

		catalog.addCommand(commandName, new CommandSupport(commandName) {

			@Override
			protected boolean executeCommand(final Context ctx) {
				return false;
			}
		});
	}

	@SuppressWarnings("unused")
	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void test_createWithDupCommands() {
		final String commandName = UUID.randomUUID().toString();
		final Command command = new CommandSupport(commandName) {

			@Override
			protected boolean executeCommand(final Context ctx) {
				return false;
			}
		};
		new CommandCatalogImpl("cat", command, command);
	}

	@Test
	public void test_equals_hashCode() {
		final String commandName = UUID.randomUUID().toString();
		final Command command = new CommandSupport(commandName) {

			@Override
			protected boolean executeCommand(final Context ctx) {
				return false;
			}
		};
		final CommandCatalogImpl cat1 = new CommandCatalogImpl("cat", command);
		final CommandCatalogImpl cat2 = new CommandCatalogImpl("cat", command);

		Assert.assertTrue(cat1.equals(cat2));
		Assert.assertEquals(cat1, cat2);
		Assert.assertEquals(cat1.hashCode(), cat2.hashCode());
	}

	@Test
	public void test_getCommand() {
		final String[] commandNames = catalog.getCommandNames();
		Assert.assertNotNull(commandNames);
		Assert.assertTrue(ArrayUtils.isNotEmpty(commandNames));

		for (final String command : commandNames) {
			final Command c = (Command) catalog.getCommand(command);
			Assert.assertEquals(c.getName(), command);
		}
	}

	@Test
	public void test_getCommandNames() {
		final String[] commandNames = catalog.getCommandNames();
		Assert.assertNotNull(commandNames);
		Assert.assertTrue(ArrayUtils.isNotEmpty(commandNames));

		Assert.assertTrue(ArrayUtils.contains(commandNames, "timeCommand"));
		Assert.assertTrue(ArrayUtils.contains(commandNames, "logCommand"));

		final List<String> commandNamesList = new ArrayList<>(2);
		for (final Iterator<String> it = catalog.getNames(); it.hasNext();) {
			commandNamesList.add(it.next());
		}
		Assert.assertEquals(commandNamesList.size(), commandNames.length);

		for (final String command : commandNames) {
			Assert.assertTrue(commandNamesList.contains(command));
		}
	}

	@Test
	public void test_getName() {
		Assert.assertEquals(catalog.getName(), "test-command-catalog");
		log.info("catalog: {}", catalog);
	}
}
