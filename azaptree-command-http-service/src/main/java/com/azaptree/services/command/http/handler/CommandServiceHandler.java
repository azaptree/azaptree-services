package com.azaptree.services.command.http.handler;

/*
 * #%L
 * AZAPTREE-COMMAND-HTTP-SERVICE
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import com.azaptree.services.command.CommandService;
import com.azaptree.services.http.handler.AsyncSuspendContinueHttpHandlerSupport;

public class CommandServiceHandler extends AsyncSuspendContinueHttpHandlerSupport {

	@Autowired
	private CommandService commandService;

	public CommandServiceHandler(final Executor executor) {
		super(executor);
	}

	public CommandServiceHandler(final Executor executor, final long continuationTimeoutMillis) {
		super(executor, continuationTimeoutMillis);
	}

	private void generateWADL(final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND_404);
		response.setContentType(MimeTypes.TEXT_XML_UTF_8);
		// TODO: generate WADL
		baseRequest.setHandled(true);
	}

	@Override
	protected void preProcess(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException,
	        ServletException {
		validateRequest(target, baseRequest, request, response);
	}

	@Override
	protected void process(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
		// TODO Auto-generated method stub

	}

	private void validateGET(final String[] uriTokens, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {

	}

	private void validatePOST(final String[] uriTokensF, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
		// TODO Auto-generated method stub

	}

	private void validateRequest(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
		if (StringUtils.isBlank(target)) {
			generateWADL(baseRequest, request, response);
		}

		final String[] uriTokens = StringUtils.split(target, '/');
		if (ArrayUtils.isNotEmpty(uriTokens)) {
			if (!"command-service".equals(uriTokens[0])) {
				response.setStatus(HttpStatus.NOT_FOUND_404);
				baseRequest.setHandled(true);
				return;
			}

			final HttpMethod method = HttpMethod.valueOf(baseRequest.getMethod());
			switch (method) {
			case GET:
				validateGET(uriTokens, baseRequest, request, response);
				break;
			case POST:
				validatePOST(uriTokens, baseRequest, request, response);
				break;
			default:
				response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
				response.setHeader(HttpHeaders.ALLOW, String.format("%s,%s", HttpMethod.GET.name(), HttpMethod.POST.name()));
				baseRequest.setHandled(true);
				return;
			}
		}
	}
}
