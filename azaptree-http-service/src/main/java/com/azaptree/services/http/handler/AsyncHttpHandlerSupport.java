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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public abstract class AsyncHttpHandlerSupport extends AbstractHandler {
	protected Logger log = LoggerFactory.getLogger(getClass());

	protected final Executor executor;

	public AsyncHttpHandlerSupport(final Executor executor) {
		super();
		Assert.notNull(executor, "executor is required");
		this.executor = executor;
	}

	@Override
	public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException, ServletException {
		final Map<String, Object> context = preProcess(target, baseRequest, request, response);
		if (baseRequest.isHandled()) {
			log.debug("request was handled by preProcess()");
			return;
		}

		executor.execute(new Runnable() {

			@Override
			public void run() {
				process(target, context);
			}

		});

		postProcess(target, baseRequest, request, response);
		if (baseRequest.isHandled()) {
			log.debug("request was handled by preProcess()");
			return;
		}

		baseRequest.setHandled(true);
	}

	/**
	 * The purpose of this methods is to allow the Handler to return an HTTP response. For example, return an appropriate response that the request was accepted
	 * per the requirements of a REST API.
	 * 
	 * *** NOTE: to indicate that this method handled the request, set Request.handled to true: <code>baseRequest.setHandled(true);</code>
	 * 
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void postProcess(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException, ServletException {
		// NOOP
	}

	/**
	 * Validate the request and extract any data from the HTTP request that will be required to process the request asynchronously.
	 * 
	 * If the request is invalid, then handle the request appropriately.
	 * 
	 * *** NOTE: to indicate that this method handled the request, set Request.handled to true: <code>baseRequest.setHandled(true);</code>
	 * 
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @return
	 */
	protected Map<String, Object> preProcess(final String target, final Request baseRequest, final HttpServletRequest request,
	        final HttpServletResponse response) throws IOException, ServletException {
		return Collections.emptyMap();
	}

	/**
	 * 
	 * @param context
	 *            data that is extracted from the request
	 */
	protected abstract void process(final String target, final Map<String, Object> context);

}
