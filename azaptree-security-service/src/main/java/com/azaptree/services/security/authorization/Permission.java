package com.azaptree.services.security.authorization;

import java.util.Collection;

public interface Permission {

	Resource getResource();

	Collection<Action> getActions();

}
