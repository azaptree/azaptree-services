package com.azaptree.services.command;

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

import com.azaptree.services.commons.validation.ValidationException;

public interface CommandContextValidator {

	/**
	 * Validate the CommandContext before the Command is executed
	 * 
	 * @param command
	 * @param ctx
	 * @throws ValidationException
	 * @throws IllegalArgumentException
	 */
	void validateInput(Command command, Context ctx) throws ValidationException, IllegalArgumentException;

	/**
	 * Validate the CommandContext after the Command is executed
	 * 
	 * @param command
	 * @param ctx
	 * @throws ValidationException
	 */
	void validateOutput(Command command, Context ctx) throws ValidationException;

}
