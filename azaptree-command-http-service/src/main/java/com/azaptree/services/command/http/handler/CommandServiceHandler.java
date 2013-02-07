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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.chain.Command;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.CommandKey;
import com.azaptree.services.command.CommandService;
import com.azaptree.services.command.http.WebCommandContext;
import com.azaptree.services.command.http.WebXmlRequestCommand;
import com.azaptree.services.http.handler.AsyncSuspendContinueHttpHandlerSupport;
import com.azaptree.services.http.headers.ResponseMessageHeaders;
import com.azaptree.wadl.Application;
import com.azaptree.wadl.Doc;
import com.azaptree.wadl.Grammars;
import com.azaptree.wadl.Include;
import com.azaptree.wadl.Method;
import com.azaptree.wadl.Representation;
import com.azaptree.wadl.Resource;
import com.azaptree.wadl.Resources;
import com.azaptree.wadl.Response;
import com.google.common.collect.ImmutableMap;

/**
 * REST API:
 * 
 * <code>
 * GET  /command-service.wadl						returns WADL which describes the REST API
 * GET  /command-service/{catalog}/{command}.xsd	returns command XSD, which describes the command's XML messages
 * POST /command-service/{catalog}/{command}		executes the command - the command XML request message is provided in the HTTP request body		
 * </code>
 * 
 * @author alfio
 * 
 */
public class CommandServiceHandler extends AsyncSuspendContinueHttpHandlerSupport {

	@Autowired
	private CommandService commandService;

	private Map<String /* target uri */, CommandKey> targetUriCommandKeyMap;

	private final String applicationHttpUrlBase;

	/**
	 * 
	 * @param executor
	 * @param continuationTimeoutMillis
	 * @param applicationHttpUrlBase
	 *            used when generating the WADL to specify the base HTTP url for <code>/application/resources/@base</code>, e.g.
	 *            http://localhost:8080/command-service/
	 */
	public CommandServiceHandler(final Executor executor, final long continuationTimeoutMillis, final String applicationHttpUrlBase) {
		super(executor, continuationTimeoutMillis);
		Assert.hasText(applicationHttpUrlBase, "applicationHttpUrlBase is required");
		this.applicationHttpUrlBase = applicationHttpUrlBase;
	}

	/**
	 * 
	 * @param executor
	 * @param applicationHttpUrlBase
	 *            used when generating the WADL to specify the base HTTP url for <code>/application/resources/@base</code>, e.g.
	 *            http://localhost:8080/command-service/
	 */
	public CommandServiceHandler(final Executor executor, final String applicationHttpUrlBase) {
		super(executor);
		Assert.hasText(applicationHttpUrlBase, "applicationHttpUrlBase is required");
		this.applicationHttpUrlBase = applicationHttpUrlBase;
	}

	private Grammars createWadlGrammars() {
		final Grammars grammars = new Grammars();
		final List<CommandKey> keys = new ArrayList<>(targetUriCommandKeyMap.values());
		Collections.sort(keys);
		for (final CommandKey commandKey : keys) {
			final WebXmlRequestCommand<?, ?> command = (WebXmlRequestCommand<?, ?>) commandService.getCommand(commandKey);
			if (command.hasXmlSchema()) {
				final Include include = new Include();
				include.setHref(String.format("%s/command-service/%s/%s.xsd", applicationHttpUrlBase, commandKey.getCatalogName(), commandKey.getCommandName()));
				grammars.getInclude().add(include);
			}
		}
		return grammars;
	}

	private Resources createWadlResources(final String catalogName) {
		final Resources resources = new Resources();
		resources.setBase(String.format("%s/command-service/%s/", applicationHttpUrlBase, catalogName));
		final CommandCatalog catalog = commandService.getCommandCatalog(catalogName);
		for (final String commandName : catalog.getCommandNames()) {
			final WebXmlRequestCommand<?, ?> command = (WebXmlRequestCommand<?, ?>) commandService.getCommand(new CommandKey(catalogName, commandName));
			resources.getResource().add(command.createCommandResourceWadl());

			if (command.hasXmlSchema()) {
				final Resource resource = new Resource();
				resource.setPath(commandName + ".xsd");
				resources.getResource().add(resource);

				final Doc doc = new Doc();
				doc.getContent().add("The command's XML schema is returned");
				resource.getDoc().add(doc);

				final Method method = new Method();
				method.setName("GET");
				resource.getMethodOrResource().add(method);

				final Response response = new Response();
				method.getResponse().add(response);
				response.getStatus().add(Long.valueOf(HttpStatus.OK_200));
				final Representation representation = new Representation();
				representation.setMediaType("application/xml");
				response.getRepresentation().add(representation);
			}

		}

		return resources;
	}

