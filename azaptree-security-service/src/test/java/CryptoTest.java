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
import org.apache.commons.lang3.time.StopWatch;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CryptoTest {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void test_hashingService() {
		log.info("*** test_hashingService ***");
		final DefaultHashService hashService = new DefaultHashService();

		final SecureRandomNumberGenerator secureRandomNumberGenerator = new SecureRandomNumberGenerator();
		secureRandomNumberGenerator.setDefaultNextBytesSize(64);
		final ByteSource privateSalt = secureRandomNumberGenerator.nextBytes();
		final ByteSource publicSalt = secureRandomNumberGenerator.nextBytes();

		log.info("privateSalt .length = {}", privateSalt.getBytes().length);

		hashService.setHashAlgorithmName("SHA-512");
		hashService.setHashIterations(1024 * 64);
		hashService.setPrivateSalt(privateSalt);
		hashService.setRandomNumberGenerator(secureRandomNumberGenerator);
		hashService.setGeneratePublicSalt(true);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").setSalt(publicSalt).build();
		final Hash hash = hashService.computeHash(hashRequest);
		log.info("hash.salt : {}", hash.getSalt());
		log.info("publicSalt : {}", publicSalt);
		log.info("hash Base64 : {}", hash.toBase64());
		final String hash1 = hashService.computeHash(hashRequest).toBase64();
		final String hash2 = hashService.computeHash(hashRequest).toBase64();
		log.info("hash1 Base64 : {}", hash1);
		log.info("hash2 Base64 : {}", hash2);
		Assert.assertEquals(hash1, hash2);

		Sha512Hash encodedPassword = new Sha512Hash("password", publicSalt, 1024 * 64);
		Sha512Hash encodedPassword2 = new Sha512Hash(encodedPassword.getBytes(), privateSalt, 1024 * 64);
		log.info("encodedPassword Base64 : {}", encodedPassword.toBase64());
		log.info("encodedPassword2 Base64 : {}", encodedPassword2.toBase64());

		Sha512Hash encodedPassword3 = new Sha512Hash("password", publicSalt, 1024 * 64);
		Sha512Hash encodedPassword4 = new Sha512Hash(encodedPassword3.getBytes(), privateSalt, 1024 * 64);
		log.info("encodedPassword3 Base64 : {}", encodedPassword3.toBase64());
		log.info("encodedPassword4 Base64 : {}", encodedPassword4.toBase64());

		Assert.assertEquals(encodedPassword2, encodedPassword4);
	}

	@Test
	public void test_hashingService_usingRandomSalts() {
		log.info("*** test_hashingService_usingRandomSalts ***");
		final DefaultHashService hashService = new DefaultHashService();

		final SecureRandomNumberGenerator secureRandomNumberGenerator = new SecureRandomNumberGenerator();
		secureRandomNumberGenerator.setDefaultNextBytesSize(64);
		final ByteSource privateSalt = secureRandomNumberGenerator.nextBytes();

		hashService.setHashAlgorithmName("SHA-512");
		hashService.setHashIterations(1024 * 128);
		hashService.setPrivateSalt(privateSalt);
		hashService.setRandomNumberGenerator(secureRandomNumberGenerator);
		hashService.setGeneratePublicSalt(true);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		final Hash hash = hashService.computeHash(hashRequest);
		stopWatch.stop();
		final byte[] hashBytes = hash.getBytes();

		log.info("hashBytes length = {}", hashBytes.length);
		log.info("hash Base64 length = {}", hash.toBase64().length());
		log.info("hash time: {}", stopWatch.getTime());
		log.info("hash.salt : {}", hash.getSalt());
		final ByteSource salt = hash.getSalt();
		log.info("salt : {}", salt);
		log.info("hash Base64 : {}", hash.toBase64());

		final String hash1 = hashService.computeHash(new HashRequest.Builder().setSource("password").setSalt(salt).build())
		        .toBase64();
		final String hash2 = hashService.computeHash(new HashRequest.Builder().setSource("password").setSalt(salt).build()).toBase64();
		log.info("hash1 Base64 : {}", hash1);
		log.info("hash2 Base64 : {}", hash2);
		Assert.assertEquals(hash1, hash2);

		Sha512Hash encodedPassword = new Sha512Hash("password", salt, 1024 * 64);
		Sha512Hash encodedPassword2 = new Sha512Hash(encodedPassword.getBytes(), privateSalt, 1024 * 64);
		log.info("encodedPassword Base64 : {}", encodedPassword.toBase64());
		log.info("encodedPassword2 Base64 : {}", encodedPassword2.toBase64());

		Sha512Hash encodedPassword3 = new Sha512Hash("password", salt, 1024 * 64);
		Sha512Hash encodedPassword4 = new Sha512Hash(encodedPassword3.getBytes(), privateSalt, 1024 * 64);
		log.info("encodedPassword3 Base64 : {}", encodedPassword3.toBase64());
		log.info("encodedPassword4 Base64 : {}", encodedPassword4.toBase64());

		Assert.assertEquals(encodedPassword2, encodedPassword4);

		hashService.setHashIterations(1024 * 127);

	}

	@Test
	public void test_secureRandomNumberGenerator_nextBytesSize() {
		log.info("*** test_secureRandomNumberGenerator_nextBytesSize ***");
		final DefaultHashService hashService = new DefaultHashService();
		final SecureRandomNumberGenerator secureRandomNumberGenerator = new SecureRandomNumberGenerator();
		secureRandomNumberGenerator.setDefaultNextBytesSize(8);
		final ByteSource privateSalt = secureRandomNumberGenerator.nextBytes();
		log.info("privateSalt = {}", privateSalt);
		log.info("privateSalt byte length = {}", privateSalt.getBytes().length);

		hashService.setHashAlgorithmName("SHA-512");
		hashService.setHashIterations(1024 * 128);
		hashService.setPrivateSalt(privateSalt);
		hashService.setRandomNumberGenerator(secureRandomNumberGenerator);
		hashService.setGeneratePublicSalt(true);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);

		final DefaultHashService hashService2 = new DefaultHashService();
		final SecureRandomNumberGenerator secureRandomNumberGenerator2 = new SecureRandomNumberGenerator();
		secureRandomNumberGenerator2.setDefaultNextBytesSize(16);

		hashService2.setHashAlgorithmName("SHA-512");
		hashService2.setHashIterations(1024 * 128);
		hashService2.setPrivateSalt(privateSalt);
		hashService2.setRandomNumberGenerator(secureRandomNumberGenerator2);
		hashService2.setGeneratePublicSalt(true);

		final HashRequest hashRequest2 = new HashRequest.Builder().setSource("password").setSalt(hash.getSalt()).build();
		final Hash hash2 = hashService.computeHash(hashRequest2);

		log.info("hash = {}", hash.toBase64());
		log.info("hash2 = {}", hash2.toBase64());

		Assert.assertEquals(hash2.toBase64(), hash.toBase64());
	}
}
