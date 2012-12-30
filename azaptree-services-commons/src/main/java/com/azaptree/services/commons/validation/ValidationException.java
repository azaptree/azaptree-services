package com.azaptree.services.commons.validation;

/*
 * #%L
 * AZAPTREE-SERVICES-COMMONS
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

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.util.Assert;

public class ValidationException extends javax.validation.ValidationException {

	private static final long serialVersionUID = 1L;

	protected Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();

	public ValidationException() {
		super();
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

	public ValidationException(final ConstraintViolation<?>... constraintViolations) {
		super();
		Assert.notNull(constraintViolations, "constraintViolations is required");
		Assert.isTrue(constraintViolations.length > 0, "constraintViolations cannot be empty");

		for (final ConstraintViolation<?> v : constraintViolations) {
			this.constraintViolations.add(v);
		}
	}

	public Set<ConstraintViolation<?>> getConstraintViolations() {
		return constraintViolations;
	}

	@Override
	public String getMessage() {
		if (constraintViolations == null) {
			return super.getMessage();
		}

		final StringBuilder sb = new StringBuilder(constraintViolations.size() * 50);
		for (final ConstraintViolation<?> v : constraintViolations) {
			sb.append(v.getPropertyPath()).append(" : ").append(v.getMessage()).append('\n');
		}
		return sb.toString();
	}

}
