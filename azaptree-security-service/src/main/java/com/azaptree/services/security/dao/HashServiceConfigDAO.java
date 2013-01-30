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
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.dao.DAOException;
import com.azaptree.services.domain.entity.dao.JDBCVersionedEntityDAOSupport;
import com.azaptree.services.domain.entity.dao.ObjectNotFoundException;
import com.azaptree.services.domain.entity.dao.StaleObjectException;
import com.azaptree.services.domain.entity.dao.VersionedEntityRowMapperSupport;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.config.impl.HashServiceConfig;
import com.google.common.base.Optional;

public class HashServiceConfigDAO extends JDBCVersionedEntityDAOSupport<HashServiceConfiguration> {
	private final RowMapper<HashServiceConfiguration> rowMapper = new VersionedEntityRowMapperSupport<HashServiceConfiguration>() {

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

	public HashServiceConfigDAO(final JdbcTemplate jdbc) {
		super(jdbc, "t_hash_service_config");
	}

	@Override
	public HashServiceConfiguration create(final HashServiceConfiguration entity) {
		Assert.notNull(entity, "entity is required");

		final HashServiceConfig config = new HashServiceConfig(entity);
		config.created();

		final Optional<UUID> createdBy = entity.getCreatedByEntityId();
		final UUID createdByUUID = createdBy.isPresent() ? createdBy.get() : null;
		final String sql = "insert into t_hash_service_config "
		        + "(entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by,name,private_salt,hash_iterations,hash_algorithm,secure_rand_next_bytes_size)"
		        + " values (?,?,?,?,?,?,?,?,?,?,?)";

		jdbc.update(sql,
		        config.getEntityId(),
		        config.getEntityVersion(),
		        new Timestamp(config.getEntityCreatedOn()),
		        createdByUUID,
		        new Timestamp(config.getEntityUpdatedOn()),
		        createdByUUID,
		        config.getName(),
		        config.getPrivateSalt(),
		        config.getHashIterations(),
		        config.getHashAlgorithmName(),
		        config.getSecureRandomNumberGeneratorNextBytesSize());
		return entity;
	}

	@Override
	public HashServiceConfiguration create(final HashServiceConfiguration entity, final UUID createdBy) {
		Assert.notNull(entity, "entity is required");

		final HashServiceConfig config = new HashServiceConfig(entity);
		config.created(createdBy);

		final String sql = "insert into t_hash_service_config "
		        + "(entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by,name,private_salt,hash_iterations,hash_algorithm,secure_rand_next_bytes_size)"
		        + " values (?,?,?,?,?,?,?,?,?,?,?)";

		jdbc.update(sql,
		        config.getEntityId(),
		        config.getEntityVersion(),
		        new Timestamp(config.getEntityCreatedOn()),
		        createdBy,
		        new Timestamp(config.getEntityUpdatedOn()),
		        createdBy,
		        config.getName(),
		        config.getPrivateSalt(),
		        config.getHashIterations(),
		        config.getHashAlgorithmName(),
		        config.getSecureRandomNumberGeneratorNextBytesSize());
		return entity;
	}

	@Override
	protected RowMapper<HashServiceConfiguration> getRowMapper() {
		return rowMapper;
	}

	@Override
	public HashServiceConfiguration update(final HashServiceConfiguration entity) throws DAOException, StaleObjectException, ObjectNotFoundException {
		validateForUpdate(entity);

		final HashServiceConfig config = new HashServiceConfig(entity);
		config.updated();

		final Optional<UUID> updatedBy = entity.getUpdatedByEntityId();
		final UUID updatedByUUID = updatedBy.isPresent() ? updatedBy.get() : null;
		final String sql = "update t_hash_service_config "
		        + "set entity_id=?,entity_version=?,entity_updated_on=?,entity_updated_by=?,name=?,private_salt=?,hash_iterations=?,hash_algorithm=?,secure_rand_next_bytes_size=?)"
		        + "where entity_id=? and entity_version=?";

		final int updateCount = jdbc.update(sql,
		        config.getEntityId(),
		        config.getEntityVersion(),
		        new Timestamp(config.getEntityUpdatedOn()),
		        updatedByUUID,
		        config.getName(),
		        config.getPrivateSalt(),
		        config.getHashIterations(),
		        config.getHashAlgorithmName(),
		        config.getSecureRandomNumberGeneratorNextBytesSize(),
		        config.getEntityId(),
		        config.getEntityVersion());
		if (updateCount == 0) {
			if (exists(entity.getEntityId())) {
				throw new StaleObjectException();
			}

			throw new ObjectNotFoundException();
		}
		return entity;
	}

	@Override
	public HashServiceConfiguration update(final HashServiceConfiguration entity, final UUID updatedBy) throws DAOException, StaleObjectException,
	        ObjectNotFoundException {
		validateForUpdate(entity);

		final HashServiceConfig config = new HashServiceConfig(entity);
		config.updated(updatedBy);

		final String sql = "update t_hash_service_config "
		        + "set entity_id=?,entity_version=?,entity_updated_on=?,entity_updated_by=?,name=?,private_salt=?,hash_iterations=?,hash_algorithm=?,secure_rand_next_bytes_size=?)"
		        + "where entity_id=? and entity_version=?";

		final int updateCount = jdbc.update(sql,
		        config.getEntityId(),
		        config.getEntityVersion(),
		        new Timestamp(config.getEntityUpdatedOn()),
		        updatedBy,
		        config.getName(),
		        config.getPrivateSalt(),
		        config.getHashIterations(),
		        config.getHashAlgorithmName(),
		        config.getSecureRandomNumberGeneratorNextBytesSize(),
		        config.getEntityId(),
		        config.getEntityVersion());
		if (updateCount == 0) {
			if (exists(entity.getEntityId())) {
				throw new StaleObjectException();
			}

			throw new ObjectNotFoundException();
		}
		return entity;
	}

}
