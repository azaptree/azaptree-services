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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.command.CommandCatalog;
import com.azaptree.services.command.CommandService;
import com.azaptree.services.command.http.WebCommandContext;
import com.azaptree.services.command.http.WebRequestCommand;
import com.azaptree.services.command.impl.CommandCatalogImpl;
import com.azaptree.services.command.impl.CommandServiceImpl;
import com.azaptree.services.command.messages.AdditionRequestMessage;
import com.azaptree.services.command.messages.AdditionResponseMessage;
import com.azaptree.services.command.messages.ObjectFactory;
import com.azaptree.services.http.HttpService;
import com.azaptree.services.http.HttpServiceConfig;
import com.azaptree.services.http.impl.ExecutorThreadPoolWithGracefulShutdown;
import com.azaptree.services.http.impl.HttpServiceImpl;
import com.google.common.base.Optional;

@ContextConfiguration(classes = { CommandServiceHandlerTest.Config.class })
public class CommandServiceHandlerTest extends AbstractTestNGSpringContextTests {
	@Configuration
	public static class Config {
		@Bean
		WebRequestCommand<AdditionRequestMessage, AdditionResponseMessage> addNumbersCommand() {
			return new WebRequestCommand<AdditionRequestMessage, AdditionResponseMessage>(AdditionRequestMessage.class, AdditionResponseMessage.class) {

				@Override
				protected boolean executeCommand(final WebCommandContext<AdditionRequestMessage, AdditionResponseMessage> ctx) {
					final HttpServletResponse response = ctx.getHttpServletResponse();
					response.setStatus(HttpStatus.OK_200);
					response.setCharacterEncoding("UTF-8");
					response.setContentType("application/xml");

					final AdditionRequestMessage requestMessage = ctx.getRequestMessage();
					double sum = 0;
					for (final double number : requestMessage.getNumber()) {
						sum += number;
					}

					final AdditionResponseMessage responseMessage = new AdditionResponseMessage();
					responseMessage.setSum(sum);
					ctx.setResponseMessage(responseMessage);
					final ObjectFactory objectFactory = new ObjectFactory();
					writeResponseMessage(ctx.getHttpServletResponse(), objectFactory.createAddNumbersResponse(responseMessage));
					return true;
				}

				@Override
				public Optional<QName> getRequestXmlElement() {
					final ObjectFactory objectFactory = new ObjectFactory();
					final QName elemName = objectFactory.createAddNumbersRequest(objectFactory.createAdditionRequestMessage()).getName();
					return Optional.of(elemName);
				}

				@Override
				public Optional<QName> getResponseXmlElement() {
					final ObjectFactory objectFactory = new ObjectFactory();
					final QName elemName = objectFactory.createAddNumbersResponse(objectFactory.createAdditionResponseMessage()).getName();
					return Optional.of(elemName);
				}
			};
		}

		@Bean
		CommandCatalog commandCatalog() {
			return new CommandCatalogImpl("CommandServiceHandlerTest",
			        helloWorldCommand(),
			        addNumbersCommand());
		}

		@Bean
		CommandService commandService() {
			return new CommandServiceImpl();
		}

		@Bean
		CommandServiceHandler commandServiceHandler() {
			return new CommandServiceHandler(executor(), "http://localhost:8080");
		}

		@Bean(destroyMethod = "shutdown")
		Executor executor() {
			return Executors.newCachedThreadPool();
		}

		@SuppressWarnings("rawtypes")
		@Bean
		WebRequestCommand helloWorldCommand() {
			return new WebRequestCommand() {

				@Override
				protected boolean executeCommand(final WebCommandContext ctx) {
					final HttpServletResponse response = ctx.getHttpServletResponse();
					response.setStatus(HttpStatus.OK_200);
					response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
					try (final PrintWriter pw = response.getWriter()) {
						pw.print("TIMESTAMP :");
						pw.print(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(System.currentTimeMillis()));
					} catch (final IOException e) {
						throw new IllegalStateException("Failed to obtain HTTP response writer", e);
					}

					return true;
				}

				@Override
				public Optional getRequestXmlElement() {
					return Optional.absent();
				}

				@Override
				public Optional getResponseXmlElement() {
					return Optional.absent();
				}
			};
		}

		@Bean(destroyMethod = "stop")
		HttpClient httpClient() throws Exception {
			final HttpClient client = new HttpClient();
			client.setThreadPool(new ExecutorThreadPoolWithGracefulShutdown(Executors.newCachedThreadPool(), 30));
			client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
			client.start();
			return client;
		}

		@Bean
		HttpService httpService() {
			return new HttpServiceImpl(httpServiceConfig());
		}

		@Bean
		HttpServiceConfig httpServiceConfig() {
			return new HttpServiceConfig("command-service", commandServiceHandler());
		}

	}

