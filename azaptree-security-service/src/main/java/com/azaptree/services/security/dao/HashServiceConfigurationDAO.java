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
import java.util.UUID;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.dao.DAOException;
import com.azaptree.services.domain.entity.dao.EntityRowMapperSupport;
import com.azaptree.services.domain.entity.dao.JDBCEntityDAOSupport;
import com.azaptree.services.domain.entity.dao.ObjectNotFoundException;
import com.azaptree.services.domain.entity.dao.StaleObjectException;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.config.impl.HashServiceConfig;

public class HashServiceConfigurationDAO extends JDBCEntityDAOSupport<HashServiceConfiguration> implements HashServiceConfigurationRepository {
	private final RowMapper<HashServiceConfiguration> rowMapper = new EntityRowMapperSupport<HashServiceConfiguration>() {

		@Override
		protected HashServiceConfig createEntity(final ResultSet rs, final int rowNum) throws SQLException {
			final String name = rs.getString("name");
			final byte[] privateSalt = rs.getBytes("private_salt");
			final String hashAlgorithm = rs.getString("hash_algorithm");
			final int hashIterations = rs.getInt("hash_iterations");
			final int secureRandomNumberGeneratorNextBytesSize = rs.getInt("secure_rand_next_bytes_size");

			return new HashServiceConfig(name, privateSalt, hashIterations, hashAlgorithm, secureRandomNumberGeneratorNextBytesSize);
		}

		@Override
		protected HashServiceConfiguration mapRow(final HashServiceConfiguration entity, final ResultSet rs, final int rowNum) {
			return entity;
		}
	};

	public HashServiceConfigurationDAO(final JdbcTemplate jdbc) {
		super(jdbc, "t_hash_service_config");
	}

	@Override
	public HashServiceConfiguration create(final HashServiceConfiguration entity) {
		Assert.notNull(entity, "entity is required");

		final HashServiceConfig config = new HashServiceConfig(entity);
		config.setEntityId(UUID.randomUUID());

		final String sql = "insert into t_hash_service_config "
		        + "(entity_id,name,private_salt,hash_iterations,hash_algorithm,secure_rand_next_bytes_size)"
		        + " values (?,?,?,?,?,?)";

		jdbc.update(sql,
		        config.getEntityId(),
		        config.getName(),
		        config.getPrivateSalt(),
		        config.getHashIterations(),
		        config.getHashAlgorithmName(),
		        config.getSecureRandomNumberGeneratorNextBytesSize());
		return config;
	}

	@Override
	public HashServiceConfiguration findByName(final String name) {
		Assert.hasText(name, "name is required");
		final Object[] args = { name };
		try {
			return jdbc.queryForObject("select * from t_hash_service_config where name = ?", args, rowMapper);
		} catch (final IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	@Override
	protected RowMapper<HashServiceConfiguration> getRowMapper() {
		return rowMapper;
	}

	@Override
	protected void initFieldColumnMappings() {
		super.initFieldColumnMappings();
		fieldColumnMappings.put("Name", "name");
		fieldColumnMappings.put("PrivateSalt", "private_salt");
		fieldColumnMappings.put("HashAlgorithmName", "hash_algorithm");
		fieldColumnMappings.put("HashIterations", "hash_iterations");
		fieldColumnMappings.put("SecureRandomNumberGeneratorNextBytesSize", "secure_rand_next_bytes_size");
	}

	@Override
	public UUID lookupIdByName(final String name) {
		Assert.hasText(name, "name is required");
		final Object[] args = { name };
		try {
			return jdbc.queryForObject("select entity_id from t_hash_service_config where name = ?", args, UUID.class);
		} catch (final IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	/**
	 * HashServiceConfiguration should not be updated once created because other services may depend on hashes that were previously created by the HashService
	 * created by the config. For example, HashedCredential matching will depend on using the same HashServiceConfiguration for proper matching.
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public HashServiceConfiguration update(final HashServiceConfiguration entity) throws DAOException, StaleObjectException, ObjectNotFoundException {
		throw new UnsupportedOperationException("HashServiceConfiguration should not be updated once created");
	}

}
