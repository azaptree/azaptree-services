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
import java.sql.SQLException;
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
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.Subject.Status;
import com.azaptree.services.security.domain.impl.SubjectImpl;
import com.google.common.base.Optional;

@Repository
public class SubjectDAO extends JDBCVersionedEntityDAOSupport<Subject> implements SubjectRepository {

	private final RowMapper<Subject> rowMapper = new VersionedEntityRowMapperSupport<Subject>() {

		@Override
		protected Subject createEntity(final ResultSet rs, final int rowNum) {
			return new SubjectImpl();
		}

		@Override
		protected Subject mapRow(final Subject entity, final ResultSet rs, final int rowNum) throws SQLException {
			final SubjectImpl subject = (SubjectImpl) entity;
			subject.setMaxSessions(rs.getInt("max_sessions"));
			subject.setStatus(Status.getStatus(rs.getInt("status")));
			subject.setStatusTimestamp(rs.getTimestamp("status_timestamp").getTime());
			subject.setConsecutiveAuthenticationFailedCount(rs.getInt("consec_auth_failed_count"));
			final Timestamp lastTimeAutenticationFailed = rs.getTimestamp("last_auth_failed_ts");
			if (lastTimeAutenticationFailed != null) {
				subject.setLastTimeAuthenticationFailed(lastTimeAutenticationFailed.getTime());
			}
			return entity;
		}

	};

	public SubjectDAO(final JdbcTemplate jdbc) {
		super(jdbc, "t_subject");
	}

	@Override
	public Subject create(final Subject entity) {
		Assert.notNull(entity, "entity is required");

		final SubjectImpl subject = new SubjectImpl(entity);
		subject.created();

		final String sql = "insert into t_subject (entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by,max_sessions,status,status_timestamp,consec_auth_failed_count,last_auth_failed_ts) values (?,?,?,?,?,?,?,?,?,?,?)";

		final Optional<UUID> createdBy = entity.getCreatedByEntityId();
		final UUID createdByUUID = createdBy.isPresent() ? createdBy.get() : null;
		jdbc.update(sql,
		        subject.getEntityId(),
		        subject.getEntityVersion(),
		        new Timestamp(subject.getEntityCreatedOn()),
		        createdByUUID,
		        new Timestamp(subject.getEntityUpdatedOn()),
		        createdByUUID,
		        subject.getMaxSessions(),
		        subject.getStatus().code,
		        new Timestamp(subject.getStatusTimestamp()),
		        subject.getConsecutiveAuthenticationFailedCount(),
		        subject.getLastTimeAuthenticationFailed() > 0 ? new Timestamp(subject.getLastTimeAuthenticationFailed()) : null);
		return subject;
	}

	@Override
	public Subject create(final Subject entity, final UUID createdBy) {
		Assert.notNull(entity, "entity is required");
		Assert.notNull(createdBy, "createdBy is required");
		final SubjectImpl subject = new SubjectImpl(entity);

		subject.created(createdBy);
		final String sql = "insert into t_subject (entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by,max_sessions,status,status_timestamp,consec_auth_failed_count,last_auth_failed_ts) values (?,?,?,?,?,?,?,?,?,?,?)";

		jdbc.update(sql,
		        subject.getEntityId(),
		        subject.getEntityVersion(),
		        new Timestamp(subject.getEntityCreatedOn()),
		        createdBy,
		        new Timestamp(subject.getEntityUpdatedOn()),
		        createdBy,
		        subject.getMaxSessions(),
		        subject.getStatus().code,
		        new Timestamp(subject.getStatusTimestamp()),
		        subject.getConsecutiveAuthenticationFailedCount(),
		        subject.getLastTimeAuthenticationFailed() > 0 ? new Timestamp(subject.getLastTimeAuthenticationFailed()) : null);
		return subject;
	}

	@Override
	protected RowMapper<Subject> getRowMapper() {
		return rowMapper;
	}

	@Override
	protected void initFieldColumnMappings() {
		super.initFieldColumnMappings();
		fieldColumnMappings.put("MaxSessions", "max_sessions");
		fieldColumnMappings.put("Status", "status");
		fieldColumnMappings.put("StatusTimestamp", "status_timestamp");
		fieldColumnMappings.put("ConsecutiveAuthenticationFailedCount", "consec_auth_failed_count");
		fieldColumnMappings.put("LastTimeAuthenticationFailed", "last_auth_failed_ts");
	}

