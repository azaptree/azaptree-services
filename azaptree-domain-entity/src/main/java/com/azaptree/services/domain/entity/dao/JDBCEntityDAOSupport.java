package com.azaptree.services.domain.entity.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.Entity;

public abstract class JDBCEntityDAOSupport<T extends Entity> implements EntityDAO<T> {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected final JdbcTemplate jdbc;

	public JDBCEntityDAOSupport(final JdbcTemplate jdbc) {
		Assert.notNull(jdbc, "jdbc is required");
		this.jdbc = jdbc;
	}

}
