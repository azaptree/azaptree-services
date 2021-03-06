package com.azaptree.services.security.domain;

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

import java.util.Date;
import java.util.UUID;

import com.azaptree.services.domain.entity.VersionedEntity;
import com.google.common.base.Optional;

/**
 * For security reasons, the actual credential is not known - only its hash.
 * 
 * Unique key constraint: subjectId + name
 * 
 * @author alfio
 * 
 */
public interface HashedCredential extends VersionedEntity, Hash {

	String getName();

	/**
	 * UUID of Subject that owns this HashedCredential
	 * 
	 * @return
	 */
	UUID getSubjectId();

	/**
	 * UUID of HashServiceConfiguration that was used to configure a HashService for this HashedCredential
	 * 
	 * @return
	 */
	UUID getHashServiceConfigurationId();

	/**
	 * A credential may have a policy that it expires after a certain time.
	 * 
	 * @return
	 */
	Optional<Date> getExpiresOn();

}
