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
import com.azaptree.services.security.domain.HashedCredential;
import com.azaptree.services.security.domain.impl.HashedCredentialImpl;
import com.google.common.base.Optional;

public class HashedCredentialDAO extends JDBCVersionedEntityDAOSupport<HashedCredential> {

	private final RowMapper<HashedCredential> rowMapper = new VersionedEntityRowMapperSupport<HashedCredential>() {

		@Override
		protected HashedCredential createEntity(final ResultSet rs, final int rowNum) throws SQLException {
			final UUID subjectId = (UUID) rs.getObject("subject_id");
			final String name = rs.getString("name");
			final byte[] hash = rs.getBytes("hash");
			final String hashAlgorithm = rs.getString("hash_algorithm");
			final int hashIterations = rs.getInt("hash_iterations");
			final byte[] salt = rs.getBytes("salt");

			return new HashedCredentialImpl(subjectId, name, hash, hashAlgorithm, hashIterations, salt);
		}

		@Override
		protected HashedCredential mapRow(final HashedCredential entity, final ResultSet rs, final int rowNum) {
			return entity;
		}
	};

	public HashedCredentialDAO(final JdbcTemplate jdbc) {
		super(jdbc, "t_hashed_credential");
	}

	@Override
	public HashedCredential create(final HashedCredential hashedCredential) {
		Assert.notNull(hashedCredential, "hashedCredential is required");

		final HashedCredentialImpl entity = new HashedCredentialImpl(hashedCredential.getSubjecId(), hashedCredential.getName(), hashedCredential.getHash(),
		        hashedCredential.getHashAlgorithm(), hashedCredential.getHashIterations(), hashedCredential.getSalt());
		final Optional<UUID> createdBy = entity.getCreatedByEntityId();

		entity.created();
		final String sql = "insert into t_hashed_credential "
		        + "(entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by,name,subject_id,hash,hash_algorithm,hash_iterations,salt)"
		        + " values (?,?,?,?,?,?,?,?,?,?,?,?)";

		final Optional<UUID> updatedBy = entity.getUpdatedByEntityId();
		jdbc.update(sql,
		        entity.getEntityId(),
		        entity.getEntityVersion(),
		        new Timestamp(entity.getEntityCreatedOn()),
		        createdBy.isPresent() ? createdBy.get() : null,
		        new Timestamp(entity.getEntityUpdatedOn()),
		        updatedBy.isPresent() ? updatedBy.get() : null,
		        entity.getName(),
		        entity.getSubjecId(),
		        entity.getHash(),
		        entity.getHashAlgorithm(),
		        entity.getHashIterations(),
		        entity.getSalt());
		return entity;
	}

	@Override
	public HashedCredential create(final HashedCredential hashedCredential, final UUID createdBy) {
		Assert.notNull(hashedCredential, "hashedCredential is required");

		final HashedCredentialImpl entity = new HashedCredentialImpl(hashedCredential.getSubjecId(), hashedCredential.getName(), hashedCredential.getHash(),
		        hashedCredential.getHashAlgorithm(), hashedCredential.getHashIterations(), hashedCredential.getSalt());

		entity.created(createdBy);
		final String sql = "insert into t_hashed_credential "
		        + "(entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by,name,subject_id,hash,hash_algorithm,hash_iterations,salt)"
		        + " values (?,?,?,?,?,?,?,?,?,?,?,?)";

		final Optional<UUID> updatedBy = entity.getUpdatedByEntityId();
		jdbc.update(sql,
		        entity.getEntityId(),
		        entity.getEntityVersion(),
		        new Timestamp(entity.getEntityCreatedOn()),
		        createdBy,
		        new Timestamp(entity.getEntityUpdatedOn()),
		        updatedBy.isPresent() ? updatedBy.get() : null,
		        entity.getName(),
		        entity.getSubjecId(),
		        entity.getHash(),
		        entity.getHashAlgorithm(),
		        entity.getHashIterations(),
		        entity.getSalt());
		return entity;
	}

	@Override
	protected RowMapper<HashedCredential> getRowMapper() {
		return rowMapper;
	}

	@Override
	protected void initFieldColumnMappings() {
		super.initFieldColumnMappings();
		fieldColumnMappings.put("Name", "name");
		fieldColumnMappings.put("SubjectId", "subject_id");
		fieldColumnMappings.put("Hash", "hash");
		fieldColumnMappings.put("HashAlgorithm", "hash_algorithm");
		fieldColumnMappings.put("HashIterations", "hash_iterations");
		fieldColumnMappings.put("Salt", "salt");
	}

	@Override
	public HashedCredential update(final HashedCredential entity) throws DAOException, StaleObjectException, ObjectNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashedCredential update(final HashedCredential entity, final UUID updatedBy) throws DAOException, StaleObjectException, ObjectNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

}
