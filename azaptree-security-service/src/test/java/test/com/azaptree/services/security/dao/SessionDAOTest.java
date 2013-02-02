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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
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

import com.azaptree.services.security.dao.SessionDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.Session;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.Subject.Status;
import com.azaptree.services.security.domain.impl.SessionImpl;
import com.azaptree.services.security.domain.impl.SubjectImpl;

@ContextConfiguration(classes = { SessionDAOTest.Config.class })
public class SessionDAOTest extends AbstractTestNGSpringContextTests {
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
		public SessionDAO sessionDao() {
			return new SessionDAO(jdbcTemplate());
		}

		@Bean
		public SubjectDAO subjectDao() {
			return new SubjectDAO(jdbcTemplate());
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SessionDAO sessionDAO;

	@Autowired
	private SubjectDAO subjectDAO;

	@Test
	@Transactional
	public void test_deleteSessionsBySubjectId() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		sessionDAO.deleteSessionsBySubjectId(subject.getEntityId());
		Assert.assertNull(sessionDAO.findById(session.getEntityId()));
	}

	@Test
	@Transactional
	public void test_getSessionIdsBySubject() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		final UUID[] uuids = sessionDAO.getSessionIdsBySubject(subject.getEntityId());
		Assert.assertEquals(uuids.length, 1);
		Assert.assertTrue(ArrayUtils.contains(uuids, session.getEntityId()));
	}

	@Test
	@Transactional
	public void test_touchSession() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		Assert.assertTrue(sessionDAO.touchSession(session.getEntityId()));
		final Session session3 = sessionDAO.findById(session.getEntityId());
		Assert.assertTrue(session3.getLastAccessedOn() > session2.getLastAccessedOn());
	}

	@Transactional
	@Test
	public void testCascadeDelete() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		subjectDAO.delete(subject.getEntityId());
		Assert.assertNull(sessionDAO.findById(session.getEntityId()));
	}

	@Transactional
	@Test
	public void testCreate() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);
	}

	@Transactional
	@Test
	public void testCreateWithHost() throws UnknownHostException {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId(), 1800, InetAddress.getLocalHost().getHostAddress()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		log.info("session2: {}", session2);
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);
	}

	@Transactional
	@Test
	public void testDelete() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		sessionDAO.delete(session.getEntityId());
		Assert.assertNull(sessionDAO.findById(session.getEntityId()));
	}

	@Test(expectedExceptions = { UnsupportedOperationException.class })
	public void testUpdateNotSupported() {
		sessionDAO.update(new SessionImpl(UUID.randomUUID()));
	}

}
