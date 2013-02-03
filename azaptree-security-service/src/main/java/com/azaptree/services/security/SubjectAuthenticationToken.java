package com.azaptree.services.security;

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

import java.util.Map;
import java.util.UUID;

import org.apache.shiro.authc.AuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class SubjectAuthenticationToken implements AuthenticationToken {
	private static final long serialVersionUID = 1L;

	private final UUID subjectId;
	private final Map<String, Object> credentials;

	private final boolean rememberMe;

	public SubjectAuthenticationToken(final UUID subjectId, final Map<String, Object> credentials) {
		this(subjectId, credentials, false);
	}

	public SubjectAuthenticationToken(final UUID subjectId, final Map<String, Object> credentials, final boolean rememberMe) {
		Assert.notNull(subjectId, "subjectId is required");
		Assert.isTrue(!CollectionUtils.isEmpty(credentials));
		this.subjectId = subjectId;
		this.credentials = credentials;
		this.rememberMe = rememberMe;
	}

	/**
	 * @return Map<String,String> key = credential name, value = credential value
	 */
	@Override
	public Map<String, Object> getCredentials() {
		return credentials;
	}

	@Override
	public Object getPrincipal() {
		return getSubjectId();
	}

	public UUID getSubjectId() {
		return subjectId;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

}