	public static String prettyFormat(final String input, final int indent) {
		try {
			final Source xmlInput = new StreamSource(new StringReader(input));
			final StringWriter stringWriter = new StringWriter();
			final StreamResult xmlOutput = new StreamResult(stringWriter);
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			final Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (final Exception e) {
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private HttpServiceConfig httpSericeConfig;

	@Autowired
	private HttpClient client;

	@Resource(name = "addNumbersCommand")
	private WebRequestCommand<AdditionRequestMessage, AdditionResponseMessage> addNumbersCommand;

	@Test
	public void test_addNumbersCommand() throws IOException, JAXBException, InterruptedException {
		final ContentExchange contentExchange = new ContentExchange(true);
		contentExchange.setMethod("POST");
		final String commandCatalogName = "CommandServiceHandlerTest";
		final String commandName = "addNumbersCommand";
		contentExchange.setURL(String.format("http://localhost:%d/command-service/%s/%s", httpSericeConfig.getPort(), commandCatalogName, commandName));
		contentExchange.setRequestContentType("application/xml");

		final AdditionRequestMessage requestMessage = new AdditionRequestMessage();
		requestMessage.getNumber().add(1d);
		requestMessage.getNumber().add(2d);
		requestMessage.getNumber().add(3d);
		final Marshaller marshaller = addNumbersCommand.getJaxbContext().get().createMarshaller();
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ObjectFactory objectFactory = new ObjectFactory();
		marshaller.marshal(objectFactory.createAddNumbersRequest(requestMessage), bos);
		System.out.println(bos.toString());

		contentExchange.setRequestContent(new ByteArrayBuffer(bos.toByteArray()));
		client.send(contentExchange);

		contentExchange.waitForDone();
		Assert.assertEquals(contentExchange.getResponseStatus(), HttpStatus.OK_200);

		final byte[] responseContent = contentExchange.getResponseContentBytes();
		final ByteArrayInputStream bis = new ByteArrayInputStream(responseContent);
		log.info("test_addNumbersCommand(): response content: {}", new String(responseContent));
		final Unmarshaller unmarshaller = addNumbersCommand.getJaxbContext().get().createUnmarshaller();

		final JAXBElement<AdditionResponseMessage> responseMessage = (JAXBElement<AdditionResponseMessage>) unmarshaller.unmarshal(bis);
		Assert.assertEquals(responseMessage.getValue().getSum(), 1d + 2d + 3d);
	}

	@Test
	public void test_getCommandWithNoXSD() throws IOException, InterruptedException {
		final ContentExchange contentExchange = new ContentExchange(true);
		contentExchange.setMethod("GET");
		final String commandCatalogName = "CommandServiceHandlerTest";
		final String commandName = "helloWorldCommand";
		contentExchange.setURL(String.format("http://localhost:%d/command-service/%s/%s.xsd", httpSericeConfig.getPort(), commandCatalogName, commandName));

		client.send(contentExchange);

		contentExchange.waitForDone();
		Assert.assertEquals(contentExchange.getResponseStatus(), HttpStatus.NO_CONTENT_204);

		final byte[] responseContent = contentExchange.getResponseContentBytes();
		Assert.assertTrue(ArrayUtils.isEmpty(responseContent));
	}

	@Test
	public void test_getCommandXSD() throws IOException, InterruptedException {
		final ContentExchange contentExchange = new ContentExchange(true);
		contentExchange.setMethod("GET");
		final String commandCatalogName = "CommandServiceHandlerTest";
		final String commandName = "addNumbersCommand";
		contentExchange.setURL(String.format("http://localhost:%d/command-service/%s/%s.xsd", httpSericeConfig.getPort(), commandCatalogName, commandName));

		client.send(contentExchange);

		contentExchange.waitForDone();
		Assert.assertEquals(contentExchange.getResponseStatus(), HttpStatus.OK_200);

		final byte[] responseContent = contentExchange.getResponseContentBytes();
		final String xsd = new String(responseContent, "UTF-8");
		Assert.assertTrue(StringUtils.isNotBlank(xsd));
		log.info("test_getCommandXSD() xsd :\n{}", prettyFormat(xsd, 4));
	}

	@Test
	public void test_getWADL() throws IOException, InterruptedException {
		final ContentExchange contentExchange = new ContentExchange(true);
		contentExchange.setMethod("GET");
		contentExchange.setURL(String.format("http://localhost:%d/command-service.wadl", httpSericeConfig.getPort()));

		client.send(contentExchange);

		contentExchange.waitForDone();
		Assert.assertEquals(contentExchange.getResponseStatus(), HttpStatus.OK_200);

		final byte[] responseContent = contentExchange.getResponseContentBytes();
		final String xsd = new String(responseContent, "UTF-8");
		Assert.assertTrue(StringUtils.isNotBlank(xsd));
		log.info("test_getWADL() xsd :\n{}", prettyFormat(xsd, 4));
	}

	@Test
	public void test_helloWorldCommand() throws IOException, InterruptedException {
		final ContentExchange contentExchange = new ContentExchange(true);
		contentExchange.setMethod("POST");
		final String commandCatalogName = "CommandServiceHandlerTest";
		final String commandName = "helloWorldCommand";
		contentExchange.setURL(String.format("http://localhost:%d/command-service/%s/%s", httpSericeConfig.getPort(), commandCatalogName, commandName));
		client.send(contentExchange);

		contentExchange.waitForDone();
		Assert.assertEquals(contentExchange.getResponseStatus(), HttpStatus.OK_200);
		final String responseMsg = contentExchange.getResponseContent();
		log.info("test_helloWorldCommand() : responseMsg : {}", responseMsg);
		Assert.assertTrue(StringUtils.startsWith(responseMsg, "TIMESTAMP :"));
	}
}
