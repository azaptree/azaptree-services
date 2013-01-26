package com.azaptree.services.security.dao;

/*
 * #%L
 * AZAPTREE SECURITY SERVICE
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
import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.dao.DAOException;
import com.azaptree.services.domain.entity.dao.JDBCVersionedEntityDAOSupport;
import com.azaptree.services.domain.entity.dao.ObjectNotFoundException;
import com.azaptree.services.domain.entity.dao.StaleObjectException;
import com.azaptree.services.domain.entity.dao.VersionedEntityRowMapperSupport;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.impl.SubjectImpl;
import com.google.common.base.Optional;

@Repository
public class SubjectDAO extends JDBCVersionedEntityDAOSupport<Subject> {

	private final RowMapper<Subject> rowMapper = new VersionedEntityRowMapperSupport<Subject>() {

		@Override
		protected Subject createEntity(final ResultSet rs, int rowNum) {
			return new SubjectImpl();
		}

		@Override
		protected Subject mapRow(final Subject entity, final ResultSet rs, final int rowNum) {
			return entity;
		}

	};

	public SubjectDAO(final JdbcTemplate jdbc) {
		super(jdbc, "t_subject");
	}

	@Override
	public Subject create(final Subject entity) {
		Assert.notNull(entity, "entity is required");

		final SubjectImpl subject = new SubjectImpl();
		final Optional<UUID> createdBy = entity.getCreatedByEntityId();

		subject.created();
		final String sql = "insert into t_subject (entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by) values (?,?,?,?,?,?)";

		final Optional<UUID> updatedBy = subject.getUpdatedByEntityId();
		jdbc.update(sql,
		        subject.getEntityId(),
		        subject.getEntityVersion(),
		        new Timestamp(subject.getEntityCreatedOn()),
		        createdBy.isPresent() ? createdBy.get() : null,
		        new Timestamp(subject.getEntityUpdatedOn()),
		        updatedBy.isPresent() ? updatedBy.get() : null);
		return subject;
	}

	@Override
	public Subject create(final Subject entity, final UUID createdBy) {
		Assert.notNull(entity, "entity is required");
		Assert.notNull(createdBy, "createdBy is required");
		final SubjectImpl subject = new SubjectImpl();

		subject.created(createdBy);
		final String sql = "insert into t_subject (entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by) values (?,?,?,?,?,?)";

		final Optional<UUID> updatedBy = subject.getUpdatedByEntityId();
		jdbc.update(sql,
		        subject.getEntityId(),
		        subject.getEntityVersion(),
		        new Timestamp(subject.getEntityCreatedOn()),
		        createdBy,
		        new Timestamp(subject.getEntityUpdatedOn()),
		        updatedBy.isPresent() ? updatedBy.get() : null);
		return subject;
	}

	@Override
	protected RowMapper<Subject> getRowMapper() {
		return rowMapper;
	}

	@Override
	public Subject update(final Subject entity) {
		validateForUpdate(entity);

		final String sql = "update t_subject set entity_version = ?, entity_updated_on = ?,entity_updated_by = ? where entity_id = ? and entity_version = ?";
		final SubjectImpl updatedSubject = new SubjectImpl(entity);
		updatedSubject.updated();
		final Optional<UUID> optionalUpdatedBy = updatedSubject.getUpdatedByEntityId();
		final UUID updatedById = optionalUpdatedBy.isPresent() ? optionalUpdatedBy.get() : null;
		final int updateCount = jdbc.update(sql,
		        updatedSubject.getEntityVersion(),
		        new Timestamp(updatedSubject.getEntityUpdatedOn()),
		        updatedById,
		        updatedSubject.getEntityId(), entity.getEntityVersion());
		if (updateCount == 0) {
			throw new StaleObjectException();
		}

		return updatedSubject;
	}

	@Override
	public Subject update(final Subject entity, final UUID updatedBy) throws DAOException, StaleObjectException, ObjectNotFoundException {
		validateForUpdate(entity);

		final String sql = "update t_subject set entity_version = ?, entity_updated_on = ?,entity_updated_by = ? where entity_id = ? and entity_version = ?";
		final SubjectImpl updatedSubject = new SubjectImpl(entity);
		updatedSubject.updated(updatedBy);
		final int updateCount = jdbc.update(sql,
		        updatedSubject.getEntityVersion(),
		        new Timestamp(updatedSubject.getEntityUpdatedOn()),
		        updatedBy,
		        updatedSubject.getEntityId(), entity.getEntityVersion());
		if (updateCount == 0) {
			throw new StaleObjectException();
		}

		return updatedSubject;
	}
}
