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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
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
import com.google.common.collect.ImmutableSet;

public class HashedCredentialDAO extends JDBCVersionedEntityDAOSupport<HashedCredential> implements HashedCredentialRepository {

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

		entity.created();
		final String sql = "insert into t_hashed_credential "
		        + "(entity_id,entity_version,entity_created_on,entity_created_by,entity_updated_on,entity_updated_by,name,subject_id,hash,hash_algorithm,hash_iterations,salt)"
		        + " values (?,?,?,?,?,?,?,?,?,?,?,?)";

		final Optional<UUID> createdBy = entity.getCreatedByEntityId();
		final UUID createdByUUID = createdBy.isPresent() ? createdBy.get() : null;
		jdbc.update(sql,
		        entity.getEntityId(),
		        entity.getEntityVersion(),
		        new Timestamp(entity.getEntityCreatedOn()),
		        createdByUUID,
		        new Timestamp(entity.getEntityUpdatedOn()),
		        createdByUUID,
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

		jdbc.update(sql,
		        entity.getEntityId(),
		        entity.getEntityVersion(),
		        new Timestamp(entity.getEntityCreatedOn()),
		        createdBy,
		        new Timestamp(entity.getEntityUpdatedOn()),
		        createdBy,
		        entity.getName(),
		        entity.getSubjecId(),
		        entity.getHash(),
		        entity.getHashAlgorithm(),
		        entity.getHashIterations(),
		        entity.getSalt());
		return entity;
	}

	@Override
	public Set<HashedCredential> findBySubjectId(final UUID subjectId) throws DAOException {
		Assert.notNull(subjectId, "subjectId is required");
		final String sql = "select * from t_hashed_credential where subject_id = ?";
		final Object[] args = { subjectId };
		final List<HashedCredential> rs = jdbc.query(sql, args, rowMapper);
		if (rs.isEmpty()) {
			return Collections.emptySet();
		}
		return ImmutableSet.<HashedCredential> builder().addAll(rs).build();
	}

	@Override
	public HashedCredential findBySubjectIdAndName(final UUID subjectId, final String name) {
		Assert.notNull(subjectId, "subjectId is required");
		Assert.hasText(name, "name is required");
		final String sql = "select * from t_hashed_credential where subject_id = ? and name = ?";
		final Object[] args = { subjectId, name };
		try {
			return jdbc.queryForObject(sql, args, rowMapper);
		} catch (final IncorrectResultSizeDataAccessException e) {
			return null;
		}
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
	public boolean subjectHasCredential(final UUID subjectId, final String name, final byte[] hash) {
		Assert.notNull(subjectId, "subjectId is required");
		Assert.hasText(name, "name is required");
		Assert.isTrue(ArrayUtils.isNotEmpty(hash), "hash is required");
		final String sql = "select hash from t_hashed_credential where subject_id = ? and name = ?";
		final Object[] args = { subjectId, name };
		return jdbc.query(sql, args, new ResultSetExtractor<Boolean>() {

			@Override
			public Boolean extractData(final ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					return Arrays.equals(rs.getBytes(1), hash);
				}

				return false;
			}
		});
	}

	@Override
	public HashedCredential update(final HashedCredential entity) throws DAOException, StaleObjectException, ObjectNotFoundException {
		validateForUpdate(entity);

		final String sql = "update t_hashed_credential set entity_version=?, entity_updated_on=?,entity_updated_by=?," +
		        "name=?,subject_id=?,hash=?,hash_algorithm=?,hash_iterations=?,salt=?" +
		        " where entity_id=? and entity_version=?";
		final HashedCredentialImpl updatedEntity = new HashedCredentialImpl(entity);
		updatedEntity.updated();
		final Optional<UUID> optionalUpdatedBy = updatedEntity.getUpdatedByEntityId();
		final UUID updatedById = optionalUpdatedBy.isPresent() ? optionalUpdatedBy.get() : null;
		final int updateCount = jdbc.update(sql,
		        updatedEntity.getEntityVersion(),
		        new Timestamp(updatedEntity.getEntityUpdatedOn()),
		        updatedById,
		        entity.getName(),
		        entity.getSubjecId(),
		        entity.getHash(),
		        entity.getHashAlgorithm(),
		        entity.getHashIterations(),
		        entity.getSalt(),
		        updatedEntity.getEntityId(),
		        entity.getEntityVersion());
		if (updateCount == 0) {
			if (exists(updatedEntity.getEntityId())) {
				throw new StaleObjectException();
			}

			throw new ObjectNotFoundException();
		}

		return updatedEntity;
	}

	@Override
	public HashedCredential update(final HashedCredential entity, final UUID updatedBy) throws DAOException, StaleObjectException, ObjectNotFoundException {
		validateForUpdate(entity);

		final String sql = "update t_hashed_credential set entity_version=?, entity_updated_on=?,entity_updated_by=?," +
		        "name=?,subject_id=?,hash=?,hash_algorithm=?,hash_iterations=?,salt=?" +
		        " where entity_id=? and entity_version=?";
		final HashedCredentialImpl updatedEntity = new HashedCredentialImpl(entity);
		updatedEntity.updated(updatedBy);
		final Optional<UUID> optionalUpdatedBy = updatedEntity.getUpdatedByEntityId();
		final UUID updatedById = optionalUpdatedBy.isPresent() ? optionalUpdatedBy.get() : null;
		final int updateCount = jdbc.update(sql,
		        updatedEntity.getEntityVersion(),
		        new Timestamp(updatedEntity.getEntityUpdatedOn()),
		        updatedById,
		        entity.getName(),
		        entity.getSubjecId(),
		        entity.getHash(),
		        entity.getHashAlgorithm(),
		        entity.getHashIterations(),
		        entity.getSalt(),
		        updatedEntity.getEntityId(),
		        entity.getEntityVersion());
		if (updateCount == 0) {
			if (exists(updatedEntity.getEntityId())) {
				throw new StaleObjectException();
			}

			throw new ObjectNotFoundException();
		}

		return updatedEntity;
	}
}
