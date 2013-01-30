package com.azaptree.services.domain.entity.dao;

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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.VersionedEntity;

public abstract class JDBCVersionedEntityDAOSupport<T extends VersionedEntity> extends JDBCEntityDAOSupport<T> implements VersionedEntityDAO<T> {

	public JDBCVersionedEntityDAOSupport(final JdbcTemplate jdbc, String table) {
		super(jdbc, table);
	}

	@Override
	protected void initFieldColumnMappings() {
		super.initFieldColumnMappings();
		fieldColumnMappings.put("EntityVersion", "entity_version");
		fieldColumnMappings.put("EntityCreatedOn", "entity_created_on");
		fieldColumnMappings.put("CreatedByEntityId", "entity_created_by");
		fieldColumnMappings.put("EntityUpdatedOn", "entity_updated_on");
		fieldColumnMappings.put("UpdatedByEntityId", "entity_updated_by");
	}

	/**
	 * 
	 * @param entity
	 * 
	 * @throws ObjectNotFoundException
	 *             if the entity does not exist in the database
	 */
	protected void validateForUpdate(VersionedEntity entity) {
		Assert.notNull(entity, "entity is required");
		Assert.notNull(entity.getEntityId(), "entityId must not be null");
		Assert.notNull(entity.getEntityCreatedOn(), "entityCreatedOn must not be null");
		Assert.isTrue(entity.getEntityVersion() > 0, "entityVersion must be > 0");

		if (!exists(entity.getEntityId())) {
			throw new ObjectNotFoundException();
		}

	}

}
