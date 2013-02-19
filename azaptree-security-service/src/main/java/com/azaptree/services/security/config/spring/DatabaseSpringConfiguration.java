package com.azaptree.services.security.config.spring;

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
public abstract class DatabaseSpringConfiguration implements DatabaseConfiguration {

	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new DataSourceTransactionManager(securityServiceDataSource());
	}

	/**
	 * At a minimum the following properties need to be configured:
	 * 
	 * <ul>
	 * <li>url : jdbc:postgresql://[host]:[port]/[database-name]</li>
	 * <li>userName</li>
	 * <li>password</li>
	 * <li>initSQL : set search_path to [schema-name]</li>
	 * </ul>
	 * 
	 * @param ds
	 */
	protected abstract org.apache.tomcat.jdbc.pool.DataSource configure(org.apache.tomcat.jdbc.pool.DataSource ds);

	@Override
	@Bean(destroyMethod = "close")
	public DataSource securityServiceDataSource() {
		final org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
		ds.setDefaultAutoCommit(false);
		ds.setDriverClassName("org.postgresql.Driver");
		ds.setValidationQuery("select 1");

		ds.setTestOnBorrow(true);
		ds.setTestOnConnect(true);
		ds.setTestWhileIdle(true);
		ds.setTimeBetweenEvictionRunsMillis(1000 * 30);
		ds.setValidationInterval(1000 * 60);

		ds.setLogValidationErrors(true);
		ds.setInitialSize(10);
		ds.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;" +
		        "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
		        "org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport");

		ds.setCommitOnReturn(true);

		ds.setMaxAge(1000 * 60 * 60);

		ds.setLogAbandoned(true);
		ds.setSuspectTimeout(60);

		return configure(ds);
	}

	@Override
	@Bean
	public JdbcTemplate securityServiceJdbcTemplate() {
		return new JdbcTemplate(securityServiceDataSource());
	}

}
