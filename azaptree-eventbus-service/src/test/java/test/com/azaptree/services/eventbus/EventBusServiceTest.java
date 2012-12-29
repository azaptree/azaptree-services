/*
 * Copyright 2012 The AZAPTREE Authors
 * 
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
 */
package test.com.azaptree.services.eventbus;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.azaptree.services.eventbus.EventBusService;
import com.azaptree.services.eventbus.impl.EventBusServiceImpl;
import com.google.common.eventbus.Subscribe;

@ContextConfiguration(classes = { EventBusServiceTest.Config.class })
public class EventBusServiceTest extends AbstractTestNGSpringContextTests {
	@Configuration
	public static class Config {

		@Bean
		EventBusService asynchronousBeanNamedEventBusService() {
			return new EventBusServiceImpl(executor);
		}

		@Bean
		EventBusService asynchronousCustomBeanNamedEventBusService() {
			return new EventBusServiceImpl("CUSTOM_NAMED", executor);
		}

		@Bean
		EventBusService synchronousBeanNamedEventBusService() {
			return new EventBusServiceImpl();
		}

		@Bean
		EventBusService synchronousCustomBeanNamedEventBusService() {
			return new EventBusServiceImpl("CUSTOM_NAMED");
		}
	}

	public static class LongEventHandler {
		public AtomicInteger counter = new AtomicInteger();

		@Subscribe
		public void logEvent(final Long event) {
			log.info("EVENT LONG: {}", event);
			counter.incrementAndGet();
		}

		public void resetCounter() {
			counter.set(0);
		}
	}

	public static class StringEventHandler {

		public AtomicInteger counter = new AtomicInteger();

		@Subscribe
		public void logEvent(final String event) {
			log.info("EVENT String: {}", event);
			counter.incrementAndGet();
		}

		public void resetCounter() {
			counter.set(0);
		}
	}

	private static final Logger log = LoggerFactory.getLogger(EventBusServiceTest.class);

	private static final ExecutorService executor = Executors.newCachedThreadPool();

	@Resource(name = "synchronousBeanNamedEventBusService")
	EventBusService synchronousBeanNamedEventBusService;

	@Resource(name = "asynchronousBeanNamedEventBusService")
	EventBusService asynchronousBeanNamedEventBusService;

	@Resource(name = "synchronousCustomBeanNamedEventBusService")
	EventBusService synchronousCustomBeanNamedEventBusService;

	@Resource(name = "asynchronousCustomBeanNamedEventBusService")
	EventBusService asynchronousCustomBeanNamedEventBusService;

	@Resource
	ApplicationContext appCtx;

	public static final StringEventHandler stringEventHandler = new StringEventHandler();

	public static final LongEventHandler longEventHandler = new LongEventHandler();

	@AfterClass
	public void afterClass() {
		executor.shutdownNow();
	}

	@BeforeClass
	public void beforeClass() {
		log.info("beforeClass() - START");
		final Map<String, EventBusService> beans = appCtx.getBeansOfType(EventBusService.class);
		for (final EventBusService service : beans.values()) {
			service.register(stringEventHandler);
			service.register(longEventHandler);
			log.info("REGISTERED EVENT HANDLERS: {}", service.getEventBusName());
		}
		log.info("beforeClass() - END");
	}

	public void test(final EventBusService service) {
		stringEventHandler.resetCounter();
		longEventHandler.resetCounter();

		Assert.assertFalse(service.isAsynchronous());

		Assert.assertEquals(stringEventHandler.counter.get(), 0);

		service.post(10l);
		Assert.assertEquals(longEventHandler.counter.get(), 1);

		service.post(10l);
		Assert.assertEquals(longEventHandler.counter.get(), 2);

		service.post(10);
		Assert.assertEquals(longEventHandler.counter.get(), 2);

		Assert.assertEquals(stringEventHandler.counter.get(), 0);

		service.post("HELLO");
		Assert.assertEquals(stringEventHandler.counter.get(), 1);

		service.post("CIAO");
		Assert.assertEquals(stringEventHandler.counter.get(), 2);

		service.unregister(stringEventHandler);
		service.post("CIAO");
		Assert.assertEquals(stringEventHandler.counter.get(), 2);
		service.post(10l);
		Assert.assertEquals(stringEventHandler.counter.get(), 2);

		service.register(stringEventHandler);
		service.post("CIAO");
		Assert.assertEquals(stringEventHandler.counter.get(), 3);
		service.post(10l);
		Assert.assertEquals(stringEventHandler.counter.get(), 3);
	}

	@Test
	public void test_asynchronousBeanNamedEventBusService() throws InterruptedException {
		Assert.assertEquals(asynchronousBeanNamedEventBusService.getEventBusName(), "asynchronousBeanNamedEventBusService");

		longEventHandler.resetCounter();
		for (int i = 0; i < 10; i++) {
			asynchronousBeanNamedEventBusService.post(Long.valueOf(i));
		}

		Assert.assertTrue(asynchronousBeanNamedEventBusService.isAsynchronous());
		Thread.sleep(100l);
		Assert.assertEquals(longEventHandler.counter.get(), 10);
	}

	@Test
	public void test_asynchronousCustomBeanNamedEventBusService() throws InterruptedException {
		Assert.assertEquals(asynchronousCustomBeanNamedEventBusService.getEventBusName(), "CUSTOM_NAMED");

		longEventHandler.resetCounter();
		for (int i = 0; i < 10; i++) {
			asynchronousCustomBeanNamedEventBusService.post(Long.valueOf(i));
		}

		Assert.assertTrue(asynchronousCustomBeanNamedEventBusService.isAsynchronous());
		Thread.sleep(100l);
		Assert.assertEquals(longEventHandler.counter.get(), 10);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void test_registerEventHandlerWithNohandlerMethods() {
		asynchronousCustomBeanNamedEventBusService.register(new Object());
	}

	@Test
	public void test_synchronousBeanNamedEventBusService() {
		Assert.assertEquals(synchronousBeanNamedEventBusService.getEventBusName(), "synchronousBeanNamedEventBusService");
		test(synchronousBeanNamedEventBusService);
	}

	@Test
	public void test_synchronousCustomBeanNamedEventBusService() {
		Assert.assertEquals(synchronousCustomBeanNamedEventBusService.getEventBusName(), "CUSTOM_NAMED");
		test(synchronousCustomBeanNamedEventBusService);
	}

}
