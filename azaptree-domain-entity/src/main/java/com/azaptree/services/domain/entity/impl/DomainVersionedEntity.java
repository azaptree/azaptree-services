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

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.VersionedEntity;
import com.azaptree.services.json.JsonUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Optional;

public class DomainVersionedEntity extends DomainEntity implements VersionedEntity {

	protected long entityCreatedOn, entityUpdatedOn;

	protected volatile long entityVersion;

	protected UUID createdByEntityId, updatedByEntityId;

	public DomainVersionedEntity() {
	}

	public DomainVersionedEntity(final VersionedEntity entity) {
		Assert.notNull(entity, "entity is required");
		this.setEntityId(entity.getEntityId());
		this.setEntityCreatedOn(entity.getEntityCreatedOn());
		if (entity.getCreatedByEntityId().isPresent()) {
			this.setCreatedBy(entity.getCreatedByEntityId().get());
		} else {
			this.setCreatedBy(null);
		}
		this.setEntityUpdatedOn(entity.getEntityUpdatedOn());
		if (entity.getUpdatedByEntityId().isPresent()) {
			this.setUpdatedBy(entity.getUpdatedByEntityId().get());
		} else {
			this.setUpdatedBy(null);
		}
		this.setEntityVersion(entity.getEntityVersion());
	}

	public DomainVersionedEntity(final InputStream json) throws IOException {
		super(json);
	}

	public DomainVersionedEntity(final String json) {
		super(json);
	}

	/**
	 * Call this method when the entity is updated to increment its version and update its lastupdatedByEntityId
	 * 
	 * @param lastupdatedByEntityId
	 */
	public void created(final UUID createdByEntityId) {
		if (this.entityId != null) {
			throw new IllegalStateException("This entity has already been created: entityId = " + entityId);
		}
		this.entityId = UUID.randomUUID();
		this.entityVersion = 1;
		this.createdByEntityId = createdByEntityId;
		this.updatedByEntityId = createdByEntityId;
		this.entityCreatedOn = System.currentTimeMillis();
		this.entityUpdatedOn = this.entityCreatedOn;
	}

	/**
	 * Call this method when the entity is updated to increment its version and update its lastupdatedByEntityId
	 * 
	 * @param lastupdatedByEntityId
	 */
	public void created() {
		if (this.entityId != null) {
			throw new IllegalStateException("This entity has already been created: entityId = " + entityId);
		}
		this.entityId = UUID.randomUUID();
		entityVersion = 1;
		this.entityCreatedOn = System.currentTimeMillis();
		this.entityUpdatedOn = this.entityCreatedOn;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DomainVersionedEntity other = (DomainVersionedEntity) obj;
		return Objects.equals(createdByEntityId, other.createdByEntityId) &&
		        Objects.equals(entityCreatedOn, other.entityCreatedOn) &&
		        Objects.equals(entityUpdatedOn, other.entityUpdatedOn) &&
		        Objects.equals(entityVersion, other.entityVersion) &&
		        Objects.equals(updatedByEntityId, other.updatedByEntityId);
	}

	@Override
	public Optional<UUID> getCreatedByEntityId() {
		if (createdByEntityId == null) {
			return Optional.absent();
		}
		return Optional.of(createdByEntityId);
	}

	@Override
	public long getEntityCreatedOn() {
		return entityCreatedOn;
	}

	@Override
	public long getEntityUpdatedOn() {
		return entityUpdatedOn;
	}

	@Override
	public long getEntityVersion() {
		return entityVersion;
	}

	@Override
	public Optional<UUID> getUpdatedByEntityId() {
		if (updatedByEntityId == null) {
			return Optional.absent();
		}
		return Optional.of(updatedByEntityId);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(entityId, entityVersion);
	}

	public void setCreatedBy(final UUID createdByEntityId) {
		this.createdByEntityId = createdByEntityId;
	}

	/**
	 * Knows how to parse the following JSON fields:
	 * 
	 * <ul>
	 * <li>entityId</li>
	 * <li>entityVersion</li>
	 * <li>entityCreatedOn</li>
	 * <li>createdByEntityId</li>
	 * <li>entityUpdatedOn</li>
	 * <li>updatedByEntityId</li>
	 * </ul>
	 * 
	 * For any other field, JSON parsing is delegated to init(JsonParser parser)
	 * 
	 */
	@SuppressWarnings("incomplete-switch")
	@Override
	public void init(InputStream json) throws IOException {
		try (final JsonParser parser = JsonUtils.createJsonParser(json)) {
			JsonToken tk;
			while ((tk = parser.nextToken()) != null) {
				switch (tk) {
				case FIELD_NAME:
					final String fieldName = parser.getCurrentName();
					if (fieldName.equals("entityId")) {
						setEntityId(UUID.fromString(parser.nextTextValue()));
					} else if (fieldName.equals("entityVersion")) {
						setEntityVersion(parser.nextLongValue(0));
					} else if (fieldName.equals("entityCreatedOn")) {
						setEntityCreatedOn(parser.nextLongValue(0));
					} else if (fieldName.equals("createdByEntityId")) {
						setCreatedBy(UUID.fromString(parser.nextTextValue()));
					} else if (fieldName.equals("entityUpdatedOn")) {
						setEntityUpdatedOn(parser.nextLongValue(0));
					} else if (fieldName.equals("updatedByEntityId")) {
						setUpdatedBy(UUID.fromString(parser.nextTextValue()));
					} else {
						init(parser);
					}
					break;
				}
			}
		}

	}

	/**
	 * Sub-classes should override this to parse sub-class specific fields.
	 * 
	 * This is invoked by init(InputStream), when an unknown field is encountered
	 * 
	 * @param parser
	 */
	protected void init(JsonParser parser) throws IOException {
		LoggerFactory.getLogger(getClass()).warn("init(JsonParser parser) invoked to handle field: {}", parser.getCurrentName());
	}

	public void setEntityCreatedOn(final long entityCreatedOn) {
		this.entityCreatedOn = entityCreatedOn;
	}

	public void setEntityUpdatedOn(final long entityLastupdatedOn) {
		entityUpdatedOn = entityLastupdatedOn;
	}

	public void setEntityVersion(final long entityVersion) {
		this.entityVersion = entityVersion;
	}

	public void setUpdatedBy(final UUID lastUpdatedByEntityId) {
		updatedByEntityId = lastUpdatedByEntityId;
	}

	/**
	 * Call this method when the entity is updated before it is persisted to increment its version and update its lastupdatedByEntityId
	 * 
	 * @param lastupdatedByEntityId
	 */
	public void updated(final UUID lastupdatedByEntityId) {
		if (this.entityId == null) {
			throw new IllegalStateException("The entity has not yet been created : entityId == null");
		}
		entityVersion++;
		updatedByEntityId = lastupdatedByEntityId;
		entityUpdatedOn = System.currentTimeMillis();
	}

	/**
	 * Call this method when the entity is updated before it is persisted to increment its version and update its lastupdatedByEntityId
	 * 
	 * @param lastupdatedByEntityId
	 */
	public void updated() {
		if (this.entityId == null) {
			throw new IllegalStateException("The entity has not yet been created : entityId == null");
		}
		entityVersion++;
		entityUpdatedOn = System.currentTimeMillis();
	}

}
