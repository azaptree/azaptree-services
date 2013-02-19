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

import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.commons.TypeReferenceKey;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.dao.SubjectDAO;

public class DeleteSubject extends CommandSupport {

	@Autowired
	private SubjectDAO subjectDAO;

	public static final TypeReferenceKey<UUID> SUBJECT_ID = CommandContextKeys.SUBJECT_ID;

	public DeleteSubject() {
		this.setInputKeys(SUBJECT_ID);
	}

	@Transactional
	@Override
	protected boolean executeCommand(final Context ctx) {
		final UUID subjectId = get(ctx, SUBJECT_ID);
		if (!subjectDAO.delete(subjectId)) {
			throw new UnknownSubjectException(subjectId.toString());
		}

		return CONTINUE_PROCESSING;
	}
}
