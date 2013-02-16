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
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.util.Assert;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.impl.CommandContextValidatorSupport;
import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.commons.TypeReferenceKey;
import com.azaptree.services.security.UnknownCredentialException;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.dao.HashedCredentialDAO;
import com.azaptree.services.security.dao.SubjectDAO;

public class DeleteSubjectCredential extends CommandSupport {

	@Autowired
	private HashedCredentialDAO hashedCredentialDAO;

	@Autowired
	private SubjectDAO subjectDAO;

	public static final TypeReferenceKey<UUID> SUBJECT_ID = CommandContextKeys.SUBJECT_ID;

	public static final TypeReferenceKey<UUID> UPDATED_BY_SUBJECT_ID = CommandContextKeys.UPDATED_BY_SUBJECT_ID;

	public static final TypeReferenceKey<String> CREDENTIAL_NAME = new TypeReferenceKey<String>("CREDENTIAL_NAME", true) {
		// intentionally empty
	};

	public DeleteSubjectCredential() {
		setValidator(new CommandContextValidatorSupport() {

			@Override
			protected void checkInput(final Command command, final Context ctx) {
				final UUID subjectId = get(ctx, SUBJECT_ID);
				if (!subjectDAO.exists(subjectId)) {
					throw new UnknownSubjectException(subjectId.toString());
				}

				final UUID updatedBySubjectId = get(ctx, UPDATED_BY_SUBJECT_ID);
				if (updatedBySubjectId != null) {
					Assert.isTrue(!subjectId.equals(updatedBySubjectId),
					        String.format("The updatedBy subject (%s) cannot be the same as the subject (%s) which we are adding the credential to",
					                subjectId,
					                updatedBySubjectId));
					if (!subjectDAO.exists(updatedBySubjectId)) {
						throw new UnknownSubjectException(String.format("Subject for updatedBy does not exist: %s", updatedBySubjectId));
					}
				}

			}

			@Override
			protected void checkOutput(final Command command, final Context ctx) {
				// none required
			}

		});

		setInputKeys(SUBJECT_ID, CREDENTIAL_NAME, UPDATED_BY_SUBJECT_ID);
	}

	@Transactional
	@Override
	protected boolean executeCommand(final Context ctx) {
		final UUID subjectId = get(ctx, SUBJECT_ID);
		final UUID updatedBySubjectId = get(ctx, UPDATED_BY_SUBJECT_ID);
		final String credentialName = get(ctx, CREDENTIAL_NAME);

		if (!hashedCredentialDAO.deleteBySubjectIdAndName(subjectId, credentialName)) {
			throw new UnknownCredentialException(credentialName);
		}
		if (updatedBySubjectId != null) {
			subjectDAO.touch(subjectId, updatedBySubjectId);
		} else {
			subjectDAO.touch(subjectId);
		}

		return CONTINUE_PROCESSING;
	}
}
