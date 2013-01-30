package test.com.azaptree.services.security.dao;

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

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.domain.entity.dao.ObjectNotFoundException;
import com.azaptree.services.domain.entity.dao.StaleObjectException;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.HashedCredential;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.Subject.Status;
import com.azaptree.services.security.domain.config.impl.HashServiceConfig;
import com.azaptree.services.security.domain.impl.HashedCredentialImpl;
import com.azaptree.services.security.domain.impl.SubjectImpl;

@ContextConfiguration(classes = HashedCredentialDAOTest.Config.class)
public class HashedCredentialDAOTest extends AbstractTestNGSpringContextTests {

	@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
	@Configuration
	public static class Config implements TransactionManagementConfigurer {
		@Override
		public PlatformTransactionManager annotationDrivenTransactionManager() {
			return dataSourceTransactionManager();
		}

		@Bean(destroyMethod = "close")
		public DataSource dataSource() {
			final org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
			ds.setDefaultAutoCommit(false);
			ds.setDriverClassName("org.postgresql.Driver");
			ds.setUrl("jdbc:postgresql://localhost:5433/azaptree");
			ds.setUsername("azaptree");
			ds.setPassword("!azaptree");
			ds.setInitSQL("set search_path to azaptree");
			ds.setTestOnBorrow(true);
			ds.setTestOnConnect(true);
			ds.setValidationQuery("select 1");
			ds.setLogValidationErrors(true);
			ds.setInitialSize(10);
			ds.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;" +
			        "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
			        "org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport");
			ds.setTimeBetweenEvictionRunsMillis(30000);
			ds.setCommitOnReturn(true);

			return ds;
		}

		@Bean
		public org.springframework.jdbc.datasource.DataSourceTransactionManager dataSourceTransactionManager() {
			return new DataSourceTransactionManager(dataSource());
		}

		@Bean
		public HashedCredentialDAO hashedCredentialDAO() {
			return new HashedCredentialDAO(jdbcTemplate());
		}

		@Bean
		public HashService hashService() {
			final SecureRandomNumberGenerator rng = new SecureRandomNumberGenerator();
			final byte[] privateSalt = rng.nextBytes(32).getBytes();
			final int hashIterations = 1024 * 128;
			final String algo = "SHA-256";
			final int nextBytesSize = 32;
			final HashServiceConfig config = new HashServiceConfig("testHash", privateSalt, hashIterations, algo, nextBytesSize);
			return config.createHashService();
		}

		@Bean
		public JdbcTemplate jdbcTemplate() {
			return new JdbcTemplate(dataSource());
		}

