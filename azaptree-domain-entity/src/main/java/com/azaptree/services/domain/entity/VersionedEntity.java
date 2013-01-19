package com.azaptree.services.domain.entity;

import java.util.Date;
import java.util.UUID;

import com.google.common.base.Optional;

public interface VersionedEntity extends Entity {
	Date getEntityCreatedOn();

	Date getEntityLastUpdatedOn();

	long getEntityVersion();

	Optional<UUID> getEntityCreatedByEntityId();

	Optional<UUID> getEntityUpdatedByEntityId();
}
