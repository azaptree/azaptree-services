package com.azaptree.services.security.commands.subjectRepository;

import org.apache.commons.chain.Context;
import org.springframework.util.Assert;

import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.services.security.dao.SubjectDAO;

public class CreateSubject extends CommandSupport {

	private final SubjectDAO subjectDAO;

	public CreateSubject(final SubjectDAO subjectDAO) {
		Assert.notNull(subjectDAO, "subjectDAO is required");
		this.subjectDAO = subjectDAO;
	}

	@Override
	protected boolean executeCommand(final Context ctx) {
		// TODO Auto-generated method stub
		return false;
	}

}
