package com.azaptree.services.security.config.spring;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.azaptree.services.security.config.DatabaseConfiguration;

@EnableTransactionManagement
@Configuration
public class DatabaseSpringConfiguration implements DatabaseConfiguration {

	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new DataSourceTransactionManager(securityServiceDataSource());
	}

	@Override
	@Bean
	public DataSource securityServiceDataSource() {
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

	@Override
	@Bean
	public JdbcTemplate securityServiceJdbcTemplate() {
		return new JdbcTemplate(securityServiceDataSource());
	}

}
