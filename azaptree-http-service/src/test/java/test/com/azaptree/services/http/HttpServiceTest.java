package test.com.azaptree.services.http;

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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
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
import com.azaptree.services.http.impl.ExecutorThreadPoolWithGracefulShutdown;
import com.azaptree.services.http.impl.HttpServiceImpl;

@ContextConfiguration(classes = { HttpServiceTest.Config.class })
public class HttpServiceTest extends AbstractTestNGSpringContextTests {
	public static class AsyncHttpHandler extends AbstractHandler {
		final Logger log = LoggerFactory.getLogger(getClass());

		final int workTime;

		private final AtomicInteger requestCounter = new AtomicInteger();

		final Executor executor;

		public AsyncHttpHandler(final int workTime, final Executor executor) {
			super();
			this.workTime = workTime;
			this.executor = executor;
		}

		@Override
		public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
		        throws IOException, ServletException {
			final AsyncContext asyncCtx = baseRequest.startAsync();
			executor.execute(new Runnable() {

				@Override
				public void run() {
					if (workTime > 0) {
						try {
							log.info("sleeping for {} seconds ", workTime);
							Thread.sleep(workTime * 1000);
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
					}

					final HttpServletRequest request = (HttpServletRequest) asyncCtx.getRequest();
					final HttpServletResponse response = (HttpServletResponse) asyncCtx.getResponse();

					try {
						final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
						sb.append("target", target);
						sb.append("request.getContextPath()", request.getContextPath());
						sb.append("request.getRequestURI()", request.getRequestURI());
						sb.append("request.getQueryString()", request.getQueryString());
						final String requestInfo = sb.toString();
						log.info("request : {}", requestInfo);
						response.setContentType("text/plain;charset=utf-8");
						response.setStatus(HttpServletResponse.SC_OK);
						response.getWriter().print(requestInfo);
						log.info("requestCounter = {}", requestCounter.incrementAndGet());
						baseRequest.setHandled(true);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						asyncCtx.complete();
					}
				}
			});

		}
	}

	@Configuration
	public static class Config {

		@Bean
		public Handler asyncHttpHandler() {
			return new AsyncHttpHandler(3, executorService());
		}

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
		public HttpService httpService8080() {
			return new HttpServiceImpl(httpServiceConfig8080());
		}

		@Bean
		public HttpService httpService8081() {
			return new HttpServiceImpl(httpServiceConfig8081());
		}

		@Bean
		public HttpService httpService8082() {
			return new HttpServiceImpl(httpServiceConfig8082());
		}

		@Bean
		public HttpServiceConfig httpServiceConfig8080() {
			final HttpServiceConfig config = new HttpServiceConfig("http-service", new HttpHandler(0));
			config.setGracefulShutdownTimeoutSecs(30);
			return config;
		}

		@Bean
		public HttpServiceConfig httpServiceConfig8081() {
			final HttpServiceConfig config = new HttpServiceConfig("http-service", new HttpHandler(3), Executors.newCachedThreadPool(), 8081);
			config.setGracefulShutdownTimeoutSecs(30);
			return config;
		}

		@Bean
		public HttpServiceConfig httpServiceConfig8082() {
			final HttpServiceConfig config = new HttpServiceConfig("http-service", asyncHttpHandler(), Executors.newCachedThreadPool(), 8082);
			config.setGracefulShutdownTimeoutSecs(30);
			return config;
		}
	}

	public static class HttpHandler extends AbstractHandler implements Server.Graceful {
		final Logger log = LoggerFactory.getLogger(getClass());

		int workTime;

		private boolean shutdown;

		private final AtomicInteger requestCounter = new AtomicInteger();

		public HttpHandler(final int workTime) {
			this.workTime = workTime;
		}

		@Override
		public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
		        throws IOException, ServletException {
			if (workTime > 0) {
				try {
					log.info("sleeping for {} seconds ", workTime);
					Thread.sleep(workTime * 1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
			final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
			sb.append("target", target);
			sb.append("request.getContextPath()", request.getContextPath());
			sb.append("request.getRequestURI()", request.getRequestURI());
			sb.append("request.getQueryString()", request.getQueryString());
			final String requestInfo = sb.toString();
			log.info("request : {}", requestInfo);
			response.setContentType("text/plain;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print(requestInfo);
			baseRequest.setHandled(true);
			log.info("requestCounter = {}", requestCounter.incrementAndGet());
		}

		public boolean isShutdown() {
			return shutdown;
		}

		@Override
		public void setShutdown(final boolean shutdown) {
			this.shutdown = shutdown;
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "httpService8080")
	private HttpService httpService8080;

	@Resource(name = "httpServiceConfig8080")
	private HttpServiceConfig httpServiceConfig8080;

	@Resource(name = "httpService8081")
	private HttpService httpService8081;

	@Resource(name = "httpServiceConfig8081")
	private HttpServiceConfig httpServiceConfig8081;

	@Resource(name = "httpService8082")
	private HttpService httpService8082;

	@Resource(name = "httpServiceConfig8082")
	private HttpServiceConfig httpServiceConfig8082;

	@Autowired
	private HttpClient client;

	@Test
	public void test_httpService8080() throws Exception {
		for (int i = 0; i < 10; i++) {
			final ContentExchange exchange = new ContentExchange(true);
			exchange.setMethod("GET");
			exchange.setURL(String.format("http://localhost:%d/test_httpService8080", httpServiceConfig8080.getPort()));
			client.send(exchange);

			final int exchangeState = exchange.waitForDone();
			Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);

			log.info("exchange.getResponseContent(): {} -> {}", exchange.getStatus(), exchange.getResponseContent());
		}
	}

	@Test
	public void test_httpService8081() throws Exception {
		final ContentExchange[] exchanges = new ContentExchange[10];
		for (int i = 0; i < exchanges.length; i++) {
			final ContentExchange exchange = new ContentExchange(true);
			exchanges[i] = exchange;
			exchange.setMethod("GET");
			exchange.setURL(String.format("http://localhost:%d/test_httpService8081", httpServiceConfig8081.getPort()));
			client.send(exchange);
		}
		Thread.sleep(500);
		httpService8081.stopAndWait();
		log.info("test_httpService8081(): stopped httpService8081");
		Assert.assertEquals(((HttpHandler) httpServiceConfig8081.getHttpRequestHandler()).requestCounter.get(), exchanges.length);
		log.info("test_httpService8081(): stopped HTTPClient");
		for (final ContentExchange exchange : exchanges) {
			log.info("exchange.getResponseContent(): {} -> {}", exchange.getStatus(), exchange.getResponseContent());
		}
	}

	@Test
	public void test_httpService8082() throws Exception {
		for (int i = 0; i < 10; i++) {
			final ContentExchange exchange = new ContentExchange(true);
			exchange.setMethod("GET");
			exchange.setURL(String.format("http://localhost:%d/test_httpService8082", httpServiceConfig8082.getPort()));
			client.send(exchange);

			final int exchangeState = exchange.waitForDone();
			Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);

			log.info("exchange.getResponseContent(): {} -> {}", exchange.getStatus(), exchange.getResponseContent());
		}
	}
}
