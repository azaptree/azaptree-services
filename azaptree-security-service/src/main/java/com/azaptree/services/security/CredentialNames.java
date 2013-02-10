package com.azaptree.services.security;

public enum CredentialNames {
	PASSWORD("password");

	public final String credentialName;

	private CredentialNames(final String credentialName) {
		this.credentialName = credentialName;
	}
}
