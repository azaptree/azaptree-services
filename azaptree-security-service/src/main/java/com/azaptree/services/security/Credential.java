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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.util.Assert;

public class Credential {
	private final String name;
	private final Object credential;
	private final Date expiresOn;

	public Credential(final String name, final Object credential) {
		Assert.hasText(name, "name is required");
		Assert.notNull(credential, "credential is required");
		this.name = name;
		this.credential = credential;
		expiresOn = null;
	}

	public Credential(final String name, final Object credential, final Date expiresOn) {
		Assert.hasText(name, "name is required");
		Assert.notNull(credential, "credential is required");
		Assert.notNull(expiresOn, "expiresOn is required");
		this.name = name;
		this.credential = credential;
		this.expiresOn = expiresOn;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Credential other = (Credential) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public Object getCredential() {
		return credential;
	}

	public Date getExpiresOn() {
		return expiresOn;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
		        .append("name", name)
		        .append("credential.class", credential.getClass().getName())
		        .append("expiresOn", expiresOn)
		        .toString();
	}

}
