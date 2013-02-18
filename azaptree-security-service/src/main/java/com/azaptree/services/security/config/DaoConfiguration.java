package com.azaptree.services.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SessionAttributeDAO;
import com.azaptree.services.security.dao.SessionDAO;
import com.azaptree.services.security.dao.SubjectDAO;

@Configuration
public interface DaoConfiguration {

	@Bean
	HashedCredentialDAO hashedCredentialDAO();

	@Bean
	SessionAttributeDAO sessionAttributeDAO();

	@Bean
	SessionDAO sessionDAO();

	@Bean
	SubjectDAO subjectDAO();
}
