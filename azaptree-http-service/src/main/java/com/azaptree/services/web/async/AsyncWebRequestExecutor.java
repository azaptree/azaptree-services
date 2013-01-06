package com.azaptree.services.web.async;

/*
 * #%L
 * AZAPTREE-SPRING-WEB-MVC
 * %%
 * Copyright (C) 2012 - 2013 AZAPTREE.COM
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

import java.util.concurrent.Executor;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;

public class AsyncWebRequestExecutor {

	protected final Executor executor;

	public AsyncWebRequestExecutor(final Executor executor) {
		Assert.notNull(executor, "executor is required");
		this.executor = executor;
	}

	/**
	 * 
	 * @param request
	 * @param timeout
	 *            Sets the timeout (in milliseconds) for this AsyncContext.
	 *            The timeout applies to this AsyncContext once the container-initiated dispatch during which one of the ServletRequest#startAsync methods was
	 *            called has returned to the container.
	 * 
	 *            The timeout will expire if neither the complete() method nor any of the dispatch methods are called. A timeout value of zero or less indicates
	 *            no timeout.
	 * 
	 *            If setTimeout(long) is not called, then the container's default timeout, which is available via a call to getTimeout(), will apply.
	 * @param command
	 */
	public void execute(final HttpServletRequest request, final long timeout, final Runnable command) {
		Assert.notNull(request, "request is required");
		Assert.notNull(command, "command is required");
		final AsyncContext asyncCtx = request.startAsync();
		asyncCtx.setTimeout(timeout);
		try {
			executor.execute(command);
		} finally {
			asyncCtx.complete();
		}
	}

}
