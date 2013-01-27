package com.azaptree.services.security.dao;

import java.util.Set;
import java.util.UUID;

import com.azaptree.services.domain.entity.dao.DAOException;
import com.azaptree.services.domain.entity.dao.VersionedEntityDAO;
import com.azaptree.services.security.domain.HashedCredential;

public interface HashedCredentialRepository extends VersionedEntityDAO<HashedCredential> {

	Set<HashedCredential> findBySubjectId(UUID subjectId) throws DAOException;

	HashedCredential findBySubjectIdAndName(UUID subjectId, String name);

	boolean subjectHasCredential(UUID subjectId, String name, byte[] hash);

}
