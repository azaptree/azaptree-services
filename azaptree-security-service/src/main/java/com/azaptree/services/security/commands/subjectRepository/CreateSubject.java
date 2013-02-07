package com.azaptree.services.security.commands.subjectRepository;

import org.apache.commons.chain.Context;

import com.azaptree.services.command.impl.CommandSupport;

public class CreateSubject extends CommandSupport {

	public CreateSubject() {
	}

	public CreateSubject(final String name) {
		super(name);
	}

	@Override
	protected boolean executeCommand(final Context ctx) {
		// TODO Auto-generated method stub
		return false;
	}

}
