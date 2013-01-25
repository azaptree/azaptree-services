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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.Entity;
import com.google.common.collect.ImmutableMap;

public abstract class JDBCEntityDAOSupport<T extends Entity> implements EntityDAO<T> {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected final JdbcTemplate jdbc;
	protected final String table;

	/**
	 * Maps entity field to table column
	 */
	protected Map<String, String> fieldColumnMappings = new HashMap<>();

	public JDBCEntityDAOSupport(final JdbcTemplate jdbc, final String table) {
		Assert.notNull(jdbc, "jdbc is required");
		Assert.hasText("table");
		this.jdbc = jdbc;
		this.table = table;
		initFieldColumnMappings();
		buildImmutableFieldColumnMappings();
	}

	/**
	 * Appends LIMIT and OFFSET for POSTGRESQL SQL queries
	 * 
	 * <code>
		  SELECT select_list
			FROM table_expression
			[ ORDER BY ... ]
			[ LIMIT { number | ALL } ] [ OFFSET number ]
	   </code>
	 * 
	 * @param sql
	 * @param page
	 */
	protected void appendLimitAndOffSet(final StringBuilder sql, final Page page) {
		sql.append(" LIMIT ").append(page.getPageSize()).append(" OFFSET ").append(page.getOffset());
	}

	protected void appendOrderBy(final StringBuilder sql, final SortField... fields) {
		if (ArrayUtils.isNotEmpty(fields)) {
			sql.append(" ORDER BY ");
			SortField f;
			for (int i = 0; i < fields.length;) {
				f = fields[i];
				final String column = fieldColumnMappings.get(f.getFieldName());
				Assert.notNull(column, "Invalid field : " + f.getFieldName());
				sql.append(column).append(' ');
				if (!f.isAscending()) {
					sql.append("DESC");
				}
				if (++i < fields.length) {
					sql.append(",");
				}
			}
		}
	}

	protected void buildImmutableFieldColumnMappings() {
		fieldColumnMappings = ImmutableMap.<String, String> builder().putAll(fieldColumnMappings).build();
	}

	protected void check(final Page page, final SortField... fields) {
		Assert.notNull(page, "page is required");
		checkEntityField(fields);
	}

	protected void checkEntityField(final SortField... field) {
		if (ArrayUtils.isEmpty(field)) {
			return;
		}
		for (final SortField f : field) {
			Assert.isTrue(fieldColumnMappings.containsKey(f.getFieldName()), "Invalid field: " + field);
		}
	}

	@Override
	public boolean delete(final UUID id) {
		Assert.notNull(id, "id is required");
		final String sql = new StringBuilder("delete from ").append(table).append(" where entity_id = ?").toString();
		return jdbc.update(sql, id) > 0;
	}

	@Override
	public SearchResults<T> findAll(final Page page, final SortField... sort) {
		check(page, sort);
		final StringBuilder sql = new StringBuilder("select * from t_subject");
		appendOrderBy(sql, sort);
		appendLimitAndOffSet(sql, page);

		final long totalCount = getTotalCount();
		final List<T> data = jdbc.query(sql.toString(), getRowMapper());

		return new SearchResults<>(page, totalCount, data);
	}

	@Override
	public T findById(final UUID id) {
		Assert.notNull(id, "id is required");
		final String sql = new StringBuilder("select * from ").append(table).append(" where entity_id = ?").toString();
		final Object[] args = { id };
		try {
			return jdbc.queryForObject(sql, args, getRowMapper());
		} catch (final IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	@Override
	public Set<String> getEntityFields() {
		return fieldColumnMappings.keySet();
	}

	protected abstract RowMapper<T> getRowMapper();

	@Override
	public long getTotalCount() {
		return jdbc.queryForLong(new StringBuilder("select count(*) from ").append(table).toString());
	}

	/**
	 * <code>
	 * EntityId -> entity_id
	 * </code>
	 */
	protected void initFieldColumnMappings() {
		fieldColumnMappings.put("EntityId", "entity_id");
	}
}
