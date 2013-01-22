package com.azaptree.services.domain.entity.dao;

/*
 * #%L
 * AZAPTREE-DOMAIN-ENTITY
 * %%
 * Copyright (C) 2012 - 2013 AZAPTREE.COM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.azaptree.services.domain.entity.EntityAuditLogRecord;
import com.azaptree.services.domain.entity.EntityAuditLogRecord.AuditAction;

@Repository
public interface EntityAuditLogRecordDAO extends EntityDAO<EntityAuditLogRecord> {

	EntityAuditLogRecord getLatestLogRecord(UUID entityId);

	EntityAuditLogRecord getLatestLogRecord(UUID entityId, AuditAction action);

	Iterator<EntityAuditLogRecord> getLogRecords(Date from);

	Iterator<EntityAuditLogRecord> getLogRecords(Date from, Date to);

	Iterator<EntityAuditLogRecord> getLogRecords(UUID entityId, Date from);
}
