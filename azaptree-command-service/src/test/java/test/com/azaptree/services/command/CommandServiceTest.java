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

import java.util.Arrays;

import javax.annotation.Resource;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.CommandChain;
import com.azaptree.services.command.CommandContext;
import com.azaptree.services.command.CommandException;
import com.azaptree.services.command.CommandKey;
import com.azaptree.services.command.CommandService;
import com.azaptree.services.command.CommandServiceJmxApi;
import com.azaptree.services.command.impl.CommandCatalogImpl;
import com.azaptree.services.command.impl.CommandChainSupport;
import com.azaptree.services.command.impl.CommandContextValidatorSupport;
import com.azaptree.services.command.impl.CommandServiceImpl;
import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.commons.TypeReferenceKey;
import com.azaptree.services.commons.validation.ValidationException;

@ContextConfiguration(classes = { CommandServiceTest.Config.class })
public class CommandServiceTest extends AbstractTestNGSpringContextTests {

	@Configuration
	public static class Config {

		@Bean
		public CommandService commandService() {
			return new CommandServiceImpl();
		}

		@Bean
		public Command badCommand() {
			return new CommandSupport() {

				@Override
				protected boolean executeCommand(final Context ctx) {
					throw new RuntimeException("ERROR");
				}
			};
		}

		@Bean
		public Command logCommand() {
			final CommandSupport command = new CommandSupport() {
				{
					inputKeys = new TypeReferenceKey<?>[] { MSG };
					outputKeys = new TypeReferenceKey<?>[] { RESP_MSG };
				}

				@Override
				protected boolean executeCommand(final Context ctx) {
					log.info("msg: {}", ctx.get(MSG));
					put(ctx, RESP_MSG, "RECEIVED MESSAGE: " + get(ctx, MSG));
					return org.apache.commons.chain.Command.CONTINUE_PROCESSING;
				}
			};

			command.setValidator(new CommandContextValidatorSupport() {

				@Override
				protected void checkInput(final Command command, final Context ctx) {
					// NO ADDITIONAL VALIDATION REQUIRED

				}

				@Override
				protected void checkOutput(final Command command, final Context ctx) {
					// NO ADDITIONAL VALIDATION REQUIRED
				}
			});

			return command;
		}

		@Bean
		public CommandChain logTimeCommandChain() {
			return new CommandChainSupport(logCommand(), timeCommand()) {
				// INTENTIALLY EMPTY
			};
		}

		@Bean
		public CommandCatalog testCatalog() {
			return new CommandCatalogImpl("test-command-catalog",
			        timeCommand(),
			        logCommand(),
			        logTimeCommandChain(),
			        timeCommandDoesNotSetOutputKeys(),
			        badCommand(),
			        new CommandChainSupport("logTimeCommandChain2", logCommand(), timeCommand()) {
				        // INTENTIALLY EMPTY
			        });
		}

		@Bean
		public CommandCatalog testCatalog2() {
			return new CommandCatalogImpl("test-command-catalog-2",
			        timeCommand(),
			        logCommand(),
			        logTimeCommandChain(),
			        timeCommandDoesNotSetOutputKeys());
		}

		@Bean
		public Command timeCommand() {
			final CommandSupport command = new CommandSupport() {
				{
					outputKeys = new TypeReferenceKey<?>[] { TIME };
				}

				@Override
				protected boolean executeCommand(final Context ctx) {
					put(ctx, TIME, System.currentTimeMillis());
					return org.apache.commons.chain.Command.CONTINUE_PROCESSING;
				}

			};

			command.setValidator(new CommandContextValidatorSupport() {

				@Override
				protected void checkInput(final Command command, final Context ctx) {
					// NO ADDITIONAL VALIDATION REQUIRED

				}

				@Override
				protected void checkOutput(final Command command, final Context ctx) {
					// NO ADDITIONAL VALIDATION REQUIRED
				}
			});

			return command;
		}

