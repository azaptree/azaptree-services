package com.azaptree.services.domain.entity;

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

import java.util.UUID;

public interface EntityAuditLogRecord extends Entity {

	public enum AuditAction {
		CREATED(0),
		UPDATED(1),
		DELETED(2);

		public final int id;

		private AuditAction(final int id) {
			this.id = id;
		}

	}

	AuditAction getAuditAction();

	UUID getAuditedEntityId();

	/**
	 * Epoch time
	 * 
	 * @return
	 */
	long getEntityAuditlogRecordCreatedOn();

	String getEntityJson();

	/**
	 * Should map the the Entity Class
	 * 
	 * @return
	 */
	String getEntityType();
}
