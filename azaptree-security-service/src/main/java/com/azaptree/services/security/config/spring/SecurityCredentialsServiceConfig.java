package com.azaptree.services.security.config.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azaptree.services.security.CredentialNames;
import com.azaptree.services.security.CredentialToByteSourceConverter;
import com.azaptree.services.security.SecurityCredentialsService;
import com.azaptree.services.security.credentialToByteSourceConverters.StringToByteSourceConverter;
import com.azaptree.services.security.impl.SecurityCredentialsServiceImpl;

@Configuration
public class SecurityCredentialsServiceConfig {

	protected Map<String, CredentialToByteSourceConverter<?>> credentialToByteSourceConverters() {
		final Map<String, CredentialToByteSourceConverter<?>> converters = new HashMap<>();
		converters.put(CredentialNames.PASSWORD.credentialName, stringToByteSourceConverter());
		return converters;
	}

	@Bean
	public SecurityCredentialsService securityCredentialsService() {
		return new SecurityCredentialsServiceImpl(credentialToByteSourceConverters());
	}

	@Bean
	public StringToByteSourceConverter stringToByteSourceConverter() {
		return new StringToByteSourceConverter();
	}

}
