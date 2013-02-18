package com.azaptree.services.security.commands.subjectRepository;

import java.util.UUID;

import com.azaptree.services.commons.TypeReferenceKey;
import com.azaptree.services.security.domain.Subject;

public interface CommandContextKeys {

	public static final TypeReferenceKey<UUID> SUBJECT_ID = new TypeReferenceKey<UUID>("SUBJECT_ID", true) {
		// intentionally empty
	};

	public static final TypeReferenceKey<UUID> UPDATED_BY_SUBJECT_ID = new TypeReferenceKey<UUID>("UPDATED_BY_SUBJECT_ID", false) {
		// intentionally empty
	};

	public static final TypeReferenceKey<Subject> SUBJECT = new TypeReferenceKey<Subject>("SUBJECT", true) {
		// intentionally empty
	};

}
