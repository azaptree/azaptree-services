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
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.dao.JDBCEntityDAOSupport;
import com.azaptree.services.domain.entity.dao.Page;
import com.azaptree.services.domain.entity.dao.SearchResults;
import com.azaptree.services.domain.entity.dao.SortField;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.impl.SubjectImpl;
import com.google.common.base.Optional;

@Repository
public class SubjectDAO extends JDBCEntityDAOSupport<Subject> {

	private final RowMapper<Subject> rowMapper = new RowMapper<Subject>() {

		@Override
		public Subject mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final SubjectImpl subject = new SubjectImpl();
			subject.setEntityId(UUID.fromString(rs.getString("entity_id")));
			subject.setEntityVersion(rs.getLong("entity_version"));
			subject.setEntityCreatedOn(rs.getTimestamp("entity_created_on").getTime());
			final String createdBy = rs.getString("entity_created_by");
			if (StringUtils.isNotBlank(createdBy)) {
				subject.setCreatedBy(UUID.fromString(createdBy));
			}
			subject.setEntityUpdatedOn(rs.getTimestamp("entity_updated_on").getTime());
			final String updatedBy = rs.getString("entity_updated_by");
			if (StringUtils.isNotBlank(updatedBy)) {
				subject.setUpdatedBy(UUID.fromString(updatedBy));
			}
			return subject;
		}
	};

	public SubjectDAO(final JdbcTemplate jdbc) {
		super(jdbc);
	}

	@Override
	public Subject create(final Subject entity) {
		Assert.notNull(entity);

		final SubjectImpl subject = new SubjectImpl();
		final Optional<UUID> createdBy = entity.getCreatedByEntityId();

		subject.created(createdBy.isPresent() ? createdBy.get() : null);
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
	public boolean delete(final UUID id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SearchResults<Subject> findAll(final Page page, final SortField... sort) {
		return null;
	}

	@Override
	public SearchResults<Subject> findByExample(final Subject example, final Page page, final SortField... sort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Subject findById(final UUID id) {
		Assert.notNull(id, "id is required");
		final String sql = "select * from t_subject where entity_id = ?";
		final Object[] args = { id };
		try {
			return jdbc.queryForObject(sql, args, rowMapper);
		} catch (final IncorrectResultSizeDataAccessException e) {
			return null;
		}

	}

	@Override
	public void update(final Subject entity) {
		// TODO Auto-generated method stub

	}

}
