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
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.util.ByteSource;
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

import com.azaptree.services.security.dao.HashServiceConfigurationDAO;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.HashedCredential;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.Subject.Status;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.config.impl.HashServiceConfig;
import com.azaptree.services.security.domain.impl.HashedCredentialImpl;
import com.azaptree.services.security.domain.impl.SubjectImpl;

@ContextConfiguration(classes = { HashServiceConfigurationDAOTest.Config.class })
public class HashServiceConfigurationDAOTest extends AbstractTestNGSpringContextTests {

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
		public HashServiceConfigurationDAO hashServiceConfigurationDAO() {
			return new HashServiceConfigurationDAO(jdbcTemplate());
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

	@Autowired
	private HashServiceConfigurationDAO dao;

	@Autowired
	private SubjectDAO subjectDao;

	@Autowired
	private HashedCredentialDAO hashedCredentialDAO;

	@Transactional
	@Test
	public void test_create() {
		final HashServiceConfig config = new HashServiceConfig("test_create" + UUID.randomUUID());
		final HashServiceConfiguration savedConfig = dao.create(config);

		final HashServiceConfiguration config2 = dao.findById(savedConfig.getEntityId());
		Assert.assertNotNull(config2);
		Assert.assertEquals(config2, savedConfig);

		final HashService hashService = config2.createHashService();
		final HashRequest hashRequest = new HashRequest.Builder().setSource("password").build();
		final Hash hash = hashService.computeHash(hashRequest);

		final Subject subject = subjectDao.create(new SubjectImpl(Status.ACTIVATED));
		final HashedCredential cred = hashedCredentialDAO.create(new HashedCredentialImpl(subject.getEntityId(), "password", savedConfig.getEntityId(), hash
		        .getBytes(), hash.getAlgorithmName(), hash.getIterations(), hash.getSalt().getBytes(), null));

		final HashRequest hashRequest2 = new HashRequest.Builder().setSource("password")
		        .setAlgorithmName(cred.getHashAlgorithm())
		        .setIterations(cred.getHashIterations())
		        .setSalt(ByteSource.Util.bytes(cred.getSalt()))
		        .build();
		final Hash hash2 = hashService.computeHash(hashRequest2);
		Assert.assertTrue(Arrays.equals(hash2.getBytes(), cred.getHash()));
	}

	@Transactional
	@Test(expectedExceptions = { UnsupportedOperationException.class })
	public void test_update() {
		final HashServiceConfig config = new HashServiceConfig("test_create" + UUID.randomUUID());
		final HashServiceConfiguration savedConfig = dao.update(config);
	}

	@Transactional
	@Test
	public void test_delete() {
		final HashServiceConfig config = new HashServiceConfig("test_create" + UUID.randomUUID());
		final HashServiceConfiguration savedConfig = dao.create(config);

		final HashServiceConfiguration config2 = dao.findById(savedConfig.getEntityId());
		Assert.assertNotNull(config2);
		Assert.assertEquals(config2, savedConfig);

		Assert.assertTrue(dao.delete(config2.getEntityId()));
		Assert.assertFalse(dao.exists(savedConfig.getEntityId()));
	}
}