		@Bean
		public SubjectDAO subjectDao() {
			return new SubjectDAO(jdbcTemplate());
		}
	}

	final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SubjectDAO subjectDao;

	@Autowired
	private HashedCredentialDAO hashedCredentialDAO;

	@Autowired
	private HashService hashService;

	@Transactional
	@Test
	public void test_create_findById_cascade_delete() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		Assert.assertNotNull(savedPassword);
		Assert.assertNotNull(savedPassword.getEntityId());
		Assert.assertNotNull(savedPassword.getSubjecId());
		log.info(savedPassword.toJson());

		final HashedCredential password2 = hashedCredentialDAO.findById(savedPassword.getEntityId());
		Assert.assertNotNull(password2);
		Assert.assertNotNull(password2.getEntityId());
		Assert.assertNotNull(password2.getSubjecId());
		log.info(password2.toJson());

		Assert.assertEquals(password2, savedPassword);

		subjectDao.delete(subject.getEntityId());
		Assert.assertNull(hashedCredentialDAO.findById(savedPassword.getEntityId()));
	}

	@Transactional
	@Test
	public void test_create_findById_delete() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		Assert.assertNotNull(savedPassword);
		Assert.assertNotNull(savedPassword.getEntityId());
		Assert.assertNotNull(savedPassword.getSubjecId());
		log.info(savedPassword.toJson());

		final HashedCredential password2 = hashedCredentialDAO.findById(savedPassword.getEntityId());
		Assert.assertNotNull(password2);
		Assert.assertNotNull(password2.getEntityId());
		Assert.assertNotNull(password2.getSubjecId());
		Assert.assertNotNull(password2.getHash());
		Assert.assertNotNull(password2.getHashAlgorithm());
		Assert.assertNotNull(password2.getSalt());
		Assert.assertEquals(password2.getSubjecId(), subject.getEntityId());
		Assert.assertTrue(Arrays.equals(hash.getBytes(), password2.getHash()));
		Assert.assertTrue(Arrays.equals(hash.getSalt().getBytes(), password2.getSalt()));

		log.info(password2.toJson());

		Assert.assertEquals(password2, savedPassword);

		hashedCredentialDAO.delete(savedPassword.getEntityId());
		Assert.assertNull(hashedCredentialDAO.findById(savedPassword.getEntityId()));
	}

	@Transactional
	@Test
	public void test_create_withCreatedBy_findById_delete() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password, UUID.randomUUID());

		Assert.assertNotNull(savedPassword);
		Assert.assertNotNull(savedPassword.getEntityId());
		Assert.assertNotNull(savedPassword.getSubjecId());
		log.info(savedPassword.toJson());

		final HashedCredential password2 = hashedCredentialDAO.findById(savedPassword.getEntityId());
		Assert.assertNotNull(password2);
		Assert.assertTrue(password2.getCreatedByEntityId().isPresent());
		Assert.assertNotNull(password2.getCreatedByEntityId().get());
		Assert.assertNotNull(password2.getEntityId());
		Assert.assertNotNull(password2.getSubjecId());
		Assert.assertNotNull(password2.getHash());
		Assert.assertNotNull(password2.getHashAlgorithm());
		Assert.assertNotNull(password2.getSalt());
		Assert.assertEquals(password2.getSubjecId(), subject.getEntityId());
		Assert.assertTrue(Arrays.equals(hash.getBytes(), password2.getHash()));
		Assert.assertTrue(Arrays.equals(hash.getSalt().getBytes(), password2.getSalt()));

		log.info(password2.toJson());

		Assert.assertEquals(password2, savedPassword);

		hashedCredentialDAO.delete(savedPassword.getEntityId());
		Assert.assertNull(hashedCredentialDAO.findById(savedPassword.getEntityId()));
	}

	@Transactional
	@Test
	public void test_findBySubjectId() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		final Set<HashedCredential> credentials = hashedCredentialDAO.findBySubjectId(subject.getEntityId());
		Assert.assertFalse(credentials.isEmpty());
		Assert.assertEquals(credentials.size(), 1);
		Assert.assertTrue(credentials.contains(savedPassword));

		Assert.assertTrue(hashedCredentialDAO.findBySubjectId(savedPassword.getEntityId()).isEmpty());
	}

	@Transactional
	@Test
	public void test_findBySubjectIdAndName() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		final HashedCredential retrievedCredential = hashedCredentialDAO.findBySubjectIdAndName(subject.getEntityId(), savedPassword.getName());
		Assert.assertEquals(retrievedCredential, savedPassword);

		Assert.assertNull(hashedCredentialDAO.findBySubjectIdAndName(subject.getEntityId(), savedPassword.getEntityId().toString()));
	}

	@Transactional
	@Test
	public void test_subjectHasCredential() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		Assert.assertTrue(hashedCredentialDAO.subjectHasCredential(subject.getEntityId(), savedPassword.getName(), savedPassword.getHash()));
		Assert.assertFalse(hashedCredentialDAO.subjectHasCredential(subject.getEntityId(), savedPassword.getName(), hashService.computeHash(hashRequest)
		        .getBytes()));
	}

	@Transactional
	@Test(expectedExceptions = { ObjectNotFoundException.class })
	public void test_update_ObjectNotFoundException() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		Assert.assertNotNull(savedPassword);
		Assert.assertNotNull(savedPassword.getEntityId());
		Assert.assertNotNull(savedPassword.getSubjecId());
		log.info(savedPassword.toJson());

		final HashedCredential password2 = hashedCredentialDAO.findById(savedPassword.getEntityId());
		Assert.assertNotNull(password2);
		Assert.assertNotNull(password2.getEntityId());
		Assert.assertNotNull(password2.getSubjecId());
		log.info(password2.toJson());

		Assert.assertEquals(password2, savedPassword);

		subjectDao.delete(subject.getEntityId());
		Assert.assertNull(hashedCredentialDAO.findById(savedPassword.getEntityId()));

		hashedCredentialDAO.update(savedPassword);
	}

	@Transactional
	@Test(expectedExceptions = { ObjectNotFoundException.class })
	public void test_update_withUpdatedBy_ObjectNotFoundException() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		Assert.assertNotNull(savedPassword);
		Assert.assertNotNull(savedPassword.getEntityId());
		Assert.assertNotNull(savedPassword.getSubjecId());
		log.info(savedPassword.toJson());

		final HashedCredential password2 = hashedCredentialDAO.findById(savedPassword.getEntityId());
		Assert.assertNotNull(password2);
		Assert.assertNotNull(password2.getEntityId());
		Assert.assertNotNull(password2.getSubjecId());
		log.info(password2.toJson());

		Assert.assertEquals(password2, savedPassword);

		subjectDao.delete(subject.getEntityId());
		Assert.assertNull(hashedCredentialDAO.findById(savedPassword.getEntityId()));

		hashedCredentialDAO.update(savedPassword, UUID.randomUUID());
	}

	@Transactional
	@Test(expectedExceptions = { StaleObjectException.class })
	public void test_update_withUpdatedBy_StaleObjectException() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		Assert.assertNotNull(savedPassword);
		Assert.assertNotNull(savedPassword.getEntityId());
		Assert.assertNotNull(savedPassword.getSubjecId());
		log.info(savedPassword.toJson());

		final HashedCredential password2 = hashedCredentialDAO.findById(savedPassword.getEntityId());
		Assert.assertNotNull(password2);
		Assert.assertNotNull(password2.getEntityId());
		Assert.assertNotNull(password2.getSubjecId());
		log.info(password2.toJson());

		Assert.assertEquals(password2, savedPassword);

		hashedCredentialDAO.update(savedPassword, UUID.randomUUID());
		hashedCredentialDAO.update(savedPassword, UUID.randomUUID());
	}

	@Transactional
	@Test(expectedExceptions = { StaleObjectException.class })
	public void test_update_StaleObjectException() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		Assert.assertNotNull(savedPassword);
		Assert.assertNotNull(savedPassword.getEntityId());
		Assert.assertNotNull(savedPassword.getSubjecId());
		log.info(savedPassword.toJson());

		final HashedCredential password2 = hashedCredentialDAO.findById(savedPassword.getEntityId());
		Assert.assertNotNull(password2);
		Assert.assertNotNull(password2.getEntityId());
		Assert.assertNotNull(password2.getSubjecId());
		log.info(password2.toJson());

		Assert.assertEquals(password2, savedPassword);

		hashedCredentialDAO.update(savedPassword);
		hashedCredentialDAO.update(savedPassword);
	}

	@Transactional
	@Test
	public void test_update() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		final HashRequest hashRequest2 = new HashRequest.Builder()
		        .setSource("password")
		        .setAlgorithmName(savedPassword.getHashAlgorithm())
		        .setIterations(savedPassword.getHashIterations())
		        .setSalt(ByteSource.Util.bytes(savedPassword.getSalt()))
		        .build();

		final Hash hash2 = hashService.computeHash(hashRequest2);
		Assert.assertTrue(Arrays.equals(hash2.getBytes(), savedPassword.getHash()));
		Assert.assertEquals(hash2.toBase64(), ByteSource.Util.bytes(savedPassword.getHash()).toBase64());

		final HashRequest hashRequest3 = new HashRequest.Builder().setSource("password2").build();
		final Hash hash3 = hashService.computeHash(hashRequest3);

		final HashedCredential changedPassword = new HashedCredentialImpl(savedPassword, hash3.getBytes(), hash3.getAlgorithmName(),
		        hash3.getIterations(), hash3.getSalt().getBytes());
		final HashedCredential savedChangedPassword = hashedCredentialDAO.update(changedPassword);

		Assert.assertEquals(savedChangedPassword.getEntityVersion(), 2l);
		Assert.assertTrue(savedChangedPassword.getEntityUpdatedOn() > savedPassword.getEntityUpdatedOn());
		Assert.assertEquals(savedChangedPassword.getEntityCreatedOn(), savedPassword.getEntityCreatedOn());
		Assert.assertNotEquals(savedChangedPassword, savedPassword);
	}

	@Transactional
	@Test
	public void test_update_with_updatedBy() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDao.create(temp);

		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);
		final HashedCredential password = new HashedCredentialImpl(subject.getEntityId(), "password", hash.getBytes(), hash.getAlgorithmName(),
		        hash.getIterations(), hash.getSalt().getBytes());
		final HashedCredential savedPassword = hashedCredentialDAO.create(password);

		final HashRequest hashRequest2 = new HashRequest.Builder()
		        .setSource("password")
		        .setAlgorithmName(savedPassword.getHashAlgorithm())
		        .setIterations(savedPassword.getHashIterations())
		        .setSalt(ByteSource.Util.bytes(savedPassword.getSalt()))
		        .build();

		final Hash hash2 = hashService.computeHash(hashRequest2);
		Assert.assertTrue(Arrays.equals(hash2.getBytes(), savedPassword.getHash()));
		Assert.assertEquals(hash2.toBase64(), ByteSource.Util.bytes(savedPassword.getHash()).toBase64());

		final HashRequest hashRequest3 = new HashRequest.Builder().setSource("password2").build();
		final Hash hash3 = hashService.computeHash(hashRequest3);

		final HashedCredential changedPassword = new HashedCredentialImpl(savedPassword, hash3.getBytes(), hash3.getAlgorithmName(),
		        hash3.getIterations(), hash3.getSalt().getBytes());
		final HashedCredential savedChangedPassword = hashedCredentialDAO.update(changedPassword, UUID.randomUUID());

		Assert.assertEquals(savedChangedPassword.getEntityVersion(), 2l);
		Assert.assertTrue(savedChangedPassword.getEntityUpdatedOn() > savedPassword.getEntityUpdatedOn());
		Assert.assertEquals(savedChangedPassword.getEntityCreatedOn(), savedPassword.getEntityCreatedOn());
		Assert.assertNotEquals(savedChangedPassword, savedPassword);

		final HashedCredential savedChangedPassword2 = hashedCredentialDAO.findById(savedChangedPassword.getEntityId());
		Assert.assertTrue(savedChangedPassword2.getUpdatedByEntityId().isPresent());
		Assert.assertNotNull(savedChangedPassword2.getUpdatedByEntityId().get());
	}
}
