package com.azaptree.services.http;

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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jetty.server.Handler;
import org.springframework.util.Assert;

public class HttpServiceConfig {
	private final String name;

	private final ExecutorService requestExcecutorService;

	private final int port;

	private Integer requestBufferSize;
	private Integer requestHeaderBufferSize;
	private Integer responseBufferSize;

	private final Handler httpRequestHandler;

	private int gracefulShutdownTimeoutSecs = 30;

	public HttpServiceConfig(final String name, final Handler httpRequestHandler) {
		Assert.hasText(name, "name is required");
		Assert.notNull(httpRequestHandler, "httpRequestHandler is required");
		this.name = name;
		this.httpRequestHandler = httpRequestHandler;
		requestExcecutorService = Executors.newCachedThreadPool();
		final String PORT = System.getenv("PORT");
		if (StringUtils.isNotBlank(PORT)) {
			port = Integer.parseInt(PORT);
		} else {
			port = 8080;
		}
	}

	public HttpServiceConfig(final String name, final Handler httpRequestHandler, final ExecutorService requestExcecutor, final int port) {
		Assert.hasText(name, "name is required");
		Assert.notNull(httpRequestHandler, "httpRequestHandler is required");
		this.name = name;
		this.httpRequestHandler = httpRequestHandler;
		Assert.notNull(requestExcecutor, "requestExcecutor is required");
		Assert.isTrue(port > 0, "port must be > 0");
		requestExcecutorService = requestExcecutor;
		this.port = port;
	}

	/**
	 * The graceful shutdown timeout. If set, the server will not immediately stop. Instead, all Connectors will be closed so that
	 * new connections will not be accepted and all handlers that implement Server.Graceful will be put into the shutdown mode so that no new requests will be
	 * accepted, but existing requests can complete. The server will then wait the configured timeout before stopping.
	 * 
	 * @return
	 */
	public int getGracefulShutdownTimeoutSecs() {
		return gracefulShutdownTimeoutSecs;
	}

	public Handler getHttpRequestHandler() {
		return httpRequestHandler;
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public Integer getRequestBufferSize() {
		return requestBufferSize;
	}

	public ExecutorService getRequestExcecutor() {
		return requestExcecutorService;
	}

	public Integer getRequestHeaderBufferSize() {
		return requestHeaderBufferSize;
	}

	public Integer getResponseBufferSize() {
		return responseBufferSize;
	}

	/**
	 * Set graceful shutdown timeout. If set, the server will not immediately stop. Instead, all Connectors will be closed so that
	 * new connections will not be accepted and all handlers that implement Server.Graceful will be put into the shutdown mode so that no new requests will be
	 * accepted, but existing requests can complete. The server will then wait the configured timeout before stopping.
	 * 
	 * @param gracefulShutdownTimeoutSecs
	 *            the seconds to wait for existing request to complete before stopping the server.
	 */
	public void setGracefulShutdownTimeoutSecs(final int gracefulShutdownTimeoutSecs) {
		this.gracefulShutdownTimeoutSecs = gracefulShutdownTimeoutSecs;
	}

	public void setRequestBufferSize(final Integer requestBufferSize) {
		this.requestBufferSize = requestBufferSize;
	}

	public void setRequestHeaderBufferSize(final Integer requestHeaderBufferSize) {
		this.requestHeaderBufferSize = requestHeaderBufferSize;
	}

	public void setResponseBufferSize(final Integer responseBufferSize) {
		this.responseBufferSize = responseBufferSize;
	}

	@Override
	public String toString() {
		final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
		sb.append("name", name);
		sb.append("port", port);
		sb.append("requestExcecutorService", requestExcecutorService);
		if (requestBufferSize != null) {
			sb.append("requestBufferSize", requestBufferSize);
		}
		if (requestHeaderBufferSize != null) {
			sb.append("requestHeaderBufferSize", requestHeaderBufferSize);
		}
		if (responseBufferSize != null) {
			sb.append("responseBufferSize", responseBufferSize);
		}
		return sb.toString();
	}

}
