package test.com.azaptree.services.security.config;

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

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.HashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.security.domain.config.impl.HashServiceConfig;

public class HashServiceConfigTest {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testHashService() {
		final SecureRandomNumberGenerator rng = new SecureRandomNumberGenerator();
		final byte[] privateSalt = rng.nextBytes(32).getBytes();
		final int hashIterations = 1024 * 128;
		final String algo = "SHA-256";
		final int nextBytesSize = 32;
		final HashServiceConfig config1 = new HashServiceConfig("testHash", privateSalt, hashIterations, algo, nextBytesSize);
		log.info("hashConfig: {}", config1);
		final HashService hashService1 = config1.createHashService();
		final HashService hashService2 = config1.createHashService();

		final HashRequest req1 = new HashRequest.Builder().setSource("password").build();
		final Hash hash1 = hashService1.computeHash(req1);

		final HashRequest req2 = new HashRequest.Builder().setSource("password").setSalt(hash1.getSalt()).build();
		final Hash hash2 = hashService2.computeHash(req2);

		Assert.assertEquals(hash2.toBase64(), hash1.toBase64());
	}

	@Test
	public void testHashService2() {
		final HashServiceConfig config1 = new HashServiceConfig("testHash");
		log.info("hashConfig: {}", config1);
		final HashService hashService1 = config1.createHashService();
		final HashService hashService2 = config1.createHashService();

		final HashRequest req1 = new HashRequest.Builder().setSource("password").build();
		final Hash hash1 = hashService1.computeHash(req1);

		final HashRequest req2 = new HashRequest.Builder().setSource("password").setSalt(hash1.getSalt()).build();
		final Hash hash2 = hashService2.computeHash(req2);

		Assert.assertEquals(hash2.toBase64(), hash1.toBase64());
	}

}
