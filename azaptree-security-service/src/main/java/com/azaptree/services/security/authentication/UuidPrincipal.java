package com.azaptree.services.security.authentication;

import java.util.UUID;

public interface UuidPrincipal extends Principal {

	UUID getUuid();

}
