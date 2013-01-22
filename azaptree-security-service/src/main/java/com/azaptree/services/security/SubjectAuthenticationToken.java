package com.azaptree.services.security;

import java.util.Map;
import java.util.UUID;

import org.apache.shiro.authc.AuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;

public class SubjectAuthenticationToken implements AuthenticationToken {
	private static final long serialVersionUID = 1L;

	private final UUID subjectId;
	private final Map<String, String> credentials;

	public SubjectAuthenticationToken(final UUID subjectId, final Map<String, String> credentials) {
		Assert.notNull(subjectId, "subjectId is required");
		Assert.isTrue(!CollectionUtils.isEmpty(credentials));
		this.subjectId = subjectId;
		this.credentials = ImmutableMap.<String, String> builder().putAll(credentials).build();
	}

	@Override
	public Map<String, String> getCredentials() {
		return credentials;
	}

	@Override
	public Object getPrincipal() {
		return getSubjectId();
	}

	public UUID getSubjectId() {
		return subjectId;
	}

}
