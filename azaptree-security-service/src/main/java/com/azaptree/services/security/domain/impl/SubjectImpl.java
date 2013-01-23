package com.azaptree.services.security.domain.impl;

/*
 * #%L
 * AZAPTREE SECURITY SERVICE
 * %%
 * Copyright (C) 2012 - 2013 AZAPTREE.COM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.VersionedEntity;
import com.azaptree.services.domain.entity.impl.DomainVersionedEntity;
import com.azaptree.services.security.domain.Subject;

public class SubjectImpl extends DomainVersionedEntity implements Subject {

	public SubjectImpl(final VersionedEntity entity) {
		Assert.notNull(entity, "entity is required");
		this.setEntityId(entity.getEntityId());
		this.setEntityCreatedOn(entity.getEntityCreatedOn());
		if (entity.getCreatedByEntityId().isPresent()) {
			this.setCreatedBy(entity.getCreatedByEntityId().get());
		} else {
			this.setCreatedBy(null);
		}
		this.setEntityUpdatedOn(entity.getEntityUpdatedOn());
		if (entity.getUpdatedByEntityId().isPresent()) {
			this.setUpdatedBy(entity.getUpdatedByEntityId().get());
		} else {
			this.setUpdatedBy(null);
		}
		this.setEntityVersion(entity.getEntityVersion());
	}

	public SubjectImpl() {
		super();
	}

}
