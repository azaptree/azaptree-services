package com.azaptree.services.commons;

/*
 * #%L
 * AZAPTREE-SERVICES-COMMONS
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

import java.lang.reflect.ParameterizedType;

import org.springframework.util.Assert;

/**
 * 
 * References a generic type.
 * 
 * See: http://gafter.blogspot.com/2006/12/super-type-tokens.html
 * 
 * 
 * @author alfio
 * 
 * @param <T>
 */
public abstract class TypeReferenceKey<T> extends TypeReference<T> {

	private boolean required = false;
	private T defaultValue;
	private final String name;
	private final int hashCode;

	protected TypeReferenceKey() {
		super();
		name = getRawType().getSimpleName();
		this.hashCode = name.hashCode();
	}

	protected TypeReferenceKey(final boolean required) {
		this();
		this.required = required;
	}

	protected TypeReferenceKey(final boolean required, final T defaultValue) {
		super();
		this.required = required;
		this.defaultValue = defaultValue;
		name = getRawType().getSimpleName();
		this.hashCode = name.hashCode();
	}

	public TypeReferenceKey(final String name, final boolean required) {
		super();
		this.name = name;
		this.required = required;
		this.hashCode = name.hashCode();
	}

	protected TypeReferenceKey(final String name, final boolean required, final T defaultValue) {
		super();
		Assert.hasText(name);
		this.name = name;
		this.hashCode = name.hashCode();
		this.required = required;
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TypeReferenceKey)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final TypeReferenceKey other = (TypeReferenceKey) obj;
		return name.equals(other.name) && getRawType().equals(other.getRawType());
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public String getName() {
		return name;
	}

	@Override
	public Class<?> getRawType() {
		return type instanceof Class<?> ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public boolean isRequired() {
		return required;
	}

	@Override
	public String toString() {
		return String.format("TypeReferenceKey [type=%s, required=%s, defaultValue=%s, name=%s]", type, required, defaultValue, name);
	}

}
