package com.azaptree.services.security.commands.subjectRepository;

import java.util.UUID;

import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;

import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.commons.TypeReferenceKey;
import com.azaptree.services.security.UnknownSubjectException;
import com.azaptree.services.security.dao.SubjectDAO;
import com.azaptree.services.security.domain.Subject;

/**
 * If the subject is not found, then a {@link UnknownSubjectException} is thrown
 * 
 * @author alfio
 */
public class GetSubject extends CommandSupport {

	@Autowired
	private SubjectDAO subjectDAO;

	public static final TypeReferenceKey<UUID> SUBJECT_ID = CommandContextKeys.SUBJECT_ID;

	public static final TypeReferenceKey<Subject> SUBJECT = CommandContextKeys.SUBJECT;

	public GetSubject() {
		this.setInputKeys(SUBJECT_ID);
	}

	@Override
	protected boolean executeCommand(final Context ctx) {
		final UUID subjectId = get(ctx, SUBJECT_ID);
		final Subject subject = subjectDAO.findById(subjectId);
		if (subject == null) {
			throw new UnknownSubjectException();
		}
		put(ctx, SUBJECT, subject);

		return CONTINUE_PROCESSING;
	}
}
