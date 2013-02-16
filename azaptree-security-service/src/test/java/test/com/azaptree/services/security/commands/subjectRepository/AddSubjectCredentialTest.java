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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Calendar;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.shiro.crypto.hash.HashService;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.testng.annotations.Test;

import com.azaptree.services.command.CommandContext;
import com.azaptree.services.security.Credential;
import com.azaptree.services.security.CredentialNames;
import com.azaptree.services.security.DuplicateCredentialException;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.UnsupportedCredentialTypeException;
import com.azaptree.services.security.commands.subjectRepository.AddSubjectCredential;
import com.azaptree.services.security.commands.subjectRepository.CreateSubject;
import com.azaptree.services.security.config.spring.SecurityCredentialsServiceConfig;
import com.azaptree.services.security.dao.HashServiceConfigurationDAO;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.HashedCredential;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.config.impl.HashServiceConfig;
import com.azaptree.services.security.domain.impl.SubjectImpl;

@ContextConfiguration(classes = { AddSubjectCredentialTest.Config.class })
public class AddSubjectCredentialTest extends AbstractTestNGSpringContextTests {

	@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
	@Configuration
	@Import(SecurityCredentialsServiceConfig.class)
	public static class Config implements TransactionManagementConfigurer {

		@Bean
		public AddSubjectCredential addSubjectCredential() {
			return new AddSubjectCredential(hashServiceConfiguation());
		}

		@Override
		public PlatformTransactionManager annotationDrivenTransactionManager() {
			return dataSourceTransactionManager();
		}

		@Bean
		public CreateSubject createSubject() {
			return new CreateSubject(hashServiceConfiguation());
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
			return hashServiceConfiguation().getHashService();
		}

