package com.azaptree.services.security.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

@Configuration
public interface DatabaseConfiguration extends TransactionManagementConfigurer {

	@Bean
	DataSource securityServiceDataSource();

	@Bean
	JdbcTemplate securityServiceJdbcTemplate();

}
