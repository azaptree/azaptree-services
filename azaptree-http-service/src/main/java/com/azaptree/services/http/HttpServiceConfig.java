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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jetty.server.Handler;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.Assert;

public class HttpServiceConfig implements BeanNameAware {
	private String name;

	private final Executor requestExcecutor;
	private ExecutorService requestExcecutorService;

	private final int port;

	private Integer requestBufferSize;
	private Integer requestHeaderBufferSize;
	private Integer responseBufferSize;

	private final String contextPath;

	private final Handler httpRequestHandler;

	public HttpServiceConfig(final Handler httpRequestHandler) {
		Assert.notNull(httpRequestHandler, "httpRequestHandler is required");
		this.httpRequestHandler = httpRequestHandler;
		requestExcecutorService = Executors.newCachedThreadPool();
		requestExcecutor = requestExcecutorService;
		final String PORT = System.getenv("PORT");
		if (StringUtils.isNotBlank(PORT)) {
			port = Integer.parseInt(PORT);
		} else {
			port = 8080;
		}
		contextPath = "/";
	}

	public HttpServiceConfig(final Handler httpRequestHandler, final Executor requestExcecutor, final int port, final String contextPath) {
		Assert.notNull(httpRequestHandler, "httpRequestHandler is required");
		this.httpRequestHandler = httpRequestHandler;
		Assert.notNull(requestExcecutor, "requestExcecutor is required");
		Assert.isTrue(port > 0, "port must be > 0");
		Assert.hasText(contextPath, "contextPath is required");
		this.requestExcecutor = requestExcecutor;
		this.port = port;
		this.contextPath = contextPath;
	}

	@PreDestroy
	public void destroy() throws InterruptedException {
		if (requestExcecutorService != null) {
			requestExcecutorService.shutdown();
			while (!requestExcecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
				LoggerFactory.getLogger(getClass()).info("Waiting for requestExcecutorService tasks to complete");
			}
		}
	}

	public String getContextPath() {
		return contextPath;
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

	public Executor getRequestExcecutor() {
		return requestExcecutor;
	}

	public Integer getRequestHeaderBufferSize() {
		return requestHeaderBufferSize;
	}

	public Integer getResponseBufferSize() {
		return responseBufferSize;
	}

	@Override
	public void setBeanName(final String name) {
		this.name = name;
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
		sb.append("contextPath", contextPath);
		sb.append("requestExcecutor", requestExcecutor);
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
