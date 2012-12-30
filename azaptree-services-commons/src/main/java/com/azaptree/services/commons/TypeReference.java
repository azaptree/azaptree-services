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

/**
 * 
 * References a generic type.
 * 
 * See: http://gafter.blogspot.com/2006/12/super-type-tokens.html
 * 
 * USE CASE: this can be used to declare constants that contain
 * 
 * 
 * @author alfio
 * 
 * @param <T>
 */
public abstract class TypeReference<T> implements Comparable<TypeReference<T>> {

	protected final Type type;

	protected TypeReference() {
		final Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class<?>) {
			throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
		}
		type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public T cast(final Object object) {
		if (object == null) {
			return null;
		}
		return (T) object;
	}

	@Override
	public int compareTo(final TypeReference<T> o) {
		if (o == this) {
			return 0;
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
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TypeReference)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final TypeReference other = (TypeReference) obj;
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!getRawType().equals(other.getRawType())) {
			return false;
		}
		return true;
	}

	public Class<?> getRawType() {
		return type instanceof Class<?> ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
	}

	public Type getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return getRawType().getName().hashCode();
	}

	@Override
	public String toString() {
		return String.format("TypeReference [_type=%s]", type);
	}

}
