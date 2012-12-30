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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 
 * Used to record command execution metrics.
 * 
 * @author Alfio Zappala
 * 
 */
public class CommandExcecutionMetric {
	private final long executionTimeStart;
	private long executionTimeEnd;
	private boolean success = true;
	private Throwable throwable;

	public CommandExcecutionMetric() {
		super();
		executionTimeStart = System.currentTimeMillis();
	}

	public void failed() {
		executionTimeEnd = System.currentTimeMillis();
		success = false;
	}

	/**
	 * 
	 * @param executionTimeEnd
	 * @param exception
	 */
	public void failed(final Throwable exception) {
		executionTimeEnd = System.currentTimeMillis();
		throwable = exception;
		success = exception == null;
	}

	public long getExecutionTimeEnd() {
		return executionTimeEnd;
	}

	public long getExecutionTimeStart() {
		return executionTimeStart;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public boolean isSuccess() {
		return success;
	}

	public void succeeded() {
		executionTimeEnd = System.currentTimeMillis();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(128);
		sb.append("CommandExcecutionMetric [executionTimeStart=").append(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(executionTimeStart));
		sb.append("executionTime=").append(executionTimeEnd - executionTimeStart).append(" msec, success=").append(success);
		if (throwable != null) {
			sb.append(", throwable=").append(ExceptionUtils.getStackTrace(throwable));
		}
		sb.append(']');

		return sb.toString();
	}

	public void update(final long executionTimeEnd, final boolean success, final Throwable throwable) {
		this.executionTimeEnd = executionTimeEnd;
		this.success = success;
		this.throwable = throwable;
	}

}
