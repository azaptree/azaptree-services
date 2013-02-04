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
import com.azaptree.services.security.domain.Session;

public class SessionImpl extends DomainEntity implements Session {

	private long createdOn;
	private long lastAccessedOn;
	private int timeoutSeconds;

	private String host;

	private UUID subjectId;

	public SessionImpl() {
	}

	public SessionImpl(final InputStream json) throws IOException {
		super(json);
	}

	public SessionImpl(final Session entity) {
		super(entity);
		setSubjectId(entity.getSubjectId());
		setCreatedOn(entity.getCreatedOn());
		setLastAccessedOn(entity.getLastAccessedOn());
		setTimeoutSeconds(entity.getTimeoutSeconds());
		setHost(entity.getHost());
	}

	public SessionImpl(final String json) {
		super(json);
	}

	public SessionImpl(final UUID subjectId) {
		this(subjectId, System.currentTimeMillis(), System.currentTimeMillis(), 1800, null);
	}

	public SessionImpl(final UUID subjectId, final int timeoutSeconds, final String host) {
		this(subjectId, System.currentTimeMillis(), System.currentTimeMillis(), timeoutSeconds, host);
	}

	public SessionImpl(final UUID subjectId, final long createdOn, final long lastAccessedOn, final int timeoutSeconds, final String host) {
		setSubjectId(subjectId);
		setCreatedOn(createdOn);
		setLastAccessedOn(lastAccessedOn);
		setTimeoutSeconds(timeoutSeconds);
		setHost(host);
		validate();
	}

	@Override
	public long getCreatedOn() {
		return createdOn;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public long getLastAccessedOn() {
		return lastAccessedOn;
	}

	@Override
	public UUID getSubjectId() {
		return subjectId;
	}

	@Override
	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}

	@Override
	public boolean isExpired() {
		return System.currentTimeMillis() >= lastAccessedOn + timeoutSeconds * 1000;
	}

	public void setCreatedOn(final long createdOn) {
		this.createdOn = createdOn;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public void setLastAccessedOn(final long lastAccessedOn) {
		this.lastAccessedOn = lastAccessedOn;
	}

	public void setSubjectId(final UUID subjectId) {
		this.subjectId = subjectId;
	}

	public void setTimeoutSeconds(final int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	@Override
	public void touch() {
		lastAccessedOn = System.currentTimeMillis();
	}

	public void validate() {
		Assert.notNull(subjectId, "subjectId is required");
		Assert.isTrue(createdOn > 0, "constraint violated: createdOn > 0");
		Assert.isTrue(lastAccessedOn >= createdOn, "constraint violated: lastAccessedOn >= createdOn");
		Assert.isTrue(timeoutSeconds > 0, "constraint violated: timeoutSeconds > 0");
	}

}
