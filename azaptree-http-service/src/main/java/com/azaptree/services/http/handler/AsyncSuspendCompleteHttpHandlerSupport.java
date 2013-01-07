package com.azaptree.services.http.handler;

/*
 * #%L
 * AZAPTREE-HTTP-SERVICE
 * %%
 * Copyright (C) 2012 - 2013 AZAPTREE.COM
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

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public abstract class AsyncSuspendCompleteHttpHandlerSupport extends AbstractHandler implements Server.Graceful {

	protected final Executor executor;

	protected Logger log = LoggerFactory.getLogger(getClass());

	protected long continuationTimeoutMillis;

	protected boolean shutdown;

	public AsyncSuspendCompleteHttpHandlerSupport(final Executor executor) {
		super();
		Assert.notNull(executor, "executor is required");
		this.executor = executor;
		log.info(toString());
	}

	public AsyncSuspendCompleteHttpHandlerSupport(final Executor executor, final long continuationTimeoutMillis) {
		this(executor);
		Assert.isTrue(continuationTimeoutMillis > 0, "constraint: continuationTimeoutMillis > 0");
		this.continuationTimeoutMillis = continuationTimeoutMillis;
		log.info(toString());
	}

	@Override
	public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException, ServletException {
		final Continuation continuation = ContinuationSupport.getContinuation(baseRequest);
		continuation.suspend();
		if (continuationTimeoutMillis > 0) {
			continuation.setTimeout(continuationTimeoutMillis);
		}
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					handleAsync(target, baseRequest, request, response);
				} catch (final Exception e) {
					log.error("Request failed", e);
				} finally {
					continuation.complete();
				}

			}
		});

		baseRequest.setHandled(true);
	}

	protected abstract void handleAsync(String target, Request baseRequest, HttpServletRequest request,
	        HttpServletResponse response);

	@Override
	public void setShutdown(final boolean shutdown) {
		log.info("shutdown : {}", shutdown);
		this.shutdown = shutdown;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
		        .append("continuationTimeoutMillis", continuationTimeoutMillis)
		        .append("executor", executor)
		        .append("shutdown", shutdown)
		        .toString();
	}

}
