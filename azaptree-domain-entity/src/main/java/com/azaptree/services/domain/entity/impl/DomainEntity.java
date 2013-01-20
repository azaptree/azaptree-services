package com.azaptree.services.domain.entity.impl;

/*
 * #%L
 * AZAPTREE-DOMAIN-ENTITY
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.apache.commons.beanutils.BeanUtils;

import com.azaptree.services.domain.entity.Entity;
import com.azaptree.services.json.JsonUtils;

public class DomainEntity implements Entity {

	protected UUID entityId;

	public DomainEntity() {
	}

	public DomainEntity(final InputStream json) throws IOException {
		init(json);
	}

	public DomainEntity(final String json) {
		init(json);
	}

	@Override
	public UUID getEntityId() {
		return entityId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainEntity other = (DomainEntity) obj;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		return true;
	}

	@Override
	public void init(final InputStream json) throws IOException {
		final Object domainEntity = JsonUtils.parse(json, getClass());
		try {
			BeanUtils.copyProperties(this, domainEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return JsonUtils.serializePrettyPrint(this);
	}

	@Override
	public void init(final String json) {
		try {
			init(new ByteArrayInputStream(json.getBytes("UTF-8")));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setEntityId(final UUID entityId) {
		this.entityId = entityId;
	}

	@Override
	public String toJson() {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
		try {
			writeJson(bos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return bos.toString();
	}

	@Override
	public void writeJson(final OutputStream os) throws IOException {
		JsonUtils.serialize(os, this);
	}

}
