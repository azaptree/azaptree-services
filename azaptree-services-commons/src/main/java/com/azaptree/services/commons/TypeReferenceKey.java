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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.util.Assert;

/**
 * 
 * References a generic type.
 * 
 * See: http://gafter.blogspot.com/2006/12/super-type-tokens.html
 * 
 * 
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

	protected TypeReferenceKey() {
		super();
		name = getRawType().getSimpleName();
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
	}

	public TypeReferenceKey(final String name, final boolean required) {
		super();
		this.name = name;
		this.required = required;
	}

	protected TypeReferenceKey(final String name, final boolean required, final T defaultValue) {
		super();
		Assert.hasText(name);
		this.name = name;
		this.required = required;
		this.defaultValue = defaultValue;
	}

	@Override
	public int compareTo(final TypeReference<T> o) {
		if (o == this) {
			return 0;
		}

		if (o instanceof TypeReferenceKey && o.getType().equals(getType())) {
			final String s1 = new StringBuilder(getRawType().getName()).append(getName()).toString();
			final Type type = o.getType();
			final Class<?> rawType = type instanceof Class<?> ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
			final String s2 = new StringBuilder(rawType.getName()).append(((TypeReferenceKey<T>) o).getName()).toString();
			return s1.compareTo(s2);
		}
		final Type type = o.getType();
		final Class<?> rawType = type instanceof Class<?> ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
		return getRawType().getName().compareTo(rawType.getName());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof TypeReferenceKey)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final TypeReferenceKey other = (TypeReferenceKey) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	public boolean isRequired() {
		return required;
	}

	@Override
	public String toString() {
		return String.format("TypeReference [_type=%s, required=%s, defaultValue=%s, name=%s]", type, required, defaultValue, name);
	}

}
