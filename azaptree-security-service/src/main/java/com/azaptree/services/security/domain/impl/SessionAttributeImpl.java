package com.azaptree.services.security.domain.impl;

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

	public SessionAttributeImpl() {
	}

	public SessionAttributeImpl(final InputStream json) throws IOException {
		super(json);
	}

	public SessionAttributeImpl(final SessionAttribute entity) {
		super(entity);
		sessionId = entity.getSessionId();
		name = entity.getName();
		json = entity.getJson();
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
		this.json = json;
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