	@Override
	public void touch(final UUID subjectId) throws UnknownSubjectException {
		Assert.notNull(subjectId, "subjectId is required");
		final Subject subject = findById(subjectId);
		if (subject == null) {
			throw new ObjectNotFoundException();
		}

		final String sql = "update t_subject set entity_version=(entity_version+1), entity_updated_on=?,entity_updated_by=? where entity_id=? and entity_version=?";

		final int updateCount = jdbc.update(sql,
		        new Timestamp(System.currentTimeMillis()),
		        null,
		        subjectId,
		        subject.getEntityVersion());
		if (updateCount == 0) {
			if (exists(subjectId)) {
				throw new StaleObjectException();
			}

			throw new ObjectNotFoundException();
		}

	}

	@Override
	public void touch(final UUID subjectId, final UUID updatedBySubjectId) throws UnknownSubjectException {
		Assert.notNull(subjectId, "subjectId is required");
		Assert.notNull(updatedBySubjectId, "updatedBySubjectId not null");
		final Subject subject = findById(subjectId);
		if (subject == null) {
			throw new ObjectNotFoundException();
		}

		final String sql = "update t_subject set entity_version=(entity_version+1), entity_updated_on=?,entity_updated_by=? where entity_id=? and entity_version=?";

		final int updateCount = jdbc.update(sql,
		        new Timestamp(System.currentTimeMillis()),
		        updatedBySubjectId,
		        subjectId,
		        subject.getEntityVersion());
		if (updateCount == 0) {
			if (exists(subjectId)) {
				throw new StaleObjectException();
			}

			throw new ObjectNotFoundException();
		}
	}

	@Override
	public Subject update(final Subject entity) {
		validateForUpdate(entity);

		final String sql = "update t_subject set entity_version=?, entity_updated_on=?,entity_updated_by=?,status=?,status_timestamp=?,consec_auth_failed_count=?,last_auth_failed_ts=? where entity_id=? and entity_version=?";
		final SubjectImpl updatedSubject = new SubjectImpl(entity);
		updatedSubject.updated();
		final Optional<UUID> optionalUpdatedBy = updatedSubject.getUpdatedByEntityId();
		final UUID updatedById = optionalUpdatedBy.isPresent() ? optionalUpdatedBy.get() : null;
		final int updateCount = jdbc.update(sql,
		        updatedSubject.getEntityVersion(),
		        new Timestamp(updatedSubject.getEntityUpdatedOn()),
		        updatedById,
		        updatedSubject.getStatus().code,
		        new Timestamp(updatedSubject.getStatusTimestamp()),
		        updatedSubject.getConsecutiveAuthenticationFailedCount(),
		        updatedSubject.getLastTimeAuthenticationFailed() > 0 ? new Timestamp(updatedSubject.getLastTimeAuthenticationFailed()) : null,
		        updatedSubject.getEntityId(),
		        entity.getEntityVersion());
		if (updateCount == 0) {
			if (exists(entity.getEntityId())) {
				throw new StaleObjectException();
			}

			throw new ObjectNotFoundException();
		}

		return updatedSubject;
	}

	@Override
	public Subject update(final Subject entity, final UUID updatedBy) throws DAOException, StaleObjectException, ObjectNotFoundException {
		validateForUpdate(entity);
		Assert.notNull(updatedBy, "updatedBy is required");

		final String sql = "update t_subject set entity_version=?, entity_updated_on=?,entity_updated_by=?,status=?,status_timestamp=?,consec_auth_failed_count=?,last_auth_failed_ts=? where entity_id=? and entity_version=?";
		final SubjectImpl updatedSubject = new SubjectImpl(entity);
		updatedSubject.updated(updatedBy);
		final int updateCount = jdbc.update(sql,
		        updatedSubject.getEntityVersion(),
		        new Timestamp(updatedSubject.getEntityUpdatedOn()),
		        updatedBy,
		        updatedSubject.getStatus().code,
		        new Timestamp(updatedSubject.getStatusTimestamp()),
		        updatedSubject.getConsecutiveAuthenticationFailedCount(),
		        updatedSubject.getLastTimeAuthenticationFailed() > 0 ? new Timestamp(updatedSubject.getLastTimeAuthenticationFailed()) : null,
		        updatedSubject.getEntityId(),
		        entity.getEntityVersion());
		if (updateCount == 0) {
			if (exists(entity.getEntityId())) {
				throw new StaleObjectException();
			}

			throw new ObjectNotFoundException();
		}

		return updatedSubject;
	}
}
