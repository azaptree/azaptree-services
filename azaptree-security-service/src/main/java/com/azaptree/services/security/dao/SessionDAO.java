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
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.dao.EntityRowMapperSupport;
import com.azaptree.services.domain.entity.dao.JDBCEntityDAOSupport;
import com.azaptree.services.security.domain.Session;
import com.azaptree.services.security.domain.impl.SessionImpl;

public class SessionDAO extends JDBCEntityDAOSupport<Session> implements SessionRepository {

	public RowMapper<Session> rowMapper = new EntityRowMapperSupport<Session>() {

		@Override
		protected Session createEntity(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID subjectId = (UUID) rs.getObject("subject_id");
			final long createdOn = rs.getTimestamp("created_on").getTime();
			final long lastAccessedOn = rs.getTimestamp("last_accessed_on").getTime();
			final int timeoutSeconds = rs.getInt("timeout");
			final String host = rs.getString("host");
			return new SessionImpl(subjectId, createdOn, lastAccessedOn, timeoutSeconds, host);
		}

		@Override
		protected Session mapRow(final Session entity, final ResultSet rs, final int rowNum) throws SQLException {
			return entity;
		}
	};

	public SessionDAO(final JdbcTemplate jdbc) {
		super(jdbc, "t_session");
	}

	@Override
	public Session create(final Session entity) {
		final String sql = "insert into t_session (entity_id,subject_id,created_on,last_accessed_on,timeout,host) values (?,?,?,?,?,cast(? as inet))";
		final SessionImpl session = new SessionImpl(entity);
		session.setEntityId(UUID.randomUUID());
		session.validate();
		jdbc.update(sql,
		        session.getEntityId(),
		        session.getSubjectId(),
		        new Timestamp(session.getCreatedOn()),
		        new Timestamp(session.getLastAccessedOn()),
		        session.getTimeoutSeconds(),
		        session.getHost());

		return session;
	}

	@Override
	public int deleteSessionsBySubjectId(final UUID subjectId) {
		Assert.notNull(subjectId, "subjectId is required");
		final String sql = "delete from t_session where subject_id = ?";
		return jdbc.update(sql, subjectId);
	}

	@Override
	protected RowMapper<Session> getRowMapper() {
		return rowMapper;
	}

	@Override
	public UUID[] getSessionIdsBySubject(final UUID subjectId) {
		final String sql = "select entity_id from t_session where subject_id = ?";
		final Object[] args = { subjectId };
		final List<UUID> sessionIds = new LinkedList<>();
		jdbc.query(sql, args, new RowCallbackHandler() {

			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				sessionIds.add((UUID) rs.getObject(1));
			}
		});
		return sessionIds.toArray(new UUID[sessionIds.size()]);
	}

	@Override
	public boolean touchSession(final UUID sessionId) {
		Assert.notNull(sessionId, "sessionId is required");
		final String sql = "update t_session set last_accessed_on = ? where entity_id = ?";
		return jdbc.update(sql, new Timestamp(System.currentTimeMillis()), sessionId) > 0;
	}

	@Override
	public Session update(final Session entity) {
		throw new UnsupportedOperationException();
	}
}
