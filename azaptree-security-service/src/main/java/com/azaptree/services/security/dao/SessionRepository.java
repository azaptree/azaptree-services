package com.azaptree.services.security.dao;

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

public interface SessionRepository {
	/**
	 * 
	 * @param subjectId
	 * @return number of sessions that were deleted
	 */
	int deleteSessionsBySubjectId(UUID subjectId);

	UUID[] getSessionIdsBySubject(UUID subjectId);

	/**
	 * 
	 * @param sessionId
	 * @return false if the session does not exist or has already expired
	 */
	boolean touchSession(UUID sessionId);
}
