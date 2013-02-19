package test.com.azaptree.services.security.commands.subjectRepository;

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

import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

import org.apache.shiro.crypto.hash.HashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.command.CommandContext;
import com.azaptree.services.security.Credential;
import com.azaptree.services.security.CredentialNames;
import com.azaptree.services.security.UnknownCredentialException;
import com.azaptree.services.security.UnsupportedCredentialTypeException;
import com.azaptree.services.security.commands.subjectRepository.CreateSubject;
import com.azaptree.services.security.config.spring.DatabaseSpringConfiguration;
import com.azaptree.services.security.config.spring.SecurityCredentialsServiceConfig;
import com.azaptree.services.security.dao.HashServiceConfigurationDAO;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.HashedCredential;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.config.impl.HashServiceConfig;
import com.azaptree.services.security.domain.impl.SubjectImpl;
import com.azaptree.services.tests.support.AzaptreeAbstractTestNGSpringContextTests;

@ContextConfiguration(classes = { CreateSubjectTest.Config.class })
@ActiveProfiles({ "local" })
public class CreateSubjectTest extends AzaptreeAbstractTestNGSpringContextTests {

	@Configuration
	@ComponentScan(basePackageClasses = { com.azaptree.services.security.config.spring.local.DatabaseSpringConfiguration.class })
	@Import({ SecurityCredentialsServiceConfig.class })
	public static class Config {
		@Autowired
		private DatabaseSpringConfiguration databaseSpringConfiguration;

		@Bean
		public CreateSubject createSubject() {
			return new CreateSubject(hashServiceConfiguration());
		}

		@Bean
		public HashedCredentialDAO hashedCredentialDAO() {
			return new HashedCredentialDAO(databaseSpringConfiguration.securityServiceJdbcTemplate());
		}

		@Bean
		public HashService hashService() {
			return hashServiceConfiguration().getHashService();
		}

		@Bean
		public HashServiceConfiguration hashServiceConfiguration() {
			final HashServiceConfigurationDAO dao = new HashServiceConfigurationDAO(databaseSpringConfiguration.securityServiceJdbcTemplate());
			final String name = "HashedCredentialDAOTest";
			final HashServiceConfiguration config = dao.findByName(name);
			if (config != null) {
				return config;
			}

			return dao.create(new HashServiceConfig(name));
		}

		@Bean
		public SubjectDAO subjectDao() {
			return new SubjectDAO(databaseSpringConfiguration.securityServiceJdbcTemplate());
		}
	}

	@Autowired
	private CreateSubject createSubject;

	@Autowired
	private SubjectDAO subjectDAO;

	@Autowired
	private HashedCredentialDAO hashedCredentialDAO;

	@Transactional
	@Test
	public void test_createSubject() {
		final CommandContext ctx = new CommandContext();
		final Subject subject = new SubjectImpl(Subject.Status.ACTIVATED);
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
		final Subject createdSubject = ctx.get(CreateSubject.SUBJECT);
		log.info("test_createSubject(): createdSubject : {}", createdSubject);
		Assert.assertNotNull(createdSubject);
		Assert.assertNotNull(createdSubject.getEntityId());
		Assert.assertEquals(createdSubject.getEntityVersion(), 1l);

		final Subject retrievedSubject = subjectDAO.findById(createdSubject.getEntityId());
		Assert.assertNotNull(retrievedSubject);
		Assert.assertEquals(retrievedSubject.getEntityId(), createdSubject.getEntityId());

		final Set<HashedCredential> credentials = hashedCredentialDAO.findBySubjectId(createdSubject.getEntityId());
		Assert.assertNotNull(credentials);
		Assert.assertFalse(credentials.isEmpty());
		Assert.assertEquals(credentials.size(), 1);
		final HashedCredential retrievedCredential = credentials.iterator().next();
		Assert.assertEquals(retrievedCredential.getName(), credential.getName());
		Assert.assertEquals(retrievedCredential.getExpiresOn().get(), credential.getExpiresOn());
	}

	@Transactional
	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void test_createSubject_invalid_createdBy() {
		final CommandContext ctx = new CommandContext();
		final SubjectImpl subject = new SubjectImpl(Subject.Status.ACTIVATED);
		subject.setCreatedBy(UUID.randomUUID());
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
	}

	@Transactional
	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void test_createSubject_with_dup_credential() {
		final CommandContext ctx = new CommandContext();
		final SubjectImpl subject = new SubjectImpl(Subject.Status.ACTIVATED);
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential, credential });

		createSubject.execute(ctx);
	}

	@Transactional
	@Test(expectedExceptions = { UnknownCredentialException.class })
	public void test_createSubject_with_invalid_credential_name() {
		final CommandContext ctx = new CommandContext();
		final SubjectImpl subject = new SubjectImpl(Subject.Status.ACTIVATED);
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential("Invalid name", "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
	}

	@Transactional
	@Test(expectedExceptions = { UnsupportedCredentialTypeException.class })
	public void test_createSubject_with_invalid_credential_type() {
		final CommandContext ctx = new CommandContext();
		final SubjectImpl subject = new SubjectImpl(Subject.Status.ACTIVATED);
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, 5, now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
	}

	@Transactional
	@Test
	public void test_createSubject_with_valid_createdBy() {
		final CommandContext ctx = new CommandContext();
		final Subject subject = new SubjectImpl(Subject.Status.ACTIVATED);
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
		final Subject createdSubject = ctx.get(CreateSubject.SUBJECT);

		final SubjectImpl subject2 = new SubjectImpl(Subject.Status.ACTIVATED);
		subject2.setCreatedBy(createdSubject.getEntityId());
		ctx.put(CreateSubject.SUBJECT, subject2);
		createSubject.execute(ctx);
		final Subject createdSubject2 = ctx.get(CreateSubject.SUBJECT);

		Assert.assertEquals(createdSubject2.getCreatedByEntityId().get(), createdSubject.getEntityId());
	}
}
