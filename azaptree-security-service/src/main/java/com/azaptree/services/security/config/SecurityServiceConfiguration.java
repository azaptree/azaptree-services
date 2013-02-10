package com.azaptree.services.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azaptree.services.security.SecurityCredentialsService;
import com.azaptree.services.security.SecurityService;
import com.azaptree.services.security.SessionManagementService;
import com.azaptree.services.security.SubjectRepositoryService;

@Configuration
public interface SecurityServiceConfiguration {

	@Bean
	SecurityCredentialsService securityCredentialsService();

	@Bean
	SecurityService securityService();

	@Bean
	SessionManagementService sessionManagementService();

	@Bean
	SubjectRepositoryService subjectRepositoryService();

}
