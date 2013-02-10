package com.azaptree.services.security.credentialToByteSourceConverters;

import java.io.UnsupportedEncodingException;

import org.springframework.util.Assert;

import com.azaptree.services.security.CredentialToByteSourceConverter;

public class StringToByteSourceConverter implements CredentialToByteSourceConverter<String> {

	@Override
	public byte[] convert(final String credential) {
		Assert.hasText(credential, "credential is required");
		try {
			return credential.getBytes("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean isCompatible(final Object credential) {
		return credential instanceof String;
	}

}
