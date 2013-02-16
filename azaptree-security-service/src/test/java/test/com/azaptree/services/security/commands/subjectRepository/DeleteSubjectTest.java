package test.com.azaptree.services.security.commands.subjectRepository;

import java.util.Calendar;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.shiro.crypto.hash.HashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.command.CommandContext;
import com.azaptree.services.security.Credential;
import com.azaptree.services.security.CredentialNames;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.commands.subjectRepository.CreateSubject;
import com.azaptree.services.security.commands.subjectRepository.DeleteSubject;
import com.azaptree.services.security.config.spring.SecurityCredentialsServiceConfig;
import com.azaptree.services.security.dao.HashServiceConfigurationDAO;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.Subject;
import com.azaptree.services.security.domain.config.HashServiceConfiguration;
import com.azaptree.services.security.domain.config.impl.HashServiceConfig;
import com.azaptree.services.security.domain.impl.SubjectImpl;

@ContextConfiguration(classes = { DeleteSubjectTest.Config.class })
public class DeleteSubjectTest extends AbstractTestNGSpringContextTests {

	@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
	@Configuration
	@Import({ SecurityCredentialsServiceConfig.class })
	public static class Config implements TransactionManagementConfigurer {
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
		public DeleteSubject deleteSubject() {
			return new DeleteSubject();
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

	final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private CreateSubject createSubject;

	@Autowired
	private DeleteSubject deleteSubject;

	@Autowired
	private SubjectDAO subjectDAO;

	@Test
	public void test_delete() {
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

		ctx.clear();
		ctx.setAttribute(DeleteSubject.SUBJECT_ID, createdSubject.getEntityId());
		deleteSubject.execute(ctx);

		Assert.assertFalse(subjectDAO.exists(createdSubject.getEntityId()));
	}

	@Test(expectedExceptions = { UnknownSubjectException.class })
	public void test_delete_unknown_subject() {
		final CommandContext ctx = new CommandContext();
		ctx.setAttribute(DeleteSubject.SUBJECT_ID, UUID.randomUUID());
		deleteSubject.execute(ctx);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void test_delete_without_subject_id() {
		final CommandContext ctx = new CommandContext();
		deleteSubject.execute(ctx);
	}

}
