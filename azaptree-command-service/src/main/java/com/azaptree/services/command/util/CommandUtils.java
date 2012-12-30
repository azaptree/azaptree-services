package com.azaptree.services.command.util;

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

import com.azaptree.services.commons.TypeReferenceKey;

public abstract class CommandUtils {

	public static <T> T get(final Context ctx, final TypeReferenceKey<T> key) {
		if (ctx.containsKey(key.getName())) {
			return (T) ctx.get(key.getName());
		}

		return key.getDefaultValue();
	}

	@SuppressWarnings("unchecked")
	public static <T> T put(final Context ctx, final TypeReferenceKey<T> key, final T value) {
		return (T) ctx.put(key.getName(), value);
	}
}
