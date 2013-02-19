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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import com.azaptree.services.tests.support.AzaptreeAbstractTestNGSpringContextTests;

@ContextConfiguration(classes = { PostGresqlDataSourceTest.Config.class })
public class PostGresqlDataSourceTest extends AzaptreeAbstractTestNGSpringContextTests {
	@Configuration
	@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
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
			ds.setCommitOnReturn(true);

			return ds;
		}

		@Bean
		public org.springframework.jdbc.datasource.DataSourceTransactionManager dataSourceTransactionManager() {
			return new DataSourceTransactionManager(new LazyConnectionDataSourceProxy(dataSource()));
		}

		@Bean
		JdbcTemplate JdbcTemplate() {
			return new JdbcTemplate(dataSource());
		}

	}

	@Autowired
	private JdbcTemplate jdbc;

	@Transactional
	public void executeBadSQL() {
		try {
			jdbc.update("delete from asfsdfsdf");
		} catch (final Exception e) {
			throw new RuntimeException();
		}
	}

	@Transactional(readOnly = true)
	public void executeGoodSQL() {
		try {
			jdbc.query("select NOW()", new RowCallbackHandler() {

				@Override
				public void processRow(final ResultSet rs) throws SQLException {
					log.info("testConnection(): NOW() = {}", rs.getTimestamp(1));
				}
			});
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	@Test
	public void testConnection() {
		try {
			for (int i = 0; i < 100; i++) {
				jdbc.query("select NOW()", new RowCallbackHandler() {

					@Override
					public void processRow(final ResultSet rs) throws SQLException {
						log.info("testConnection(): NOW() = {}", rs.getTimestamp(1));
					}
				});
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	@Test
	public void testJdbcTemplate() {
		final Timestamp ts = jdbc.queryForObject("select NOW()", Timestamp.class);
		log.info("testJdbcTemplate(): NOW() = {}", ts);
	}

	@Test
	public void testTransactionRollback() {
		try {
			executeBadSQL();
		} catch (final Exception e) {
			log.info("testTransactionRollback() : ignoring exception from executeBadSQL() - transaction should have been rolled back");
		}

		executeGoodSQL();
	}
}
