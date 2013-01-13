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
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Provides support for asynchronous HTTP request processing via Jetty <a href"http://wiki.eclipse.org/Jetty/Feature/Continuations">Contininuations</a>.
 * 
 * Jetty's <a href="http://wiki.eclipse.org/Jetty/Feature/Continuations#Suspend_Continue_Pattern">Suspend Continue Pattern</a> is used.
 * 
 * Simply extend this class and provide a process() implementation.
 * 
 * Override the preProcess() method as needed, which is run the current request thread and may be used to short circuit the response - i.e., to indicate that
 * the preProcess() handled the request, then set Request.handled to true.
 * 
 * @author alfio
 * 
 */
public abstract class AsyncSuspendContinueHttpHandlerSupport extends AbstractHandler {
	protected Logger log = LoggerFactory.getLogger(getClass());

	protected final Executor executor;

	protected long continuationTimeoutMillis;

	/**
	 * Uses Jetty's default continuation timeout (10 sec)
	 * 
	 * @param executor
	 *            REQUIRED
	 */
	public AsyncSuspendContinueHttpHandlerSupport(final Executor executor) {
		super();
		Assert.notNull(executor, "executor is required");
		this.executor = executor;
		log.info(toString());
	}

	/**
	 * 
	 * @param executor
	 *            REQUIRED
	 * @param continuationTimeoutMillis
	 *            must be > 0
	 */
	public AsyncSuspendContinueHttpHandlerSupport(final Executor executor, final long continuationTimeoutMillis) {
		this(executor);
		Assert.isTrue(continuationTimeoutMillis > 0, "constraint: continuationTimeoutMillis > 0");
		this.continuationTimeoutMillis = continuationTimeoutMillis;
		log.info(toString());
	}

	private void executeContinuation(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
		final Continuation continuation = ContinuationSupport.getContinuation(baseRequest);
		continuation.suspend();
		if (continuationTimeoutMillis > 0) {
			continuation.setTimeout(continuationTimeoutMillis);
		}
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					process(target, baseRequest, request, response);
				} catch (final Exception e) {
					log.error("Request failed", e);
				} finally {
					continuation.complete();
				}

			}
		});

	}

	@Override
	public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException, ServletException {
		preProcess(target, baseRequest, request, response);
		if (baseRequest.isHandled()) {
			log.debug("request was handled by preProcess()");
			return;
		}

		executeContinuation(target, baseRequest, request, response);
		baseRequest.setHandled(true);
	}

	/**
	 * The intent is to perform some pre-processing before handing off to another thread.
	 * 
	 * Use cases include validating the request. If the request is invalid, then simply handle the request in the current thread and return the response.
	 * 
	 * *** NOTE: If the request is handled by this method, then set the Request.handled property to true: <code>baseRequest.setHandled(true);</code>
	 * 
	 * The default implementation does nothing.
	 * 
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void preProcess(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException, ServletException {
		// NOOP
	}

	/**
	 * Handles the request in a separate thread.
	 * 
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 */
	protected abstract void process(String target, Request baseRequest, HttpServletRequest request,
	        HttpServletResponse response);

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
		        .append("continuationTimeoutMillis", continuationTimeoutMillis)
		        .append("executor", executor)
		        .toString();
	}

}
