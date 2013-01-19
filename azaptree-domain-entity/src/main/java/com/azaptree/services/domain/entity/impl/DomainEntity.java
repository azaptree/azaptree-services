package com.azaptree.services.domain.entity.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import com.azaptree.services.domain.entity.Entity;

public class DomainEntity implements Entity {

	protected UUID entityId;

	public DomainEntity() {
	}

	@Override
	public UUID getEntityId() {
		return entityId;
	}

	/**
	 * Not supported and throws UnsupportedOperationException if invoked.
	 * 
	 * Sub-classes that
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void init(final InputStream json) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void init(final String json) {
		try {
			init(new ByteArrayInputStream(json.getBytes("UTF-8")));
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setEntityId(final UUID entityId) {
		this.entityId = entityId;
	}

	@Override
	public String toJson() {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
		writeJson(bos);
		return bos.toString();
	}

	@Override
	public void writeJson(final OutputStream os) {
		// TODO Auto-generated method stub

	}

}
