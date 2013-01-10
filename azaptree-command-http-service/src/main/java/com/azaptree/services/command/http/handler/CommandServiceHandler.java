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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.springframework.beans.factory.annotation.Autowired;

import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.CommandKey;
import com.azaptree.services.command.CommandService;
import com.azaptree.services.command.http.WebRequestCommand;
import com.azaptree.services.http.handler.AsyncSuspendContinueHttpHandlerSupport;
import com.google.common.collect.ImmutableMap;

/**
 * REST API:
 * 
 * <code>
 * GET  /										returns WADL which describes the REST API
 * GET  /command-service/{catalog}/{command}	returns command XSD, which describes the command's XML messages
 * POST /command-service/{catalog}/{command}	executes the command - the command XML request message is provided in the HTTP request body		
 * </code>
 * 
 * @author alfio
 * 
 */
public class CommandServiceHandler extends AsyncSuspendContinueHttpHandlerSupport {

	@Autowired
	private CommandService commandService;

	private Map<String /* target uri */, CommandKey> targetUriCommandKeyMap;

	public CommandServiceHandler(final Executor executor) {
		super(executor);
	}

	public CommandServiceHandler(final Executor executor, final long continuationTimeoutMillis) {
		super(executor, continuationTimeoutMillis);
	}

	private void generateCommandXSD(final String target, final HttpServletResponse response)
	        throws IOException {
		final CommandKey commandKey = targetUriCommandKeyMap.get(target);
		final CommandCatalog catalog = commandService.getCommandCatalog(commandKey.getCatalogName());
		@SuppressWarnings("rawtypes")
		final WebRequestCommand command = (WebRequestCommand) catalog.getCommand(commandKey.getCommandName());
		response.setStatus(HttpStatus.OK_200);
		response.setContentType(MimeTypes.TEXT_XML_UTF_8);
		command.generateSchema(response.getOutputStream());
	}

	private void generateWADL(final Request baseRequest, final HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND_404);
		response.setContentType(MimeTypes.TEXT_XML_UTF_8);
		// TODO: generate WADL
		baseRequest.setHandled(true);
	}

	@PostConstruct
	private void init() {
		final Map<String, CommandKey> map = new HashMap<>();

		final String[] catalogNames = commandService.getCommandCatalogNames();
		for (final String catalogName : catalogNames) {
			final CommandCatalog catalog = commandService.getCommandCatalog(catalogName);
			final String[] commandNames = catalog.getCommandNames();
			for (final String command : commandNames) {
				final Command cmd = catalog.getCommand(command);
				if (cmd instanceof WebRequestCommand) {
					final String uri = String.format("/command-service/%s/%s", catalogName, command);
					final CommandKey key = new CommandKey(catalogName, command);
					map.put(uri, key);
					log.info("WebRequestCommand uri: {}", uri);
				}
			}
		}

		targetUriCommandKeyMap = ImmutableMap.<String, CommandKey> builder().putAll(map).build();
	}

	@Override
	protected void preProcess(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException, ServletException {
		final String method = baseRequest.getMethod();
		if ("GET".equals(method)) {
			handleGET(target, baseRequest, response);
		} else if ("POST".equals(method)) {
			handlePOST(target, baseRequest, response);
		} else {
			handleMethodNotAllowed(baseRequest, response);
		}
	}

	private void handlePOST(String target, Request baseRequest, HttpServletResponse response) {
		if (!targetUriCommandKeyMap.containsKey(target)) {
			handleInvalidUri(baseRequest, response);
		}
	}

	private void handleGET(final String target, final Request baseRequest, final HttpServletResponse response)
	        throws IOException {
		if (StringUtils.equals(target, "/")) {
			generateWADL(baseRequest, response);
		} else if (targetUriCommandKeyMap.containsKey(target)) {
			generateCommandXSD(target, response);
		} else {
			handleInvalidUri(baseRequest, response);
		}
	}

	private void handleInvalidUri(final Request baseRequest, final HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND_404);
		baseRequest.setHandled(true);
	}

	private void handleMethodNotAllowed(final Request baseRequest, final HttpServletResponse response) {
		response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
		response.setHeader(HttpHeaders.ALLOW, "GET, POST");
		baseRequest.setHandled(true);
	}

	@Override
	protected void process(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
		// TODO Auto-generated method stub
	}

}
