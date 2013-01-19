package com.azaptree.services.security.authorization;

import java.util.Collection;

public interface Group {

	Collection<Role> getRoles();

	Collection<Permission> getPermissions();
}
