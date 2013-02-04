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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.dao.EntityRowMapperSupport;
import com.azaptree.services.domain.entity.dao.JDBCEntityDAOSupport;
import com.azaptree.services.security.domain.SessionAttribute;
import com.azaptree.services.security.domain.impl.SessionAttributeImpl;
import com.google.common.collect.ImmutableMap;

public class SessionAttributeDAO extends JDBCEntityDAOSupport<SessionAttribute> implements SesssionAttributeRepository {

	private final RowMapper<SessionAttribute> rowMapper = new EntityRowMapperSupport<SessionAttribute>() {

		@Override
		protected SessionAttribute createEntity(final ResultSet rs, final int rowNum) throws SQLException {
			return new SessionAttributeImpl((UUID) rs.getObject("session_id"), rs.getString("name"), rs.getString("value"));
		}

		@Override
		protected SessionAttribute mapRow(final SessionAttribute entity, final ResultSet rs, final int rowNum) throws SQLException {
			return entity;
		}
	};

	public SessionAttributeDAO(final JdbcTemplate jdbc) {
		super(jdbc, "t_session_attribute");
	}

	/**
	 * 
	 * throws DataIntegrityViolationException if session attribute JSON value is not valid JSON
	 */
	@Override
	public SessionAttribute create(final SessionAttribute entity) {
		Assert.notNull(entity, "entity is required");
		final SessionAttributeImpl sessionAttr = new SessionAttributeImpl(entity);
		sessionAttr.setEntityId(UUID.randomUUID());
		final String sql = "insert into t_session_attribute (entity_id,name,session_id,value) values (?,?,?,cast(? as json))";
		jdbc.update(sql,
		        sessionAttr.getEntityId(),
		        sessionAttr.getName(),
		        sessionAttr.getSessionId(),
		        sessionAttr.getJson());
		return sessionAttr;
	}

	@Override
	public String getAttributeJsonValue(final UUID sessionId, final String key) {
		Assert.notNull(sessionId, "sessionId is required");
		Assert.hasText(key, "key is required");
		try {
			return jdbc.queryForObject("select value from t_session_attribute where session_id = ? and name = ?", String.class, sessionId, key);
		} catch (final IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<String> getAttributeKeys(final UUID sessionId) {
		Assert.notNull(sessionId);
		final String sql = "select name from t_session_attribute where session_id = ?";
		return jdbc.queryForList(sql, String.class, sessionId);
	}

	@Override
	protected RowMapper<SessionAttribute> getRowMapper() {
		return rowMapper;
	}

	@Override
	public Map<String, SessionAttribute> getSessionAttributes(final UUID sessionId) {
		Assert.notNull(sessionId, "sessionId is required");

		final ImmutableMap.Builder<String, SessionAttribute> builder = ImmutableMap.<String, SessionAttribute> builder();
		final Object[] args = { sessionId };
		for (final SessionAttribute attr : jdbc.query("select * from t_session_attribute where session_id = ?", args, rowMapper)) {
			builder.put(attr.getName(), attr);
		}

		return builder.build();
	}

	@Override
	protected void initFieldColumnMappings() {
		super.initFieldColumnMappings();
		fieldColumnMappings.put("Name", "name");
		fieldColumnMappings.put("SessionId", "session_id");
		fieldColumnMappings.put("Json", "value");
	}

	@Override
	public boolean removeAttribute(final UUID sessionId, final String key) {
		Assert.notNull(sessionId, "sessionId is required");
		Assert.hasText(key, "key is required");
		return jdbc.update("delete from t_session_attribute where session_id = ? and name = ?", sessionId, key) > 0;
	}

	@Override
	public boolean setAttribute(final UUID sessionId, final String key, final String jsonValue) {
		Assert.notNull(sessionId, "sessionId is required");
		Assert.hasText(key, "key is required");
		Assert.hasText(jsonValue, "jsonValue is required");

		try {
			jdbc.queryForInt("select 1 from t_session where entity_id = ?", sessionId);
			try {
				jdbc.queryForInt("select 1 from t_session_attribute where session_id = ? and name = ?", sessionId, key);
				return jdbc.update("update t_session_attribute set value = cast(? as json) where session_id = ? and name = ?", jsonValue, sessionId, key) > 0;
			} catch (final IncorrectResultSizeDataAccessException e) {
				create(new SessionAttributeImpl(sessionId, key, jsonValue));
				return true;
			}

		} catch (final IncorrectResultSizeDataAccessException e) {
			return false;
		}
	}

	@Override
	public SessionAttribute update(final SessionAttribute entity) {
		throw new UnsupportedOperationException();
	}
}
