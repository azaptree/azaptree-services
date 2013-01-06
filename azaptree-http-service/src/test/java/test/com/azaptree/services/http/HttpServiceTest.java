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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.server.Request;
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
import com.azaptree.services.http.impl.HttpServiceImpl;

@ContextConfiguration(classes = { HttpServiceTest.Config.class })
public class HttpServiceTest extends AbstractTestNGSpringContextTests {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Configuration
	public static class Config {

		public HttpServiceConfig httpServiceConfig() {
			return new HttpServiceConfig("http-service", new AbstractHandler() {
				final Logger log = LoggerFactory.getLogger(getClass());

				@Override
				public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException,
				        ServletException {
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
				}
			});
		}

		@Bean
		public HttpService httpService() {
			return new HttpServiceImpl(httpServiceConfig());
		}

	}

	@Autowired
	private HttpService httpService;

	@Test
	public void test_httpService() throws Exception {
		HttpClient client = new HttpClient();
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		client.start();
		try {
			httpService.startAndWait();

			for (int i = 0; i < 10; i++) {
				ContentExchange exchange = new ContentExchange(true);
				exchange.setMethod("GET");
				exchange.setURL(String.format("http://localhost:%d/user", httpService.getHttpServiceConfig().getPort()));
				client.send(exchange);

				final int exchangeState = exchange.waitForDone();
				Assert.assertEquals(exchangeState, HttpExchange.STATUS_COMPLETED);

				log.info("exchange.getResponseContent(): {}", exchange.getResponseContent());
			}

		} finally {
			client.stop();
			client.destroy();
			httpService.stopAndWait();
		}
	}
}
