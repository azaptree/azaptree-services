package test.com.azaptree.services.command;

/*
 * #%L
 * AZAPTREE-COMMAND-SERVICE
 * %%
 * Copyright (C) 2012 - 2013 AZAPTREE.COM
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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.command.CommandContext;
import com.azaptree.services.commons.TypeReferenceKey;

public class CommandContextTest {
	public static final TypeReferenceKey<Long> TIME = new TypeReferenceKey<Long>("TIME", true) {
		// INTENTIALLY EMPTY
	};

	public static final TypeReferenceKey<String> MSG = new TypeReferenceKey<String>("MSG", true) {
		// INTENTIALLY EMPTY
	};

	public static final TypeReferenceKey<String> RESP_MSG = new TypeReferenceKey<String>("RESP_MSG", true) {
		// INTENTIALLY EMPTY
	};

	@Test
	public void test_put_containsKey_get() {
		final CommandContext ctx = new CommandContext();
		final String value = "CIAO";
		ctx.put(MSG, value);
		Assert.assertTrue(ctx.containsKey(MSG));
		Assert.assertEquals(ctx.get(MSG), value);

		String removedValue = ctx.remove(MSG);
		Assert.assertEquals(removedValue, value);
	}

	@Test
	public void test_put_containsKey_get_2() {
		final CommandContext ctx = new CommandContext();
		final String value = "CIAO";
		final TypeReferenceKey<?> key = MSG;
		ctx.put(key, value);
		Assert.assertTrue(ctx.containsKey(key));
		Assert.assertEquals(ctx.get(key), value);

		Object removedValue = ctx.remove(key);
		Assert.assertEquals(removedValue, value);
	}

	@Test
	public void test_putAll() {
		final CommandContext ctx = new CommandContext();
		final String value = "CIAO";
		final TypeReferenceKey<?> key = MSG;
		ctx.put(key, value);
		Assert.assertTrue(ctx.containsKey(key));
		Assert.assertEquals(ctx.get(key), value);

		final CommandContext ctx2 = new CommandContext(ctx);
		Assert.assertEquals(ctx.get(key), ctx2.get(key));
	}

}
