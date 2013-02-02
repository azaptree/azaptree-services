package com.azaptree.services.security.domain;

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

import java.util.UUID;

import com.azaptree.services.domain.entity.Entity;

public interface Session extends Entity {

	long getCreatedOn();

	/**
	 * Returns the host name or IP string of the host that originated this session, or null if the host is unknown.
	 * 
	 * @return
	 */
	String getHost();

	long getLastAccessedOn();

	/**
	 * The max length of time that a session can remain inactive
	 * 
	 * @return
	 */
	int getTimeoutSeconds();

	UUID getSubjectId();

	/**
	 * The session is expired if current timestamp >= lastAccessedOn + sessionTimeoutSeconds
	 * 
	 * @return
	 */
	boolean isExpired();

	/**
	 * Explicitly updates the lastAccessOn of this session to the current time when this method is invoked.
	 */
	void touch();

}
