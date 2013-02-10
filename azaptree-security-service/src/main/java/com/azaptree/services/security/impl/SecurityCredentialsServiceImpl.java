package com.azaptree.services.security.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.azaptree.services.security.CredentialToByteSourceConverter;
import com.azaptree.services.security.IncompatibleCredentialTypeException;
import com.azaptree.services.security.SecurityCredentialsService;
import com.azaptree.services.security.SecurityServiceException;
import com.google.common.collect.ImmutableMap;

public class SecurityCredentialsServiceImpl implements SecurityCredentialsService {

	private final Map<String, CredentialToByteSourceConverter<?>> credentialToByteSourceConverters;

	/**
	 * 
	 * @param credentialToByteSourceConverters
	 *            makes a copy of the supplied Map
	 */
	public SecurityCredentialsServiceImpl(final Map<String, CredentialToByteSourceConverter<?>> credentialToByteSourceConverters) {
		Assert.notEmpty(credentialToByteSourceConverters, "credentialToByteSourceConverters are required");
		this.credentialToByteSourceConverters = ImmutableMap.<String, CredentialToByteSourceConverter<?>> builder().putAll(credentialToByteSourceConverters)
		        .build();

		final Logger log = LoggerFactory.getLogger(SecurityCredentialsService.class);
		if (log.isInfoEnabled()) {
			final StringWriter sw = new StringWriter(256);
			final PrintWriter pw = new PrintWriter(sw);
			pw.println("credentialToByteSourceConverters:");
			for (Map.Entry<String, CredentialToByteSourceConverter<?>> entry : credentialToByteSourceConverters.entrySet()) {
				pw.print(entry.getKey());
				pw.print(" -> ");
				pw.println(entry.getValue().getClass().getName());
			}

			log.info(sw.toString());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public byte[] convertCredentialToBytes(String name, Object credential) throws SecurityServiceException, IncompatibleCredentialTypeException {
		Assert.hasText(name, "name is required");
		Assert.notNull(credential, "credential is required");

		final CredentialToByteSourceConverter converter = credentialToByteSourceConverters.get(name);
		if (converter == null) {
			throw new IncompatibleCredentialTypeException(String.format("%s -> %s", name, credential.getClass().getName()));
		}
		return converter.convert(credential);
	}

	@Override
	public Map<String, Class<?>> getSupportedCredentials() throws SecurityServiceException {
		final Map<String, Class<?>> supportedCredentials = new HashMap<>();
		for (Map.Entry<String, CredentialToByteSourceConverter<?>> entry : credentialToByteSourceConverters.entrySet()) {
			final CredentialToByteSourceConverter<?> converter = entry.getValue();
			final Class<?> converterParamType;
			for (Type genericInterface : converter.getClass().getGenericInterfaces()) {
				if (genericInterface instanceof ParameterizedType) {
					final ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
					if (((Class<?>) parameterizedType.getRawType()).isAssignableFrom(CredentialToByteSourceConverter.class)) {
						final Type type = parameterizedType.getActualTypeArguments()[0];
						converterParamType = type instanceof Class<?> ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
						supportedCredentials.put(entry.getKey(), converterParamType);
						break;
					}
				}
			}
		}

		return supportedCredentials;
	}

	@Override
	public boolean isCredentialSupported(String name, Object credential) {
		Assert.hasText(name, "name is required");
		Assert.notNull(credential, "credential is required");

		final CredentialToByteSourceConverter<?> converter = credentialToByteSourceConverters.get(name);
		if (converter == null) {
			return false;
		}

		return converter.isCompatible(credential);
	}

}
