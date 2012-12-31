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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.azaptree.services.command.Command;
import com.azaptree.services.command.CommandContextValidator;
import com.azaptree.services.command.CommandException;
import com.azaptree.services.command.util.CommandUtils;
import com.azaptree.services.commons.TypeReferenceKey;
import com.google.common.base.Optional;

/**
 * 
 * 
 * @author alfio
 * 
 */
public abstract class CommandSupport implements Command, BeanNameAware {
	protected Logger log = LoggerFactory.getLogger(getClass());

	protected String name;

	protected CommandContextValidator validator;
	protected TypeReferenceKey<?>[] outputKeys, inputKeys;

	public CommandSupport() {
	}

	public CommandSupport(String name) {
		this.name = name;
	}

	@Override
	public boolean execute(final Context ctx) throws CommandException {
		if (validator != null) {
			validator.validateInput(this, ctx);
			final boolean result = executeCommand(ctx);
			validator.validateOutput(this, ctx);
			return result;
		}

		return executeCommand(ctx);
	}

	protected abstract boolean executeCommand(Context ctx);

	protected <T> T get(final Context ctx, final TypeReferenceKey<T> key) {
		return CommandUtils.get(ctx, key);
	}

	@Override
	public Optional<TypeReferenceKey<?>[]> getInputKeys() {
		if (inputKeys == null || inputKeys.length == 0) {
			return Optional.absent();
		}
		return Optional.of(Arrays.copyOf(inputKeys, inputKeys.length));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<TypeReferenceKey<?>[]> getOutputKeys() {
		if (outputKeys == null || outputKeys.length == 0) {
			return Optional.absent();
		}
		return Optional.of(Arrays.copyOf(outputKeys, outputKeys.length));
	}

	@Override
	public CommandContextValidator getValidator() {
		return validator;
	}

	@PostConstruct
	public void init() {
		if (StringUtils.isBlank(name)) {
			name = getClass().getSimpleName();
		}
	}

	protected <T> T put(final Context ctx, final TypeReferenceKey<T> key, final T value) {
		return CommandUtils.put(ctx, key, value);
	}

	@Override
	public void setBeanName(final String name) {
		this.name = name;
	}

	public void setInputKeys(final TypeReferenceKey<?>[] inputKeys) {
		this.inputKeys = inputKeys;
	}

	public void setOutputKeys(final TypeReferenceKey<?>[] outputKeys) {
		this.outputKeys = outputKeys;
	}

	public void setValidator(final CommandContextValidator validator) {
		this.validator = validator;
	}

}
