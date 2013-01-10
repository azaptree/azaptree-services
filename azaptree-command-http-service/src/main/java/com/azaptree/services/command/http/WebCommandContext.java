package com.azaptree.services.command.http;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;

import com.azaptree.services.command.CommandContext;

/**
 * 
 * @author alfio
 * 
 * @param <T>
 *            JAXB class
 * @param <V>
 *            JAXB class
 */
public class WebCommandContext<T, V> extends CommandContext {

	private static final long serialVersionUID = 1L;

	protected final HttpServletRequest httpServletRequest;

	protected final HttpServletResponse httpServletResponse;

	protected final T requestMessage;

	protected V responseMessage;

	/**
	 * This constructor is for commands that do not require a request message.
	 * 
	 * @param httpServletRequest
	 *            REQUIRED
	 * @param httpServletResponse
	 *            REQUIRED
	 */
	public WebCommandContext(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {
		Assert.notNull(httpServletRequest, "httpServletRequest is required");
		Assert.notNull(httpServletResponse, "httpServletResponse is required");
		this.httpServletRequest = httpServletRequest;
		this.httpServletResponse = httpServletResponse;
		this.requestMessage = null;
	}

	/**
	 * 
	 * @param httpServletRequest
	 *            REQUIRED
	 * @param httpServletResponse
	 *            REQUIRED
	 * @param requestMessage
	 *            REQUIRED
	 */
	public WebCommandContext(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final T requestMessage) {
		Assert.notNull(httpServletRequest, "httpServletRequest is required");
		Assert.notNull(httpServletResponse, "httpServletResponse is required");
		this.httpServletRequest = httpServletRequest;
		this.httpServletResponse = httpServletResponse;
		Assert.notNull(requestMessage, "requestMessage is required");
		this.requestMessage = requestMessage;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}

	public T getRequestMessage() {
		return requestMessage;
	}

	public V getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(final V responseMessage) {
		this.responseMessage = responseMessage;
	}

}
