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

import javax.annotation.Resource;

import org.apache.commons.chain.Context;
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
import com.azaptree.services.command.CommandContext;
import com.azaptree.services.command.impl.CommandCatalogImpl;
import com.azaptree.services.command.impl.CommandChainSupport;
import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.commons.TypeReferenceKey;
import com.google.common.base.Optional;

@ContextConfiguration(classes = { CommandChainTest.Config.class })
public class CommandChainTest extends AbstractTestNGSpringContextTests {
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
	public void testCommandChain() throws Exception {
		final CommandChain commandChain = (CommandChain) catalog.getCommand("logTimeCommandChain");
		final Command[] commands = commandChain.getCommands();
		Assert.assertEquals(commands.length, 2);

		final CommandContext ctx = new CommandContext();
		Assert.assertEquals(commandChain.execute(ctx), org.apache.commons.chain.Command.CONTINUE_PROCESSING);
	}

	@Test(expectedExceptions = { UnsupportedOperationException.class })
	public void test_addCommand() {
		final CommandChain commandChain = (CommandChain) catalog.getCommand("logTimeCommandChain");
		commandChain.addCommand(new CommandSupport("ABC") {

			@Override
			protected boolean executeCommand(Context ctx) {
				return Command.CONTINUE_PROCESSING;
			}
		});
	}
}
