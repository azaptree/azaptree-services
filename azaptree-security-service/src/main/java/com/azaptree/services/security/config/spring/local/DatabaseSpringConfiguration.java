package com.azaptree.services.security.config.spring.local;

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

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.azaptree.services.commons.profiles.Local;

@EnableTransactionManagement
@Configuration
@Local
public class DatabaseSpringConfiguration extends com.azaptree.services.security.config.spring.DatabaseSpringConfiguration {

	@Override
	protected org.apache.tomcat.jdbc.pool.DataSource configure(final org.apache.tomcat.jdbc.pool.DataSource ds) {
		ds.setUrl("jdbc:postgresql://localhost:5433/azaptree");
		ds.setUsername("azaptree");
		ds.setPassword("!azaptree");
		ds.setInitSQL("set search_path to azaptree");
		return ds;
	}

}
