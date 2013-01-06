package com.azaptree.services.http.impl;

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

import javax.annotation.PreDestroy;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.azaptree.services.http.HttpService;
import com.azaptree.services.http.HttpServiceConfig;
import com.google.common.util.concurrent.AbstractIdleService;

public class HttpServiceImpl extends AbstractIdleService implements HttpService {

	private final Server server;

	private final HttpServiceConfig httpServiceConfig;

	/**
	 * The DefaultHandler is always added as the last Handler in the HandlerList.
	 * This handle will deal with unhandled requests in the server.
	 * For requests for favicon.ico, the Jetty icon is served.
	 * For reqests to '/' a 404 with a list of known contexts is served.
	 * For all other requests a normal 404 is served.
	 * 
	 * @param config
	 */
	public HttpServiceImpl(final HttpServiceConfig config) {
		Assert.notNull(config, "config is required");
		httpServiceConfig = config;
		getLogger().info("config: {}", config);
		server = new Server();
		server.addConnector(createSelectChannelConnector(config));
		final HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(new Handler[] { config.getHttpRequestHandler(), new DefaultHandler() });
		server.setHandler(handlerList);
	}

	private SelectChannelConnector createSelectChannelConnector(final HttpServiceConfig config) {
		final SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(config.getPort());
		connector.setName(config.getName());
		if (config.getRequestBufferSize() != null) {
			connector.setRequestBufferSize(config.getRequestBufferSize());
		}
		if (config.getRequestHeaderBufferSize() != null) {
			connector.setRequestHeaderSize(config.getRequestHeaderBufferSize());
		}
		if (config.getResponseBufferSize() != null) {
			connector.setResponseBufferSize(config.getResponseBufferSize());
		}

		return connector;
	}

	@PreDestroy
	public void destroy() {
		server.destroy();
	}

	@Override
	public HttpServiceConfig getHttpServiceConfig() {
		return httpServiceConfig;
	}

	protected Logger getLogger() {
		return LoggerFactory.getLogger(HttpService.class);
	}

	@Override
	protected void shutDown() throws Exception {
		server.stop();
	}

	@Override
	protected void startUp() throws Exception {
		server.start();
	}

}
