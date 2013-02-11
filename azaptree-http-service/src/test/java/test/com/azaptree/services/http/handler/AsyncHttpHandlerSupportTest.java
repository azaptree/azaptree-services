package test.com.azaptree.services.http.handler;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.http.HttpService;
import com.azaptree.services.http.HttpServiceConfig;
import com.azaptree.services.http.handler.AsyncHttpHandlerSupport;
import com.azaptree.services.http.impl.ExecutorThreadPoolWithGracefulShutdown;
import com.azaptree.services.http.impl.HttpServiceImpl;

@ContextConfiguration(classes = { AsyncHttpHandlerSupportTest.Config.class })
public class AsyncHttpHandlerSupportTest extends AbstractTestNGSpringContextTests {
	public static class AsyncHandler extends AsyncHttpHandlerSupport {

		final AtomicInteger requestCounter = new AtomicInteger();

		public AsyncHandler(final Executor executor) {
			super(executor);
		}

		@Override
		protected void postProcess(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
		        throws IOException,
		        ServletException {
			final String postProcess = request.getParameter("postProcess");
			if (StringUtils.equals(postProcess, "true")) {
				response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().print("request has been submitted: " + target);
				baseRequest.setHandled(true);
			}
		}

		@Override
		protected Map<String, Object> preProcess(final String target, final Request baseRequest, final HttpServletRequest request,
		        final HttpServletResponse response) throws IOException, ServletException {
			final String action = request.getParameter("action");

			if (StringUtils.isBlank(action)) {
				response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print("Invalid request. action is required: " + target);
				baseRequest.setHandled(true);
				return null;
			}

			final Map<String, Object> ctx = new HashMap<>();
			ctx.put("action", action);
			return ctx;
		}

		@Override
		protected void process(final String target, final Map<String, Object> context) {
			log.info("Handled request: {}", requestCounter.incrementAndGet());
		}

	}

	@Configuration
	public static class Config {

		@Bean(destroyMethod = "shutdown")
		public ExecutorService executorService() {
			return Executors.newCachedThreadPool();
		}

		@Bean(destroyMethod = "stop")
		public HttpClient httpClient() throws Exception {
			final HttpClient client = new HttpClient();
			client.setThreadPool(new ExecutorThreadPoolWithGracefulShutdown(Executors.newCachedThreadPool(), 30));
			client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
			client.start();
			return client;
		}

		@Bean
		public HttpService httpService() {
			return new HttpServiceImpl(httpServiceConfig());
		}

		@Bean
		public HttpServiceConfig httpServiceConfig() {
			final HttpServiceConfig config = new HttpServiceConfig("http-service", new AsyncHandler(executorService()), Executors.newCachedThreadPool(), 8090);
			config.setGracefulShutdownTimeoutSecs(30);
			config.setRequestBufferSize(8096);
			config.setRequestHeaderBufferSize(8096);
			config.setResponseBufferSize(8096);
			return config;
		}

	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private HttpService httpService;

	@Autowired
	private HttpServiceConfig httpServiceConfig;

	@Autowired
	private HttpClient client;

	@Test
	public void testAsyncHandler() throws IOException, InterruptedException {
		int requestCounter = 0;

		final AsyncHandler handler = (AsyncHandler) httpServiceConfig.getHttpRequestHandler();

		ContentExchange exchange = new ContentExchange(true);
		exchange.setMethod("GET");
		String url = String.format("http://localhost:%d/testAsyncHandler?action=test", httpServiceConfig.getPort());
		exchange.setURL(url);
		client.send(exchange);
		requestCounter++;

		int exchangeState = exchange.waitForDone();
		log.info("{status = {}, content = '{}'}", exchange.getResponseStatus(), exchange.getResponseContent());
		Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);
		Assert.assertEquals(exchange.getResponseStatus(), HttpServletResponse.SC_OK);
		Assert.assertTrue(StringUtils.isBlank(exchange.getResponseContent()));

		Thread.sleep(10);
		Assert.assertEquals(handler.requestCounter.get(), requestCounter);

		exchange = new ContentExchange(true);
		exchange.setMethod("GET");
		url = String.format("http://localhost:%d/testAsyncHandler?action=test&postProcess=true", httpServiceConfig.getPort());
		exchange.setURL(url);
		client.send(exchange);
		requestCounter++;

		exchangeState = exchange.waitForDone();
		log.info("{status = {}, content = '{}'}", exchange.getResponseStatus(), exchange.getResponseContent());
		Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);
		Assert.assertEquals(exchange.getResponseStatus(), HttpServletResponse.SC_OK);
		Assert.assertTrue(StringUtils.isNotBlank(exchange.getResponseContent()));

		Thread.sleep(10);
		Assert.assertEquals(handler.requestCounter.get(), requestCounter);

		exchange = new ContentExchange(true);
		exchange.setMethod("GET");
		url = String.format("http://localhost:%d/testAsyncHandler", httpServiceConfig.getPort());
		exchange.setURL(url);
		client.send(exchange);

		exchangeState = exchange.waitForDone();
		log.info("{status = {}, content = '{}'}", exchange.getResponseStatus(), exchange.getResponseContent());
		Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);
		Assert.assertEquals(exchange.getResponseStatus(), HttpServletResponse.SC_BAD_REQUEST);
		Assert.assertTrue(StringUtils.isNotBlank(exchange.getResponseContent()));

		Thread.sleep(10);
		Assert.assertEquals(handler.requestCounter.get(), requestCounter);
	}
}
