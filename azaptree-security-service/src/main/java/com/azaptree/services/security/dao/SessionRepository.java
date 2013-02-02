package com.azaptree.services.security.dao;

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
