package com.azaptree.services.security.dao;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.azaptree.services.security.domain.SessionAttribute;

public interface SesssionAttributeRepository {

	String getAttributeJsonValue(UUID sessionId, String key);

	List<String> getAttributeKeys(UUID sessionId);

	Map<String, SessionAttribute> getSessionAttributes(UUID sessionId);

	boolean removeAttribute(UUID sessionId, String key);

	boolean setAttribute(UUID sessionId, String key, String jsonValue);

}