		@Bean
		public HashServiceConfiguration hashServiceConfiguation() {
			final HashServiceConfigurationDAO dao = new HashServiceConfigurationDAO(jdbcTemplate());
			final String name = "HashedCredentialDAOTest";
			final HashServiceConfiguration config = dao.findByName(name);
			if (config != null) {
				return config;
			}

			return dao.create(new HashServiceConfig(name));
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
	private AddSubjectCredential addSubjectCredentialCommand;

	@Autowired
	private CreateSubject createSubject;

	@Test
	public void test() {
		final CommandContext ctx = new CommandContext();
		final Subject subject = new SubjectImpl(Subject.Status.ACTIVATED);
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
		final Subject createdSubject = ctx.get(CreateSubject.SUBJECT);

		ctx.clear();
		ctx.put(AddSubjectCredential.SUBJECT_ID, createdSubject.getEntityId());
		final Credential credential2 = new Credential(CredentialNames.FORGOT_PASSWORD_ANSWER_1.credentialName, "buona sera");
		ctx.put(AddSubjectCredential.CREDENTIAL, credential2);

		addSubjectCredentialCommand.execute(ctx);
		final HashedCredential hashedCredential2 = ctx.get(AddSubjectCredential.HASHED_CREDENTIAL);
		Assert.assertNotNull(hashedCredential2);
	}

	@Test(expectedExceptions = { DuplicateCredentialException.class })
	public void test_duplicate_credential_name() {
		final CommandContext ctx = new CommandContext();
		final Subject subject = new SubjectImpl(Subject.Status.ACTIVATED);
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
		final Subject createdSubject = ctx.get(CreateSubject.SUBJECT);

		ctx.clear();
		ctx.put(AddSubjectCredential.SUBJECT_ID, createdSubject.getEntityId());
		final Credential credential2 = new Credential(CredentialNames.PASSWORD.credentialName, "buona sera");
		ctx.put(AddSubjectCredential.CREDENTIAL, credential2);

		addSubjectCredentialCommand.execute(ctx);
		final HashedCredential hashedCredential2 = ctx.get(AddSubjectCredential.HASHED_CREDENTIAL);
		Assert.assertNotNull(hashedCredential2);
	}

	@Test(expectedExceptions = { UnsupportedCredentialTypeException.class })
	public void test_invalid_credential_name() {
		final CommandContext ctx = new CommandContext();
		final Subject subject = new SubjectImpl(Subject.Status.ACTIVATED);
		ctx.put(CreateSubject.SUBJECT, subject);

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
		final Subject createdSubject = ctx.get(CreateSubject.SUBJECT);

		ctx.clear();
		ctx.put(AddSubjectCredential.SUBJECT_ID, createdSubject.getEntityId());
		final Credential credential2 = new Credential("INVALID NAME", "buona sera");
		ctx.put(AddSubjectCredential.CREDENTIAL, credential2);

		addSubjectCredentialCommand.execute(ctx);
	}

	@Test(expectedExceptions = { UnknownSubjectException.class })
	public void test_unknown_subjectId() {
		final CommandContext ctx = new CommandContext();

		ctx.put(AddSubjectCredential.SUBJECT_ID, UUID.randomUUID());
		final Credential credential2 = new Credential(CredentialNames.FORGOT_PASSWORD_ANSWER_1.credentialName, "buona sera");
		ctx.put(AddSubjectCredential.CREDENTIAL, credential2);

		addSubjectCredentialCommand.execute(ctx);
	}

	@Test
	public void test_with_updated_by() {
		final CommandContext ctx = new CommandContext();
		ctx.put(CreateSubject.SUBJECT, new SubjectImpl(Subject.Status.ACTIVATED));

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
		final Subject createdSubject = ctx.get(CreateSubject.SUBJECT);

		ctx.put(CreateSubject.SUBJECT, new SubjectImpl(Subject.Status.ACTIVATED));
		createSubject.execute(ctx);
		final Subject createdSubject2 = ctx.get(CreateSubject.SUBJECT);

		ctx.clear();
		ctx.put(AddSubjectCredential.SUBJECT_ID, createdSubject.getEntityId());
		final Credential credential2 = new Credential(CredentialNames.FORGOT_PASSWORD_ANSWER_1.credentialName, "buona sera");
		ctx.put(AddSubjectCredential.CREDENTIAL, credential2);
		ctx.put(AddSubjectCredential.UPDATED_BY_SUBJECT_ID, createdSubject2.getEntityId());

		addSubjectCredentialCommand.execute(ctx);
		final HashedCredential hashedCredential2 = ctx.get(AddSubjectCredential.HASHED_CREDENTIAL);
		Assert.assertNotNull(hashedCredential2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void test_with_updated_by_same_as_subject_id() {
		final CommandContext ctx = new CommandContext();
		ctx.put(CreateSubject.SUBJECT, new SubjectImpl(Subject.Status.ACTIVATED));

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
		final Subject createdSubject = ctx.get(CreateSubject.SUBJECT);

		ctx.clear();
		ctx.put(AddSubjectCredential.SUBJECT_ID, createdSubject.getEntityId());
		final Credential credential2 = new Credential(CredentialNames.FORGOT_PASSWORD_ANSWER_1.credentialName, "buona sera");
		ctx.put(AddSubjectCredential.CREDENTIAL, credential2);
		ctx.put(AddSubjectCredential.UPDATED_BY_SUBJECT_ID, createdSubject.getEntityId());

		addSubjectCredentialCommand.execute(ctx);
	}

	@Test(expectedExceptions = UnknownSubjectException.class)
	public void test_with_updated_by_not_found() {
		final CommandContext ctx = new CommandContext();
		ctx.put(CreateSubject.SUBJECT, new SubjectImpl(Subject.Status.ACTIVATED));

		final Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, 90);
		final Credential credential = new Credential(CredentialNames.PASSWORD.credentialName, "secret", now.getTime());
		ctx.put(CreateSubject.CREDENTIALS, new Credential[] { credential });

		createSubject.execute(ctx);
		final Subject createdSubject = ctx.get(CreateSubject.SUBJECT);

		ctx.clear();
		ctx.put(AddSubjectCredential.SUBJECT_ID, createdSubject.getEntityId());
		final Credential credential2 = new Credential(CredentialNames.FORGOT_PASSWORD_ANSWER_1.credentialName, "buona sera");
		ctx.put(AddSubjectCredential.CREDENTIAL, credential2);
		ctx.put(AddSubjectCredential.UPDATED_BY_SUBJECT_ID, UUID.randomUUID());

		addSubjectCredentialCommand.execute(ctx);
	}
}
