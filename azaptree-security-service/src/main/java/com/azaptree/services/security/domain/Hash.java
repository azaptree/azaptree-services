package com.azaptree.services.security.domain;

public interface Hash {

	byte[] getHash();

	String getHashAlgorithm();

	int getHashIterations();

	/**
	 * The salt that is used to hash the credential
	 * 
	 * @return
	 */
	byte[] getSalt();
}
