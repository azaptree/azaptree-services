package com.azaptree.services.security.domain;

import java.util.Set;

public interface Subject {

	Set<Principal> getPrincipals();

	Set<Credential> getCredentials();
}
