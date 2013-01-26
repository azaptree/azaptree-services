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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.azaptree.services.domain.entity.VersionedEntity;
import com.azaptree.services.domain.entity.impl.DomainVersionedEntity;

public abstract class VersionedEntityRowMapperSupport<T extends VersionedEntity> implements RowMapper<T> {

	protected abstract T createEntity(final ResultSet rs, final int rowNum) throws SQLException;

	@Override
	public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		final T entity = createEntity(rs, rowNum);
		final DomainVersionedEntity domainEntity = (DomainVersionedEntity) entity;
		domainEntity.setEntityId((UUID) rs.getObject("entity_id"));
		domainEntity.setEntityVersion(rs.getLong("entity_version"));
		domainEntity.setEntityCreatedOn(rs.getTimestamp("entity_created_on").getTime());
		domainEntity.setCreatedBy((UUID) rs.getObject("entity_created_by"));
		domainEntity.setEntityUpdatedOn(rs.getTimestamp("entity_updated_on").getTime());
		domainEntity.setUpdatedBy((UUID) rs.getObject("entity_updated_by"));
		return mapRow(entity, rs, rowNum);
	}

	protected abstract T mapRow(T entity, ResultSet rs, int rowNum) throws SQLException;

}
