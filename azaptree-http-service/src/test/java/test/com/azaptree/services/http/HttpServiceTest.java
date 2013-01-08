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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.azaptree.services.http.HttpService;
import com.azaptree.services.http.HttpServiceConfig;
import com.azaptree.services.http.HttpServiceJmxApi;
import com.azaptree.services.http.handler.AsyncSuspendContinueHttpHandlerSupport;
import com.azaptree.services.http.impl.ExecutorThreadPoolWithGracefulShutdown;
import com.azaptree.services.http.impl.HttpServiceImpl;

@ContextConfiguration(classes = { HttpServiceTest.Config.class })
public class HttpServiceTest extends AbstractTestNGSpringContextTests {

	public static class AsyncHttpHandler extends AsyncSuspendContinueHttpHandlerSupport {
		final int workTime;

		private final AtomicInteger requestCounter = new AtomicInteger();

		public AsyncHttpHandler(final int workTime, final Executor executor, final long timeout) {
			super(executor, timeout);
			this.workTime = workTime;
		}

		@Override
		@PreDestroy
		public void destroy() {
			log.info("PRE-DESTROY");
		}

		@Override
		protected void process(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {

			final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
			sb.append("target", target);
			sb.append("request.getContextPath()", request.getContextPath());
			sb.append("request.getRequestURI()", request.getRequestURI());
			sb.append("request.getQueryString()", request.getQueryString());
			final String requestInfo = sb.toString();

			if (workTime > 0) {
				try {
					log.info("sleeping for {} seconds ", workTime);
					Thread.sleep(workTime * 1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}

			try {

				log.info("request : {}", requestInfo);
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().print(requestInfo);
				log.info("requestCounter = {}", requestCounter.incrementAndGet());
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class AsyncHttpHandlerThrowsException extends AsyncSuspendContinueHttpHandlerSupport {
		private final AtomicInteger requestCounter = new AtomicInteger();

		public AsyncHttpHandlerThrowsException(final Executor executor) {
			super(executor);
		}

		@Override
		protected void process(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
			log.info("requestCounter = {}", requestCounter.incrementAndGet());
			throw new RuntimeException(target);
		}
	}

	@Configuration
	public static class Config {

		@Bean
		public Handler asyncHttpHandler() {
			return new AsyncHttpHandler(2, executorService(), 10 * 1000);
		}

		@Bean
		public Handler asyncHttpHandlerThrowsException() {
			return new AsyncHttpHandlerThrowsException(executorService());
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
		public HttpService httpService8083() {
			return new HttpServiceImpl(httpServiceConfig8083());
		}

		@Bean
		public HttpServiceConfig httpServiceConfig8080() {
			final HttpServiceConfig config = new HttpServiceConfig("http-service", new HttpHandler(0));
			config.setGracefulShutdownTimeoutSecs(30);
			config.setRequestBufferSize(8096);
			config.setRequestHeaderBufferSize(8096);
			config.setResponseBufferSize(8096);
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
			config.setGracefulShutdownTimeoutSecs(1);
			return config;
		}

		@Bean
		public HttpServiceConfig httpServiceConfig8083() {
			final HttpServiceConfig config = new HttpServiceConfig("http-service", asyncHttpHandlerThrowsException(), Executors.newCachedThreadPool(), 8083);
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

	@Resource
	private ApplicationContext ctx;

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

	@Resource(name = "httpService8083")
	private HttpService httpService8083;

	@Resource(name = "httpServiceConfig8083")
	private HttpServiceConfig httpServiceConfig8083;

	@Autowired
	private HttpClient client;

	@BeforeClass
	public void beforeClass() {
		for (final String beanName : ctx.getBeanDefinitionNames()) {
			log.info("spring managed bean : {} -> {}", beanName, ctx.getBean(beanName).getClass().getName());
		}
	}

	@Test
	public void test_httpService8080() throws Exception {
		for (int i = 0; i < 10; i++) {
			final ContentExchange exchange = new ContentExchange(true);
			exchange.setMethod("GET");
			exchange.setURL(String.format("http://localhost:%d/test_httpService8080", httpServiceConfig8080.getPort()));
			client.send(exchange);

			final int exchangeState = exchange.waitForDone();
			Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);

			log.info("test_httpService8080() : exchange.getResponseContent(): {} -> {}", exchange.getStatus(), exchange.getResponseContent());
		}
	}

	@Test
	public void test_httpService8081_asyncHttpClient_stopHttpServer() throws Exception {
		final ContentExchange[] exchanges = new ContentExchange[10];
		for (int i = 0; i < exchanges.length; i++) {
			final ContentExchange exchange = new ContentExchange(true);
			exchanges[i] = exchange;
			exchange.setMethod("GET");
			exchange.setURL(String.format("http://localhost:%d/test_httpService8081", httpServiceConfig8081.getPort()));
			client.send(exchange);
		}
		Thread.sleep(200);
		httpService8081.stopAndWait();
		log.info("test_httpService8081(): stopped httpService8081");

		// check that all server requests were processed
		Assert.assertEquals(((HttpHandler) httpServiceConfig8081.getHttpRequestHandler()).requestCounter.get(), exchanges.length);
		for (final ContentExchange exchange : exchanges) {
			log.info("test_httpService8081() : exchange.getResponseContent(): {} -> {}", exchange.getStatus(), exchange.getResponseContent());
		}
	}

	@Test
	public void test_httpService8082_asyncServerHandler() throws Exception {
		final List<ContentExchange> exchanges = new ArrayList<>();
		for (int requestCount = 0; requestCount < 3; requestCount++) {
			final ContentExchange exchange = new ContentExchange(true);
			exchanges.add(exchange);
			exchange.setMethod("GET");
			exchange.setURL(String.format("http://localhost:%d/test_httpService8082", httpServiceConfig8082.getPort()));
			client.send(exchange);

			final int exchangeState = exchange.waitForDone();
			Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);

			// check that all server requests were processed
			log.info("test_httpService8082_asyncServerHandler(): exchange.getResponseContent(): {} -> {}", exchange.getStatus(), exchange.getResponseContent());
		}

		for (int requestCount = 0; requestCount < 0; requestCount++) {
			final ContentExchange exchange = new ContentExchange(true);
			exchanges.add(exchange);
			exchange.setMethod("GET");
			exchange.setURL(String.format("http://localhost:%d/test_httpService8082", httpServiceConfig8082.getPort()));
			client.send(exchange);
		}
		Thread.sleep(100);
		httpService8082.stopAndWait();
		((HttpServiceImpl) httpService8082).destroy();

		// check that all server requests were processed
		Assert.assertEquals(((AsyncHttpHandler) httpServiceConfig8082.getHttpRequestHandler()).requestCounter.get(), exchanges.size());
		for (final ContentExchange exchange : exchanges) {
			log.info("test_httpService8082_asyncServerHandler(): after server has been stopped : exchange.getResponseContent(): {} -> {}",
			        exchange.getStatus(), exchange.getResponseContent());
		}
	}

	@Test
	public void test_httpService8083_asyncServerHandlerThrowsException() throws Exception {
		for (int i = 0; i < 3; i++) {
			final ContentExchange exchange = new ContentExchange(true);
			exchange.setMethod("GET");
			exchange.setURL(String.format("http://localhost:%d/test_httpService8083", httpServiceConfig8083.getPort()));
			client.send(exchange);

			final int exchangeState = exchange.waitForDone();
			Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);

			log.info("test_httpService8083_asyncServerHandlerThrowsException(): exchange.getResponseContent(): {} -> {}", exchange.getStatus(),
			        exchange.getResponseContent());
		}
	}

	@Test
	public void testJmxApi() {
		final HttpServiceJmxApi jmxApi = (HttpServiceJmxApi) httpService8080;
		Assert.assertEquals(jmxApi.getPort(), 8080);
		Assert.assertTrue(StringUtils.isNotBlank(jmxApi.getName()));
		Assert.assertTrue(StringUtils.isNotBlank(jmxApi.getVersion()));
		Assert.assertTrue(StringUtils.isNotBlank(jmxApi.getState()));
		log.info("name : {}", jmxApi.getName());
		log.info("version : {}", jmxApi.getVersion());
		log.info("state : {}", jmxApi.getState());
	}
}
