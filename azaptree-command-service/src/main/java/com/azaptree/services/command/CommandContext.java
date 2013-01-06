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

import java.util.Map;

import org.apache.commons.chain.impl.ContextBase;
import org.springframework.util.Assert;

import com.azaptree.services.commons.TypeReferenceKey;

public class CommandContext extends ContextBase {

	private static final long serialVersionUID = 1L;

	public CommandContext() {
	}

	public CommandContext(@SuppressWarnings("rawtypes") final Map map) {
		super(map);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean containsKey(final Object key) {
		if (key instanceof TypeReferenceKey) {
			return super.containsKey(((TypeReferenceKey) key).getName());
		}
		return super.containsKey(key);
	}

	public <T> boolean containsKey(final TypeReferenceKey<T> key) {
		Assert.notNull(key, "key is required");
		return super.containsKey(key.getName());
	}

	/**
	 * If there is no such key in the context, then return the default value.
	 * 
	 * @param key
	 * @return
	 */
	public <T> T get(final TypeReferenceKey<T> key) {
		Assert.notNull(key, "key is required");
		final T t = (T) super.get(key.getName());
		return t != null ? t : key.getDefaultValue();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object put(final Object key, final Object value) {
		if (key instanceof TypeReferenceKey) {
			return super.put(((TypeReferenceKey) key).getName(), value);
		}
		return super.put(key, value);
	}

	/**
	 * 
	 * @param key
	 *            REQUIRED
	 * @param value
	 * 
	 */
	public <T> T put(final TypeReferenceKey<T> key, final T value) {
		Assert.notNull(key, "key is required");
		Assert.notNull(value, "value is required");
		return (T) super.put(key.getName(), value);
	}

	public <T> T remove(final TypeReferenceKey<T> key) {
		Assert.notNull(key, "key is required");
		return (T) super.remove(key.getName());
	}
}
