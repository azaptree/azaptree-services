package com.azaptree.services.security.commands.subjectRepository;

import java.util.UUID;

import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;

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

	@Override
	protected boolean executeCommand(final Context ctx) {
		final UUID subjectId = get(ctx, SUBJECT_ID);
		if (!subjectDAO.delete(subjectId)) {
			throw new UnknownSubjectException(subjectId.toString());
		}

		return CONTINUE_PROCESSING;
	}
}
