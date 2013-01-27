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
