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

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.util.Assert;

import com.azaptree.services.domain.entity.impl.DomainEntity;
import com.azaptree.services.security.domain.SessionAttribute;

public class SessionAttributeImpl extends DomainEntity implements SessionAttribute {

	private UUID sessionId;
	private String name;
	private String json;

	public SessionAttributeImpl(final InputStream json) throws IOException {
		super(json);
	}

	public SessionAttributeImpl(final SessionAttribute entity) {
		super(entity);
		sessionId = entity.getSessionId();
		name = entity.getName();
		setJson(entity.getJson());
	}

	public SessionAttributeImpl(final String json) {
		super(json);
	}

	public SessionAttributeImpl(final UUID sessionId, final String name, final String json) {
		Assert.notNull(sessionId, "sessionId is required");
		Assert.hasText(name, "name is required");
		Assert.hasText(json, "json is required");
		this.sessionId = sessionId;
		this.name = name;
		setJson(json);
	}

	@Override
	public String getJson() {
		return json;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UUID getSessionId() {
		return sessionId;
	}

	@Override
	public void setJson(final String json) {
		this.json = json;
	}

}
