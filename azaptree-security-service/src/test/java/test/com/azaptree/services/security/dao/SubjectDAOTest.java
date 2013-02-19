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

import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.domain.entity.dao.ObjectNotFoundException;
import com.azaptree.services.domain.entity.dao.Page;
import com.azaptree.services.domain.entity.dao.SearchResults;
import com.azaptree.services.domain.entity.dao.SortField;
import com.azaptree.services.domain.entity.dao.StaleObjectException;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.Subject.Status;
import com.azaptree.services.security.domain.impl.SubjectImpl;
import com.azaptree.services.tests.support.AzaptreeAbstractTestNGSpringContextTests;

@ContextConfiguration(classes = SubjectDAOTest.Config.class)
public class SubjectDAOTest extends AzaptreeAbstractTestNGSpringContextTests {

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
		public JdbcTemplate jdbcTemplate() {
			return new JdbcTemplate(dataSource());
		}

		@Bean
		public SubjectDAO subjectDao() {
			return new SubjectDAO(jdbcTemplate());
		}
	}

	@Autowired
	private SubjectDAO subjectDao;

	@Transactional
	@Test
	public void test_authenticationFailedCounter() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		Subject subject = subjectDao.create(temp);
		log.info("test_authenticationFailedCounter(): subject : {}", subject);

		final long afterCreateTs = System.currentTimeMillis();
		subject.incrementConsecutiveAuthenticationFailedCount();
		subject = subjectDao.update(subject);
		Assert.assertEquals(subject.getConsecutiveAuthenticationFailedCount(), 1);
		Assert.assertTrue(subject.getLastTimeAuthenticationFailed() >= afterCreateTs);

		Subject retrievedSubject = subjectDao.findById(subject.getEntityId());
		Assert.assertEquals(retrievedSubject.getConsecutiveAuthenticationFailedCount(), 1);
		Assert.assertTrue(retrievedSubject.getLastTimeAuthenticationFailed() >= afterCreateTs);
		Assert.assertEquals(retrievedSubject.getLastTimeAuthenticationFailed(), subject.getLastTimeAuthenticationFailed());

		final long lastTimeAuthenicationFailed = retrievedSubject.getLastTimeAuthenticationFailed();
		retrievedSubject.resetConsecutiveAuthenticationFailedCount();
		subject = subjectDao.update(retrievedSubject);
		Assert.assertEquals(subject.getConsecutiveAuthenticationFailedCount(), 0);
		Assert.assertEquals(subject.getLastTimeAuthenticationFailed(), lastTimeAuthenicationFailed);

		retrievedSubject = subjectDao.findById(subject.getEntityId());
		Assert.assertEquals(retrievedSubject.getConsecutiveAuthenticationFailedCount(), 0);
		Assert.assertEquals(retrievedSubject.getLastTimeAuthenticationFailed(), lastTimeAuthenicationFailed);
	}

	@Transactional
	@Test
	public void test_create_findById_delete() {
		final long now = System.currentTimeMillis();
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		final Subject subject = subjectDao.create(temp);
		stopWatch.stop();
		log.info("create time: {}", stopWatch.getTime());
		stopWatch.reset();
		Assert.assertNotNull(subject.getEntityId());
		Assert.assertTrue(subject.getEntityCreatedOn() >= now);

		log.info("subject: {}", subject);

		stopWatch.start();
		final Subject subject2 = subjectDao.findById(subject.getEntityId());
		stopWatch.stop();
		log.info("findById time: {}", stopWatch.getTime());
		stopWatch.reset();
		Assert.assertNotNull(subject2);
		Assert.assertEquals(subject2, subject);

		log.info("subject2: {}", subject2);

		Assert.assertNull(subjectDao.findById(UUID.randomUUID()));

		stopWatch.start();
		Assert.assertTrue(subjectDao.delete(subject.getEntityId()));
		stopWatch.stop();
		log.info("delete time: {}", stopWatch.getTime());
		Assert.assertNull(subjectDao.findById(subject.getEntityId()));
	}

	@Transactional
	@Test
	public void test_findAll() {
		for (int i = 0; i < 500; i++) {
			subjectDao.create(new SubjectImpl(Status.ACTIVATED));
		}

		final long totalCount = subjectDao.getTotalCount();
		// Records may have been inserted outside of this test
		Assert.assertTrue(totalCount >= 500);

		for (int i = 0; i < 3; i++) {
			final SearchResults<Subject> searchResults = subjectDao.findAll(new Page(i, 20));
			Assert.assertEquals(searchResults.getReturnCount(), 20);
			Assert.assertEquals(searchResults.getData().size(), 20);
			Assert.assertTrue(searchResults.getTotalCount() >= 500);

			for (final Subject subject : searchResults.getData()) {
				log.info(subject.toJson());
			}
		}
	}

	@Transactional
	@Test
	public void test_findAll_sorted() {
		for (int i = 0; i < 500; i++) {
			subjectDao.create(new SubjectImpl(Status.ACTIVATED), UUID.randomUUID());
		}

		final long totalCount = subjectDao.getTotalCount();
		// Records may have been inserted outside of this test
		Assert.assertTrue(totalCount >= 500);

		for (int i = 0; i < 3; i++) {
			final SearchResults<Subject> searchResults = subjectDao.findAll(new Page(i, 20));
			Assert.assertEquals(searchResults.getReturnCount(), 20);
			Assert.assertEquals(searchResults.getData().size(), 20);
			Assert.assertTrue(searchResults.getTotalCount() >= 500);

			for (final Subject subject : searchResults.getData()) {
				log.info(subject.toJson());
			}
		}

		for (int i = 0; i < 3; i++) {
			final SearchResults<Subject> searchResults = subjectDao.findAll(new Page(i, 20), new SortField("EntityId", true));
			Assert.assertEquals(searchResults.getReturnCount(), 20);
			Assert.assertEquals(searchResults.getData().size(), 20);
			Assert.assertTrue(searchResults.getTotalCount() >= 500);

			Subject prev = null;
			for (final Subject subject : searchResults.getData()) {
				log.info(subject.toJson());
				if (prev != null) {
					Assert.assertTrue(subject.getEntityId().compareTo(prev.getEntityId()) > 0);
				}
				prev = subject;
			}
		}
	}

	/**
	 * Test that a Subject can be created after a transaction is rolled back
	 */
	@Transactional
	@Test
	public void test_objectNotFound_create() {
		try {
			final SubjectImpl subject = new SubjectImpl(Status.ACTIVATED);
			subject.created();
			subjectDao.update(subject);
		} catch (final ObjectNotFoundException e) {
			log.info("test_objectNotFound_create(): ignoring ObjectNotFoundException");
		}

		test_update();
	}

	/**
	 * Test that a Subject can be created after a transaction is rolled back
	 */
	@Test
	public void test_objectNotFound_create2() {
		try {
			test_update_staleObject();
		} catch (final StaleObjectException e) {
			log.info("test_objectNotFound_create2(): ignoring {}", e.getClass().getName());
		}

		test_update();
	}

	@Transactional
	@Test
	public void test_statusTimestamp() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		Subject subject = subjectDao.create(temp);
		log.info("test_authenticationFailedCounter(): subject : {}", subject);

		final long afterCreateTs = System.currentTimeMillis();
		subject.setStatus(Status.LOCKED);
		subject = subjectDao.update(subject);
		Assert.assertEquals(subject.getStatus(), Status.LOCKED);
		Assert.assertTrue(subject.getStatusTimestamp() >= afterCreateTs);

		final Subject retrievedSubject = subjectDao.findById(subject.getEntityId());
		Assert.assertEquals(retrievedSubject.getStatus(), Status.LOCKED);
		Assert.assertTrue(retrievedSubject.getStatusTimestamp() >= afterCreateTs);
	}

	@Transactional
	@Test
	public void test_touch() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED, 3);
		final Subject subject = subjectDao.create(temp);
		final Subject updatedSubject = subjectDao.update(subject);
		Assert.assertNotNull(updatedSubject);
		Assert.assertNotEquals(updatedSubject.getEntityUpdatedOn(), subject.getEntityUpdatedOn());
		Assert.assertNotEquals(updatedSubject.getEntityVersion(), subject.getEntityVersion());

		log.info("subject : {}", subject);
		log.info("updatedSubject : {}", updatedSubject);

		subjectDao.touch(updatedSubject.getEntityId());

		final Subject updatedSubject2 = subjectDao.findById(updatedSubject.getEntityId());
		Assert.assertNotNull(updatedSubject2);
		Assert.assertNotEquals(updatedSubject.getEntityUpdatedOn(), updatedSubject2.getEntityUpdatedOn());
		Assert.assertNotEquals(updatedSubject.getEntityVersion(), updatedSubject2.getEntityVersion());
		Assert.assertTrue(updatedSubject2.getEntityVersion() > updatedSubject.getEntityVersion());
		Assert.assertFalse(updatedSubject2.getUpdatedByEntityId().isPresent());
	}

	@Transactional
	@Test
	public void test_touch_withUpdatedBy() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED, 3);
		final Subject subject = subjectDao.create(temp);
		final Subject updatedSubject = subjectDao.update(subject);
		Assert.assertNotNull(updatedSubject);
		Assert.assertNotEquals(updatedSubject.getEntityUpdatedOn(), subject.getEntityUpdatedOn());
		Assert.assertNotEquals(updatedSubject.getEntityVersion(), subject.getEntityVersion());

		log.info("subject : {}", subject);
		log.info("updatedSubject : {}", updatedSubject);

		final UUID updatedBy = UUID.randomUUID();
		subjectDao.touch(updatedSubject.getEntityId(), updatedBy);

		final Subject updatedSubject2 = subjectDao.findById(updatedSubject.getEntityId());
		Assert.assertNotNull(updatedSubject2);
		Assert.assertNotEquals(updatedSubject.getEntityUpdatedOn(), updatedSubject2.getEntityUpdatedOn());
		Assert.assertNotEquals(updatedSubject.getEntityVersion(), updatedSubject2.getEntityVersion());
		Assert.assertTrue(updatedSubject2.getEntityVersion() > updatedSubject.getEntityVersion());
		Assert.assertEquals(updatedSubject2.getUpdatedByEntityId().get(), updatedBy);
	}

	@Transactional
	@Test
	public void test_update() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED, 3);
		final Subject subject = subjectDao.create(temp);
		final Subject updatedSubject = subjectDao.update(subject);
		Assert.assertNotNull(updatedSubject);
		Assert.assertNotEquals(updatedSubject.getEntityUpdatedOn(), subject.getEntityUpdatedOn());
		Assert.assertNotEquals(updatedSubject.getEntityVersion(), subject.getEntityVersion());

		log.info("subject : {}", subject);
		log.info("updatedSubject : {}", updatedSubject);

		final Subject updatedSubject2 = subjectDao.update(updatedSubject, UUID.randomUUID());
		Assert.assertNotNull(updatedSubject2);
		Assert.assertNotEquals(updatedSubject.getEntityUpdatedOn(), updatedSubject2.getEntityUpdatedOn());
		Assert.assertNotEquals(updatedSubject.getEntityVersion(), updatedSubject2.getEntityVersion());
		Assert.assertTrue(updatedSubject2.getEntityVersion() > updatedSubject.getEntityVersion());
	}

	@Transactional
	@Test(expectedExceptions = ObjectNotFoundException.class)
	public void test_update_objectNotFound() {
		final SubjectImpl subject = new SubjectImpl(Status.ACTIVATED);
		subject.created();
		subjectDao.update(subject);
	}

	@Transactional
	@Test(expectedExceptions = StaleObjectException.class)
	public void test_update_staleObject() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final SubjectImpl subject = (SubjectImpl) subjectDao.create(temp);
		subject.updated();
		subjectDao.update(subject);
	}

	@Transactional
	@Test(expectedExceptions = ObjectNotFoundException.class)
	public void test_update_withCreatedBy_objectNotFound() {
		final SubjectImpl subject = new SubjectImpl(Status.ACTIVATED);
		subject.created();
		subjectDao.update(subject, UUID.randomUUID());
	}

	@Transactional
	@Test(expectedExceptions = StaleObjectException.class)
	public void test_update_withCreatedBy_staleObject() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final SubjectImpl subject = (SubjectImpl) subjectDao.create(temp);
		subject.updated();
		subjectDao.update(subject, UUID.randomUUID());
	}
}
