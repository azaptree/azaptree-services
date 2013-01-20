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
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import com.jolbox.bonecp.BoneCPDataSource;

@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@ContextConfiguration(classes = { PostGresqlDataSourceTest.Config.class })
public class PostGresqlDataSourceTest extends AbstractTestNGSpringContextTests {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Configuration
	public static class Config {

		@Bean(destroyMethod = "close")
		public DataSource dataSource() {
			final BoneCPDataSource ds = new BoneCPDataSource();
			ds.setDriverClass("org.postgresql.Driver");
			ds.setJdbcUrl("jdbc:postgresql://localhost:5433/azaptree");
			ds.setUsername("azaptree");
			ds.setPassword("!azaptree");

			ds.setInitSQL("set search_path to azaptree");

			ds.setIdleMaxAge(5, TimeUnit.MINUTES);
			ds.setMaxConnectionAge(1, TimeUnit.HOURS);

			ds.setConnectionTestStatement("SELECT 1");
			ds.setIdleConnectionTestPeriod(5, TimeUnit.MINUTES);

			ds.setPartitionCount(3);
			ds.setMinConnectionsPerPartition(10);
			ds.setMaxConnectionsPerPartition(30);
			ds.setAcquireIncrement(5);

			ds.setStatementsCacheSize(100);

			return ds;

		}

		@Bean
		JdbcTemplate JdbcTemplate() {
			return new JdbcTemplate(dataSource());
		}
	}

	@Autowired
	private DataSource ds;

	@Autowired
	private JdbcTemplate jdbc;

	@Transactional
	@Test
	public void testConnection() throws SQLException {
		final Connection conn = ds.getConnection();
		final Statement stmt = conn.createStatement();
		for (int i = 0; i < 100; i++) {
			try (final ResultSet rs = stmt.executeQuery("select NOW()")) {
				rs.next();
				log.info("testConnection(): NOW() = {}", rs.getTimestamp(1));
			}
		}
	}

	@Transactional
	@Test
	public void testJdbcTemplate() {
		final Timestamp ts = jdbc.queryForObject("select NOW()", Timestamp.class);
		log.info("testJdbcTemplate(): NOW() = {}", ts);
	}
}
