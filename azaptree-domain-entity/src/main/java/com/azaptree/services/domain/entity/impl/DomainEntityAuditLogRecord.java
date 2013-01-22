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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.UUID;

import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.Entity;
import com.azaptree.services.domain.entity.EntityAuditLogRecord;

public class DomainEntityAuditLogRecord extends DomainEntity implements EntityAuditLogRecord {
	private UUID auditedEntityId;

	private AuditAction auditAction;

	private long entityAuditlogRecordCreatedOn;

	private String entityType;

	private String entityJson;

	public DomainEntityAuditLogRecord() {
	}

	public DomainEntityAuditLogRecord(final Entity entity, final AuditAction auditAction) {
		Assert.notNull(entity, "entity is required");
		Assert.notNull(auditAction, "auditAction is required");
		setEntityId(UUID.randomUUID());
		setEntityAuditlogRecordCreatedOn(System.currentTimeMillis());
		setAuditedEntityId(entity.getEntityId());
		setAuditAction(auditAction);
		setEntityType(entity.getClass().getName());
		setEntityJson(entity.toJson());
	}

	@Override
	public AuditAction getAuditAction() {
		return auditAction;
	}

	@Override
	public UUID getAuditedEntityId() {
		return auditedEntityId;
	}

	@Override
	public long getEntityAuditlogRecordCreatedOn() {
		return entityAuditlogRecordCreatedOn;
	}

	@Override
	public String getEntityJson() {
		return entityJson;
	}

	@Override
	public String getEntityType() {
		return entityType;
	}

	public void setAuditAction(final AuditAction auditAction) {
		this.auditAction = auditAction;
	}

	public void setAuditedEntityId(final UUID entityAuditlogRecordId) {
		auditedEntityId = entityAuditlogRecordId;
	}

	public void setEntityAuditlogRecordCreatedOn(final long entityAuditlogRecordCreatedOn) {
		this.entityAuditlogRecordCreatedOn = entityAuditlogRecordCreatedOn;
	}

	public void setEntityJson(final String entityJson) {
		this.entityJson = entityJson;
	}

	public void setEntityType(final String entityType) {
		this.entityType = entityType;
	}

}
