package com.azaptree.services.command.impl;

/*
 * #%L
 * AZAPTREE-COMMAND-SERVICE
 * %%
 * Copyright (C) 2012 AZAPTREE.COM
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

import org.apache.commons.chain.Context;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.CommandContextValidator;
import com.azaptree.services.commons.TypeReferenceKey;
import com.azaptree.services.commons.validation.ValidationException;
import com.google.common.base.Optional;

public abstract class CommandContextValidatorSupport implements CommandContextValidator {

	protected abstract void checkInput(Command command, Context ctx);

	protected abstract void checkOutput(Command command, Context ctx);

	protected void checkRequiredInputKeys(final Command command, final Context ctx) {
		final Optional<TypeReferenceKey<?>[]> keys = command.getInputKeys();
		if (!keys.isPresent()) {
			return;
		}

		for (final TypeReferenceKey<?> key : keys.get()) {
			if (key.isRequired()) {
				if (ctx.get(key.getName()) == null && key.getDefaultValue() == null) {
					throw new ValidationException(String.format("%s  : TypeReferenceKey is required: %s", command.getName(), key));
				}
			}
		}
	}

	protected void checkRequiredOutputKeys(final Command command, final Context ctx) {
		final Optional<TypeReferenceKey<?>[]> keys = command.getOutputKeys();
		if (!keys.isPresent()) {
			return;
		}

		for (final TypeReferenceKey<?> key : keys.get()) {
			if (key.isRequired()) {
				if (ctx.get(key.getName()) == null && key.getDefaultValue() == null) {
					throw new ValidationException(String.format("%s  : TypeReferenceKey is required: %s", command.getName(), key));
				}
			}
		}
	}

	/**
	 * Checks required input keys, and then delegates to checkInput();
	 * 
	 */
	@Override
	public void validateInput(final Command command, final Context ctx) throws ValidationException {
		checkRequiredInputKeys(command, ctx);
		checkInput(command, ctx);
	}

	/**
	 * Checks required output keys, and then delegates to checkOutput();
	 * 
	 */
	@Override
	public void validateOutput(final Command command, final Context ctx) throws ValidationException {
		checkRequiredOutputKeys(command, ctx);
		checkOutput(command, ctx);
	}

}