	protected WebCommandContext<?, ?> createWebCommandContext(final WebXmlRequestCommand<?, ?> command, final Request baseRequest,
	        final HttpServletRequest request,
	        final HttpServletResponse response) throws JAXBException, IOException {
		if (command.getRequestClass().isPresent()) {
			final JAXBContext ctx = command.getJaxbContext().get();
			final Object requestMessage;

			final Unmarshaller unmarshaller = ctx.createUnmarshaller();
			requestMessage = unmarshaller.unmarshal(baseRequest.getInputStream());

			if (requestMessage instanceof JAXBElement) {
				return new WebCommandContext<>(request, response, ((JAXBElement<?>) requestMessage).getValue());
			}
			return new WebCommandContext<>(request, response, requestMessage);
		}
		return new WebCommandContext<>(request, response);
	}

	private void generateCommandXSD(final String target, final HttpServletResponse response) throws IOException {
		final CommandKey commandKey = targetUriCommandKeyMap.get(toCommandUriTarget(target));
		final CommandCatalog catalog = commandService.getCommandCatalog(commandKey.getCatalogName());
		@SuppressWarnings("rawtypes")
		final WebXmlRequestCommand command = (WebXmlRequestCommand) catalog.getCommand(commandKey.getCommandName());
		if (!command.hasXmlSchema()) {
			response.setStatus(HttpStatus.NO_CONTENT_204);
			return;
		}
		response.setStatus(HttpStatus.OK_200);
		response.setContentType("application/xml");
		response.setCharacterEncoding("UTF-8");
		command.generateSchema(response.getOutputStream());
	}

	private void generateWADL(final Request baseRequest, final HttpServletResponse response) {
		// TODO: implement HTTP caching
		response.setStatus(HttpStatus.OK_200);
		response.setContentType("application/vnd.sun.wadl+xml");

		final Application app = new Application();
		app.setGrammars(createWadlGrammars());
		for (String catalogName : commandService.getCommandCatalogNames()) {
			app.getResources().add(createWadlResources(catalogName));
		}

		try {
			final JAXBContext wadlJaxbContext = JAXBContext.newInstance(Application.class.getPackage().getName());
			final Marshaller marshaller = wadlJaxbContext.createMarshaller();
			marshaller.marshal(app, response.getOutputStream());
		} catch (JAXBException | IOException e) {
			throw new IllegalStateException("Failed to marshall WADL to HTTP response output stream", e);
		}

		baseRequest.setHandled(true);
	}

	private void handleCommandExecutionError(final Request baseRequest, final HttpServletResponse response, final Exception exception) {
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
		final StringBuilder sb = new StringBuilder("Failed to execute command : ").append(exception.getMessage());
		response.setHeader(ResponseMessageHeaders.STATUS_MSG.header, sb.toString());
		baseRequest.setHandled(true);
	}

	private void handleGET(final String target, final Request baseRequest, final HttpServletResponse response)
	        throws IOException {
		if (StringUtils.equals(target, "/command-service.wadl")) {
			generateWADL(baseRequest, response);
			baseRequest.setHandled(true);
		} else if (targetUriCommandKeyMap.containsKey(toCommandUriTarget(target))) {
			generateCommandXSD(target, response);
			baseRequest.setHandled(true);
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

	private void handlePOST(final String target, final Request baseRequest, final HttpServletResponse response) {
		if (!targetUriCommandKeyMap.containsKey(target)) {
			handleInvalidUri(baseRequest, response);
		}
	}

	private void handleUnmarshallingRequestMessageError(final Request baseRequest, final HttpServletResponse response, final Exception exception) {
		response.setStatus(HttpStatus.BAD_REQUEST_400);
		final StringBuilder sb = new StringBuilder("Failed to unmarshall request message : ").append(exception.getMessage());
		response.setHeader(ResponseMessageHeaders.STATUS_MSG.header, sb.toString());
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
				if (cmd instanceof WebXmlRequestCommand) {
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

	@SuppressWarnings("rawtypes")
	@Override
	protected void process(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
		final CommandKey commandKey = targetUriCommandKeyMap.get(target);
		final WebXmlRequestCommand command = (WebXmlRequestCommand) commandService.getCommand(commandKey);
		final WebCommandContext<?, ?> commandContext;
		try {
			commandContext = createWebCommandContext(command, baseRequest, request, response);
		} catch (JAXBException | IOException e) {
			log.error(String.format("%s : Failed to unmarshal request message", target), e);
			handleUnmarshallingRequestMessageError(baseRequest, response, e);
			return;
		}

		try {
			command.execute(commandContext);
		} catch (final Exception e) {
			log.error(String.format("%s : Failed to execute command : %s", target, commandKey), e);
			handleCommandExecutionError(baseRequest, response, e);
		}
	}

	private String toCommandUriTarget(final String target) {
		if (target.endsWith(".xsd")) {
			return target.substring(0, target.length() - 4);
		}

		return target;
	}
}
