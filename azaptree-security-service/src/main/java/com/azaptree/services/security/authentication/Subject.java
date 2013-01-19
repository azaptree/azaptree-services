package com.azaptree.services.security.authentication;

import java.util.Collection;

import com.azaptree.services.security.authorization.Group;

public interface Subject {

	/**
	 * Each Subject has a UuidPrincipal, which is the primary identifying Principal.
	 * 
	 * @return
	 */
	UuidPrincipal getUuidPrincipal();

	Collection<Group> getGroups();

	Collection<Principal> getPrinciples();

	Collection<Credential> getCredentials();

}
