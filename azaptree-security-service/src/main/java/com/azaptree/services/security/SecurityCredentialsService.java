package com.azaptree.services.security;

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

import java.util.Map;

public interface SecurityCredentialsService {
	/**
	 * 
	 * @param name
	 *            REQUIRED
	 * @param credential
	 *            REQUIRED
	 * @return
	 * @throws SecurityServiceException
	 * @throws UnsupportedCredentialTypeException
	 *             if the service does not know how to convert the credential to byte[]
	 */
	byte[] convertCredentialToBytes(String name, Object credential) throws SecurityServiceException, UnsupportedCredentialTypeException;

	/**
	 * Returns map of supported credentials types
	 * 
	 * @return
	 */
	Map<String, Class<?>> getSupportedCredentials() throws SecurityServiceException;

	boolean isCredentialSupported(String name, Object credential);
}
