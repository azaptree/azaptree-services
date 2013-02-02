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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * The purpose of the conversion to byte[] is to produce a byte source array that can be hashed and then compared to a HashedCredential.
 * 
 * @author alfio
 * 
 * @param <T>
 */
public interface CredentialToByteSourceConverter<T> {

	/**
	 * Converts a credential to a byte[]
	 * 
	 * @param credential
	 * @return
	 */
	byte[] convert(T credential);

	boolean isCompatible(String name, Object credential);

}
