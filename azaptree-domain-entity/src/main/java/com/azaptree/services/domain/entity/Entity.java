package com.azaptree.services.domain.entity;

import java.util.UUID;

import com.azaptree.services.json.JsonObject;

public interface Entity extends JsonObject {

	UUID getEntityId();
}
