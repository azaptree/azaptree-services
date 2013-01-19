package com.azaptree.services.domain.entity;

import java.util.Date;
import java.util.UUID;

public interface EntityAuditlogRecord extends Entity {

	public enum AuditAction {
		CREATED,
		UPDATED,
		DELETED
	}

	AuditAction getAuditAction();

	Date getEntityAuditlogRecordCreatedOn();

	UUID getEntityAuditlogRecordId();

	/**
	 * Should map the the Entity Class
	 * 
	 * @return
	 */
	String getEntityType();
}