		@Bean
		public Command timeCommandDoesNotSetOutputKeys() {
			final CommandSupport command = new CommandSupport() {
				{
					outputKeys = new TypeReferenceKey<?>[] { TIME };
				}

				@Override
				protected boolean executeCommand(final Context ctx) {
					return org.apache.commons.chain.Command.CONTINUE_PROCESSING;
				}

			};

			command.setValidator(new CommandContextValidatorSupport() {

				@Override
				protected void checkInput(final Command command, final Context ctx) {
					// NO ADDITIONAL VALIDATION REQUIRED

				}

				@Override
				protected void checkOutput(final Command command, final Context ctx) {
					// NO ADDITIONAL VALIDATION REQUIRED
				}
			});

			return command;
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final TypeReferenceKey<Long> TIME = new TypeReferenceKey<Long>("TIME", true) {
		// INTENTIALLY EMPTY
	};

	public static final TypeReferenceKey<String> MSG = new TypeReferenceKey<String>("MSG", true) {
		// INTENTIALLY EMPTY
	};

	public static final TypeReferenceKey<String> RESP_MSG = new TypeReferenceKey<String>("RESP_MSG", true) {
		// INTENTIALLY EMPTY
	};

	@Autowired
	private CommandService commandService;

	@Resource(name = "logCommand")
	private Command logCommand;

	@Test(expectedExceptions = { CommandException.class })
	public void test_badCommand() {
		commandService.execute(new CommandKey("test-command-catalog", "badCommand"), new CommandContext());
	}

	@Test
	public void test_commandKey_comparesTo() {
		final CommandKey[] keys = { new CommandKey("z", "a"), new CommandKey("a", "a"), new CommandKey("a", "b") };
		Arrays.sort(keys);
		Assert.assertEquals(keys[0], new CommandKey("a", "a"));
		Assert.assertEquals(keys[1], new CommandKey("a", "b"));
		Assert.assertEquals(keys[2], new CommandKey("z", "a"));
	}

	@Test
	public void test_CommandServiceJmxApi() {
		final CommandServiceJmxApi jmxApi = (CommandServiceJmxApi) commandService;
		final String[] commandCatalogNames = jmxApi.getCommandCatalogNames();
		Assert.assertTrue(ArrayUtils.isNotEmpty(commandCatalogNames));
		final String[] commandCatalogNames2 = commandService.getCommandCatalogNames();

		Arrays.sort(commandCatalogNames);
		Arrays.sort(commandCatalogNames2);
		Assert.assertTrue(ArrayUtils.isEquals(commandCatalogNames, commandCatalogNames2));

		for (String catName : commandCatalogNames) {
			final String[] commandNames = jmxApi.getCommandNames(catName);
			final String[] commandNames2 = commandService.getCommandCatalog(catName).getCommandNames();

			Arrays.sort(commandNames);
			Arrays.sort(commandNames2);
			Assert.assertTrue(ArrayUtils.isEquals(commandNames, commandNames2));
		}
	}

	@Test
	public void test_CommandSupport_getKeys() {
		Assert.assertTrue(logCommand.getInputKeys().isPresent());
		Assert.assertTrue(logCommand.getOutputKeys().isPresent());

		Assert.assertEquals(logCommand.getInputKeys().get()[0], MSG);
		Assert.assertEquals(logCommand.getOutputKeys().get()[0], RESP_MSG);
	}

	@Test
	public void test_execute() {
		final CommandContext ctx = new CommandContext();
		ctx.put(MSG, "CIAO");
		Assert.assertTrue(ctx.containsKey(MSG));
		final CommandContext ctx2 = new CommandContext(ctx);
		Assert.assertTrue(ctx2.containsKey(MSG));
		final CommandKey logCommandKey = new CommandKey("test-command-catalog", "logCommand");
		commandService.execute(logCommandKey, ctx);
		Assert.assertNotNull(ctx.get(RESP_MSG));

		final CommandKey timeCommandKey = new CommandKey("test-command-catalog", "timeCommand");
		Assert.assertEquals(ctx.remove(MSG), "CIAO");
		commandService.execute(timeCommandKey, ctx);
		Assert.assertNotNull(ctx.get(TIME));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void test_execute_invalidCatalogName() {
		final CommandContext ctx = new CommandContext();
		ctx.put(MSG, "CIAO");
		final CommandKey logCommandKey = new CommandKey("invalid-catalog-name", "logCommand");
		commandService.execute(logCommandKey, ctx);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void test_execute_invalidCommandName() {
		final CommandContext ctx = new CommandContext();
		ctx.put(MSG, "CIAO");
		final CommandKey logCommandKey = new CommandKey("test-command-catalog", "invalid-command-name");
		commandService.execute(logCommandKey, ctx);
	}

	@Test(expectedExceptions = ValidationException.class)
	public void test_execute_missingInputKey() {
		final CommandContext ctx = new CommandContext();
		final CommandKey logCommandKey = new CommandKey("test-command-catalog", "logCommand");
		commandService.execute(logCommandKey, ctx);
	}

	@Test(expectedExceptions = ValidationException.class)
	public void test_execute_missingOutputKey() {
		final CommandContext ctx = new CommandContext();
		final CommandKey logCommandKey = new CommandKey("test-command-catalog", "timeCommandDoesNotSetOutputKeys");
		commandService.execute(logCommandKey, ctx);
	}

	@Test
	public void test_getCommandCatalog() {
		final String[] catalogNames = commandService.getCommandCatalogNames();
		Assert.assertTrue(ArrayUtils.isNotEmpty(catalogNames));
		Assert.assertEquals(catalogNames.length, 2);

		final String[] expectedCatalogNames = { "test-command-catalog", "test-command-catalog-2" };
		for (final String name : catalogNames) {
			log.info("test_getCommandCatalog(): catalog name = {}", name);
			Assert.assertTrue(ArrayUtils.contains(expectedCatalogNames, name));
			final CommandCatalog catalog = commandService.getCommandCatalog(name);
			Assert.assertNotNull(catalog);
			Assert.assertEquals(catalog.getName(), name);
		}
	}
}
