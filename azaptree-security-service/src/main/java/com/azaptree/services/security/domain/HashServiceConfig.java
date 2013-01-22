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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.azaptree.services.domain.entity.VersionedEntity;

public interface HashServiceConfig extends VersionedEntity {

	String getName();

	/**
	 * Returns the name of the algorithm used to hash the input source, for example, SHA-256, MD5, etc.
	 * The name is expected to be a MessageDigest algorithm name.
	 * 
	 * @return
	 */
	String getHashAlgorithmName();

	int getHashIterations();

	byte[] getPrivateSalt();

	int getSecureRandomNumberGeneratorNextBytesSize();
}