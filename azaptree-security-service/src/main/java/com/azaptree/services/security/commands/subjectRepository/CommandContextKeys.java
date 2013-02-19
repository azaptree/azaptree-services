package com.azaptree.services.security.commands.subjectRepository;

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
