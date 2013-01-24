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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.domain.entity.dao.ObjectNotFoundException;
import com.azaptree.services.domain.entity.dao.StaleObjectException;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.impl.SubjectImpl;

@ContextConfiguration(classes = SubjectDAOTest.Config.class)
public class SubjectDAOTest extends AbstractTestNGSpringContextTests {
	final Logger log = LoggerFactory.getLogger(getClass());

	@Configuration
	public static class Config {
		@Bean(destroyMethod = "close")
		public DataSource dataSource() {
			org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
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

			return ds;
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
	public void test_create_findById_delete() {
		final long now = System.currentTimeMillis();
		final Subject temp = new SubjectImpl();
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
	public void test_update() {
		final Subject temp = new SubjectImpl();
		final Subject subject = subjectDao.create(temp);
		final Subject updatedSubject = subjectDao.update(subject);
		Assert.assertNotNull(updatedSubject);
		Assert.assertNotEquals(updatedSubject.getEntityUpdatedOn(), subject.getEntityUpdatedOn());
		Assert.assertNotEquals(updatedSubject.getEntityVersion(), subject.getEntityVersion());

		log.info("subject : {}", subject);
		log.info("updatedSubject : {}", updatedSubject);
	}

	@Transactional
	@Test(expectedExceptions = StaleObjectException.class)
	public void test_update_staleObject() {
		final Subject temp = new SubjectImpl();
		final SubjectImpl subject = (SubjectImpl) subjectDao.create(temp);
		subject.updated();
		subjectDao.update(subject);
	}

	@Transactional
	@Test(expectedExceptions = ObjectNotFoundException.class)
	public void test_update_objectNotFound() {
		final SubjectImpl subject = new SubjectImpl();
		subject.created();
		subjectDao.update(subject);
	}

}
