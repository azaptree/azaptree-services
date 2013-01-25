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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;

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
import org.testng.annotations.Test;

@ContextConfiguration(classes = { PostGresqlDataSourceTest.Config.class })
public class PostGresqlDataSourceTest extends AbstractTestNGSpringContextTests {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Configuration
	@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
	public static class Config implements TransactionManagementConfigurer {

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
			ds.setRollbackOnReturn(true);

			return ds;
		}

		@Bean
		JdbcTemplate JdbcTemplate() {
			return new JdbcTemplate(dataSource());
		}

		@Bean
		public org.springframework.jdbc.datasource.DataSourceTransactionManager dataSourceTransactionManager() {
			return new DataSourceTransactionManager(dataSource());
		}

		@Override
		public PlatformTransactionManager annotationDrivenTransactionManager() {
			return dataSourceTransactionManager();
		}

	}

	@Autowired
	private DataSource ds;

	@Autowired
	private JdbcTemplate jdbc;

	@Transactional
	@Test
	public void testConnection() throws SQLException {
		try (final Connection conn = ds.getConnection()) {
			try (final Statement stmt = conn.createStatement()) {
				for (int i = 0; i < 100; i++) {
					try (final ResultSet rs = stmt.executeQuery("select NOW()")) {
						rs.next();
						log.info("testConnection(): NOW() = {}", rs.getTimestamp(1));
					}
				}
			}
		}
	}

	@Transactional
	@Test
	public void testJdbcTemplate() {
		final Timestamp ts = jdbc.queryForObject("select NOW()", Timestamp.class);
		log.info("testJdbcTemplate(): NOW() = {}", ts);
	}

	@Transactional
	public void executeBadSQL() throws SQLException {
		try (final Connection conn = ds.getConnection()) {
			try (final Statement stmt = conn.createStatement()) {
				for (int i = 0; i < 100; i++) {
					try (final ResultSet rs = stmt.executeQuery("select * from asfsdfsdf")) {
					}
				}
			}
		}
	}

	@Test
	public void testTransactionRollback() throws SQLException {
		try {
			executeBadSQL();
		} catch (SQLException e) {
			log.info("EXPECTED EXCEPTION");
		}

		testConnection();
		testJdbcTemplate();
	}
}
