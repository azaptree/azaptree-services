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

import com.azaptree.services.domain.entity.impl.DomainVersionedEntity;
import com.azaptree.services.security.domain.Subject;

public class SubjectImpl extends DomainVersionedEntity implements Subject {

	private Status status;
	private long statusTimestamp;

	private int maxSessions = 1;

	private int consecutiveAuthenticationFailedCount;
	private long lastTimeAuthenticationFailed;

	public SubjectImpl() {
		super();
	}

	public SubjectImpl(final Status status) {
		super();
		setStatus(status);
	}

	public SubjectImpl(final Status status, final int maxSessions) {
		super();
		setStatus(status);
		setMaxSessions(maxSessions);
	}

	public SubjectImpl(final Subject entity) {
		super(entity);
		setStatus(entity.getStatus());
		setStatusTimestamp(entity.getStatusTimestamp());
		setMaxSessions(entity.getMaxSessions());
		setConsecutiveAuthenticationFailedCount(entity.getConsecutiveAuthenticationFailedCount());
		setLastTimeAuthenticationFailed(entity.getLastTimeAuthenticationFailed());
	}

	@Override
	public int getConsecutiveAuthenticationFailedCount() {
		return consecutiveAuthenticationFailedCount;
	}

	@Override
	public long getLastTimeAuthenticationFailed() {
		return lastTimeAuthenticationFailed;
	}

	@Override
	public int getMaxSessions() {
		return maxSessions;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public long getStatusTimestamp() {
		return statusTimestamp;
	}

	@Override
	public void incrementConsecutiveAuthenticationFailedCount() {
		consecutiveAuthenticationFailedCount++;
		lastTimeAuthenticationFailed = System.currentTimeMillis();
	}

	@Override
	public void resetConsecutiveAuthenticationFailedCount() {
		consecutiveAuthenticationFailedCount = 0;
	}

	public void setConsecutiveAuthenticationFailedCount(final int consecutiveAuthenticationFailedCount) {
		this.consecutiveAuthenticationFailedCount = consecutiveAuthenticationFailedCount;
	}

	public void setLastTimeAuthenticationFailed(final long lastTimeAutenticationFailed) {
		this.lastTimeAuthenticationFailed = lastTimeAutenticationFailed;
	}

	@Override
	public void setMaxSessions(final int maxSessions) {
		Assert.isTrue(maxSessions > 0, "constraint check failed: maxSessions > 0");
		this.maxSessions = maxSessions;
	}

	@Override
	public void setStatus(final Status status) {
		Assert.notNull(status, "status is required");
		this.status = status;
		statusTimestamp = System.currentTimeMillis();
	}

	public void setStatusTimestamp(final long statusTimestamp) {
		this.statusTimestamp = statusTimestamp;
	}

}
