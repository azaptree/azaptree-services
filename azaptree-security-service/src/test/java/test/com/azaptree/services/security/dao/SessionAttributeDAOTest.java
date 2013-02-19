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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.json.JsonUtils;
import com.azaptree.services.security.dao.SessionAttributeDAO;
import com.azaptree.services.security.dao.SessionDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.Session;
import com.azaptree.services.security.domain.SessionAttribute;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.Subject.Status;
import com.azaptree.services.security.domain.impl.SessionAttributeImpl;
import com.azaptree.services.security.domain.impl.SessionImpl;
import com.azaptree.services.security.domain.impl.SubjectImpl;
import com.azaptree.services.tests.support.AzaptreeAbstractTestNGSpringContextTests;

@ContextConfiguration(classes = { SessionAttributeDAOTest.Config.class })
public class SessionAttributeDAOTest extends AzaptreeAbstractTestNGSpringContextTests {

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
		public SessionAttributeDAO sessionAttributeDAO() {
			return new SessionAttributeDAO(jdbcTemplate());
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

	@Autowired
	private SessionDAO sessionDAO;

	@Autowired
	private SubjectDAO subjectDAO;

	@Autowired
	private SessionAttributeDAO sessionAttrDao;

	@Transactional
	@Test
	public void test_create() throws IOException {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		final SessionAttribute attr = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_1", session.toJson()));
		log.info("attr : {}", attr);
		Assert.assertNotNull(attr);

		final SessionAttribute attr2 = sessionAttrDao.findById(attr.getEntityId());
		Assert.assertNotNull(attr2);
		Assert.assertEquals(attr2, attr);

		final Map<String, Object> attrValue = JsonUtils.parse(new ByteArrayInputStream(attr.getJson().getBytes()));
		log.info("attrValue serialized back to JSON : {}", JsonUtils.serialize(attrValue));
	}

	@Transactional
	@Test(expectedExceptions = { DataIntegrityViolationException.class })
	public void test_create_invalid_json() {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_1", "invalid json"));
	}

	@Transactional
	@Test
	public void test_delete() throws IOException {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		final SessionAttribute attr = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_1", session.toJson()));
		log.info("attr : {}", attr);
		Assert.assertNotNull(attr);

		final SessionAttribute attr2 = sessionAttrDao.findById(attr.getEntityId());
		Assert.assertNotNull(attr2);
		Assert.assertEquals(attr2, attr);

		final Map<String, Object> attrValue = JsonUtils.parse(new ByteArrayInputStream(attr.getJson().getBytes()));
		log.info("attrValue serialized back to JSON : {}", JsonUtils.serialize(attrValue));

		sessionAttrDao.delete(attr2.getEntityId());
		Assert.assertNull(sessionAttrDao.findById(attr.getEntityId()));
	}

	@Transactional
	@Test
	public void test_getSessionAttributeKeys() throws IOException {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		final SessionAttribute attr = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_1", session.toJson()));
		log.info("attr : {}", attr);
		Assert.assertNotNull(attr);

		final SessionAttribute attr2 = sessionAttrDao.findById(attr.getEntityId());
		Assert.assertNotNull(attr2);
		Assert.assertEquals(attr2, attr);

		final Map<String, Object> attrValue = JsonUtils.parse(new ByteArrayInputStream(attr.getJson().getBytes()));
		log.info("attrValue serialized back to JSON : {}", JsonUtils.serialize(attrValue));

		List<String> keys = sessionAttrDao.getAttributeKeys(session.getEntityId());
		Assert.assertFalse(keys.isEmpty());
		Assert.assertEquals(keys.size(), 1);
		Assert.assertTrue(keys.contains(attr.getName()));

		final SessionAttribute attr3 = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_3", session.toJson()));
		keys = sessionAttrDao.getAttributeKeys(session.getEntityId());
		Assert.assertFalse(keys.isEmpty());
		Assert.assertEquals(keys.size(), 2);
		Assert.assertTrue(keys.contains(attr.getName()));
		Assert.assertTrue(keys.contains(attr3.getName()));
	}

	@Transactional
	@Test
	public void test_getSessionAttributes() throws IOException {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		final SessionAttribute attr = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_1", session.toJson()));
		log.info("attr : {}", attr);
		Assert.assertNotNull(attr);

		final SessionAttribute attr2 = sessionAttrDao.findById(attr.getEntityId());
		Assert.assertNotNull(attr2);
		Assert.assertEquals(attr2, attr);

		final Map<String, Object> attrValue = JsonUtils.parse(new ByteArrayInputStream(attr.getJson().getBytes()));
		log.info("attrValue serialized back to JSON : {}", JsonUtils.serialize(attrValue));

		List<String> keys = sessionAttrDao.getAttributeKeys(session.getEntityId());
		Assert.assertFalse(keys.isEmpty());
		Assert.assertEquals(keys.size(), 1);
		Assert.assertTrue(keys.contains(attr.getName()));

		final SessionAttribute attr3 = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_3", session.toJson()));
		keys = sessionAttrDao.getAttributeKeys(session.getEntityId());
		Assert.assertFalse(keys.isEmpty());
		Assert.assertEquals(keys.size(), 2);
		Assert.assertTrue(keys.contains(attr.getName()));
		Assert.assertTrue(keys.contains(attr3.getName()));

		Assert.assertTrue(sessionAttrDao.setAttribute(session.getEntityId(), attr3.getName(), attr2.toJson()));
		final Map<String, Object> attr3Map = JsonUtils.parse(new ByteArrayInputStream(sessionAttrDao.findById(attr3.getEntityId()).getJson().getBytes()));
		Assert.assertEquals(attr3Map.get("entityId"), attr2.getEntityId().toString());

		Assert.assertTrue(sessionAttrDao.setAttribute(session.getEntityId(), "key_4", attr3.toJson()));
		final String attr4Jsonvalue = sessionAttrDao.getAttributeJsonValue(session.getEntityId(), "key_4");
		final Map<String, Object> attr4Map = JsonUtils.parse(new ByteArrayInputStream(attr4Jsonvalue.getBytes()));
		Assert.assertEquals(attr4Map.get("entityId"), attr3.getEntityId().toString());

		final Map<String, SessionAttribute> attrs = sessionAttrDao.getSessionAttributes(session.getEntityId());
		Assert.assertNotNull(attrs);

		for (final String key : sessionAttrDao.getAttributeKeys(session.getEntityId())) {
			Assert.assertNotNull(attrs.get(key));
			Assert.assertEquals(attrs.get(key).getName(), key);
		}
	}

	@Transactional
	@Test
	public void test_removeAttribute() throws IOException {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		final SessionAttribute attr = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_1", session.toJson()));
		log.info("attr : {}", attr);
		Assert.assertNotNull(attr);

		final SessionAttribute attr2 = sessionAttrDao.findById(attr.getEntityId());
		Assert.assertNotNull(attr2);
		Assert.assertEquals(attr2, attr);

		final Map<String, Object> attrValue = JsonUtils.parse(new ByteArrayInputStream(attr.getJson().getBytes()));
		log.info("attrValue serialized back to JSON : {}", JsonUtils.serialize(attrValue));

		List<String> keys = sessionAttrDao.getAttributeKeys(session.getEntityId());
		Assert.assertFalse(keys.isEmpty());
		Assert.assertEquals(keys.size(), 1);
		Assert.assertTrue(keys.contains(attr.getName()));

		final SessionAttribute attr3 = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_3", session.toJson()));
		keys = sessionAttrDao.getAttributeKeys(session.getEntityId());
		Assert.assertFalse(keys.isEmpty());
		Assert.assertEquals(keys.size(), 2);
		Assert.assertTrue(keys.contains(attr.getName()));
		Assert.assertTrue(keys.contains(attr3.getName()));

		Assert.assertTrue(sessionAttrDao.removeAttribute(session.getEntityId(), attr3.getName()));
		Assert.assertNull(sessionAttrDao.findById(attr3.getEntityId()));
	}

	@Transactional
	@Test
	public void test_setAttribute() throws IOException {
		final Subject temp = new SubjectImpl(Status.ACTIVATED);
		final Subject subject = subjectDAO.create(temp);

		final Session session = sessionDAO.create(new SessionImpl(subject.getEntityId()));
		Assert.assertNotNull(session);

		final Session session2 = sessionDAO.findById(session.getEntityId());
		Assert.assertNotNull(session2);
		Assert.assertEquals(session2, session);

		final SessionAttribute attr = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_1", session.toJson()));
		log.info("attr : {}", attr);
		Assert.assertNotNull(attr);

		final SessionAttribute attr2 = sessionAttrDao.findById(attr.getEntityId());
		Assert.assertNotNull(attr2);
		Assert.assertEquals(attr2, attr);

		final Map<String, Object> attrValue = JsonUtils.parse(new ByteArrayInputStream(attr.getJson().getBytes()));
		log.info("attrValue serialized back to JSON : {}", JsonUtils.serialize(attrValue));

		List<String> keys = sessionAttrDao.getAttributeKeys(session.getEntityId());
		Assert.assertFalse(keys.isEmpty());
		Assert.assertEquals(keys.size(), 1);
		Assert.assertTrue(keys.contains(attr.getName()));

		final SessionAttribute attr3 = sessionAttrDao.create(new SessionAttributeImpl(session.getEntityId(), "key_3", session.toJson()));
		keys = sessionAttrDao.getAttributeKeys(session.getEntityId());
		Assert.assertFalse(keys.isEmpty());
		Assert.assertEquals(keys.size(), 2);
		Assert.assertTrue(keys.contains(attr.getName()));
		Assert.assertTrue(keys.contains(attr3.getName()));

		Assert.assertTrue(sessionAttrDao.setAttribute(session.getEntityId(), attr3.getName(), attr2.toJson()));
		final Map<String, Object> attr3Map = JsonUtils.parse(new ByteArrayInputStream(sessionAttrDao.findById(attr3.getEntityId()).getJson().getBytes()));
		Assert.assertEquals(attr3Map.get("entityId"), attr2.getEntityId().toString());

		Assert.assertTrue(sessionAttrDao.setAttribute(session.getEntityId(), "key_4", attr3.toJson()));
		final String attr4Jsonvalue = sessionAttrDao.getAttributeJsonValue(session.getEntityId(), "key_4");
		final Map<String, Object> attr4Map = JsonUtils.parse(new ByteArrayInputStream(attr4Jsonvalue.getBytes()));
		Assert.assertEquals(attr4Map.get("entityId"), attr3.getEntityId().toString());
	}

	@Test(expectedExceptions = { UnsupportedOperationException.class })
	public void testUpdateNotSupported() {
		sessionDAO.update(new SessionImpl(UUID.randomUUID()));
	}
}
