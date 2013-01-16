package test.com.azaptree.services.spring.application;

/*
 * #%L
 * AZAPTREE-SPRING-APPLICATION-SERVICE
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.spring.application.SpringApplicationService;

public class SpringApplicationServiceTest {
	private final Logger log = LoggerFactory.getLogger(getClass());

	public SpringApplicationServiceTest() {
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testInvalidCommandLine() throws Exception {
		SpringApplicationService.main(new String[0]);
	}

	@Test
	public void testSpringApplicationService() throws Exception {
		final String[] args = { "classpath:spring-application-service.xml" };

		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						SpringApplicationService.main(args);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			Thread.yield();

			ApplicationContext ctx = SpringApplicationService.getApplicationContext();
			while (ctx == null) {
				log.info("Waiting for ApplicationContext to initialize ...");
				Thread.sleep(100l);
				ctx = SpringApplicationService.getApplicationContext();
			}
			Assert.assertNotNull(ctx);
			final Properties props = ctx.getBean("systemProps", Properties.class);
			Assert.assertNotNull(props);
			Assert.assertFalse(CollectionUtils.isEmpty(props));

			Assert.assertEquals(props.getProperty("app.env"), "DEV");

			final Map<String, String> env = ctx.getBean("env", Map.class);
			Assert.assertNotNull(env);
			Assert.assertFalse(CollectionUtils.isEmpty(env));

			log.info("bean count: {}", ctx.getBeanDefinitionCount());
			for (String beanName : ctx.getBeanDefinitionNames()) {
				log.info("beanName: {}", beanName);
			}
		} finally {
			SpringApplicationService.shutdown();
		}
	}

	@Test
	public void testSpringApplicationService_configClasses() throws Exception {
		final String[] args = { "classpath:spring-application-service.xml" };

		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						SpringApplicationService.main(args);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			Thread.yield();

			ApplicationContext ctx = SpringApplicationService.getApplicationContext();
			while (ctx == null) {
				log.info("Waiting for ApplicationContext to initialize ...");
				Thread.sleep(100l);
				ctx = SpringApplicationService.getApplicationContext();
			}
			Assert.assertNotNull(ctx);
			final Properties props = ctx.getBean("systemProps", Properties.class);
			Assert.assertNotNull(props);
			Assert.assertFalse(CollectionUtils.isEmpty(props));

			final Map<String, String> env = ctx.getBean("env", Map.class);
			Assert.assertNotNull(env);
			Assert.assertFalse(CollectionUtils.isEmpty(env));

			log.info("bean count: {}", ctx.getBeanDefinitionCount());
			for (String beanName : ctx.getBeanDefinitionNames()) {
				log.info("beanName: {}", beanName);
			}
		} finally {
			SpringApplicationService.shutdown();
		}
	}

	@Test
	public void testGenerateXSD() throws JAXBException, IOException {
		final JAXBContext jc = JAXBContext.newInstance(com.azaptree.services.spring.application.config.SpringApplicationService.class);
		jc.generateSchema(new SchemaOutputResolver() {

			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				File file = new File("target", suggestedFileName);
				StreamResult result = new StreamResult(file);
				result.setSystemId(file.toURI().toURL().toString());
				return result;
			}
		});

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		jc.generateSchema(new SchemaOutputResolver() {

			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				StreamResult result = new StreamResult(bos);
				result.setSystemId("");
				return result;
			}
		});

		System.out.println(bos.toString());
	}

}
