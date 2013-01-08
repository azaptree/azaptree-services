package test.com.azaptree.services.executor;

/*
 * #%L
 * AZAPTREE-EXECUTOR-SERVICE
 * %%
 * Copyright (C) 2012 AZAPTREE.COM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.executor.ThreadPoolConfig;
import com.azaptree.services.executor.ThreadPoolExecutor;

@ContextConfiguration(classes = { ThreadPoolExecutorTest.Config.class })
public class ThreadPoolExecutorTest extends AbstractTestNGSpringContextTests {
	@Configuration
	public static class Config {

		@Bean
		public ThreadPoolExecutor executor() {
			return new ThreadPoolExecutor();
		}

		@Bean
		public ThreadPoolExecutor executor2() {
			return new ThreadPoolExecutor("executor2");
		}

		@Bean
		public ThreadPoolExecutor azapExecutor() {
			return new ThreadPoolExecutor(azapExecutorConfig());
		}

		@Bean
		public ThreadPoolConfig azapExecutorConfig() {
			return new ThreadPoolConfig("azap", true);
		}

		@Bean
		public ThreadPoolExecutor daemonExecutor() {
			return new ThreadPoolExecutor(daemonExecutorConfig());
		}

		@Bean
		public ThreadPoolConfig daemonExecutorConfig() {
			final ThreadPoolConfig config = new ThreadPoolConfig();
			config.setDaemon(true);
			return config;
		}

		@Bean
		public ThreadPoolExecutor defaultExecutor() {
			return new ThreadPoolExecutor(defaultExecutorConfig());
		}

		@Bean
		public ThreadPoolConfig defaultExecutorConfig() {
			return new ThreadPoolConfig();
		}

		@Bean
		public ThreadPoolExecutor pauseableExecutor() {
			return new ThreadPoolExecutor(pauseableExecutorConfig());
		}

		@Bean
		public ThreadPoolConfig pauseableExecutorConfig() {
			final ThreadPoolConfig config = new ThreadPoolConfig();
			config.setDaemon(true);
			config.setWorkQueue(new LinkedBlockingQueue<Runnable>());
			return config;
		}
	}

	public static class Task implements Runnable {
		final Logger log = LoggerFactory.getLogger(getClass());

		final static AtomicInteger counter = new AtomicInteger();

		static void resetCounter() {
			counter.set(0);
		}

		String threadName;

		boolean daemon;

		@Override
		public void run() {
			log.info("counter = {}", counter.incrementAndGet());
			threadName = Thread.currentThread().getName();
			daemon = Thread.currentThread().isDaemon();
		}

	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource(name = "executor")
	private ThreadPoolExecutor executor;

	@Resource(name = "executor2")
	private ThreadPoolExecutor executor2;

	@Resource(name = "defaultExecutor")
	private ThreadPoolExecutor defaultExecutor;

	@Resource(name = "defaultExecutorConfig")
	private ThreadPoolConfig defaultExecutorConfig;

	@Resource(name = "daemonExecutor")
	private ThreadPoolExecutor daemonExecutor;

	@Resource(name = "daemonExecutorConfig")
	private ThreadPoolConfig daemonExecutorConfig;

	@Resource(name = "pauseableExecutor")
	private ThreadPoolExecutor pauseableExecutor;

	@Resource(name = "pauseableExecutorConfig")
	private ThreadPoolConfig pauseableExecutorConfig;

	@Resource(name = "azapExecutor")
	private ThreadPoolExecutor azapExecutor;

	@Resource(name = "azapExecutorConfig")
	private ThreadPoolConfig azapExecutorConfig;

	@Test
	public void test_azapDaemonExecutor() throws InterruptedException {
		final Task t = new Task();
		Task.resetCounter();
		azapExecutor.execute(t);
		Thread.sleep(10l);

		Assert.assertEquals(azapExecutor.getCompletedTaskCount(), 1);
		Assert.assertEquals(Task.counter.get(), 1);
		Assert.assertEquals(azapExecutorConfig.getName(), "azap");
		Assert.assertEquals(azapExecutorConfig.isDaemon(), true);
		Assert.assertEquals(t.daemon, true);
		Assert.assertTrue(t.threadName.startsWith(azapExecutorConfig.getName()));
	}

	@Test
	public void testPurge() {
		pauseableExecutor.setCorePoolSize(1);
		pauseableExecutor.setMaximumPoolSize(1);
		pauseableExecutor.pause();
		for (int i = 0; i < 100; i++) {
			final FutureTask<Boolean> futureTask = new FutureTask<>(new Task(), Boolean.TRUE);
			pauseableExecutor.execute(futureTask);
			futureTask.cancel(true);
		}
		Assert.assertEquals(pauseableExecutor.getQueue().size(), 99);
		pauseableExecutor.purge();
		Assert.assertEquals(pauseableExecutor.getQueue().size(), 0);
		pauseableExecutor.resume();
	}

	@Test
	public void test_executor2() throws InterruptedException {
		final Task t = new Task();
		Task.resetCounter();
		executor2.execute(t);
		Thread.sleep(10l);

		Assert.assertEquals(executor2.getCompletedTaskCount(), 1);
		Assert.assertEquals(Task.counter.get(), 1);
		Assert.assertTrue(t.threadName.startsWith(executor2.getName()));

		executor2.setCorePoolSize(10);
		Assert.assertEquals(executor2.getCorePoolSize(), 10);

		executor2.setMaximumPoolSize(100);
		Assert.assertEquals(executor2.getMaximumPoolSize(), 100);

		log.info("test_executor2() : executor2.setMaximumPoolSize(1) : {}", executor2);

		executor2.setKeepAliveTime(1, TimeUnit.MILLISECONDS);
		executor2.allowCoreThreadTimeOut(true);
		Assert.assertTrue(executor2.isCoreThreadTimeOutAllowed());

		Thread.sleep(10l);
		Assert.assertEquals(executor2.getPoolSize(), 0);

		executor2.setKeepAliveTimeSecs(1);
		executor2.execute(t);
		Assert.assertEquals(executor2.getPoolSize(), 1);
		Thread.sleep(1100l);
		Assert.assertEquals(executor2.getPoolSize(), 0);
	}

	@Test
	public void test_daemonExecutor() throws InterruptedException {
		testExecutor(daemonExecutor, "daemonExecutor", daemonExecutorConfig);
	}

	@Test
	public void test_defaultExecutor() throws InterruptedException {
		testExecutor(defaultExecutor, "defaultExecutor", defaultExecutorConfig);
	}

	public void testExecutor(final ThreadPoolExecutor executor, final String name, final ThreadPoolConfig config) throws InterruptedException {
		Task.resetCounter();
		Assert.assertEquals(name, executor.getName());
		Assert.assertEquals(config.getCorePoolSize(), executor.getCorePoolSize());
		Assert.assertEquals(config.getKeepAliveTime(), executor.getKeepAliveTime(config.getKeepAliveTimeUnit()));

		for (int i = 0; i < 100; i++) {
			executor.execute(new Task());
		}

		for (int i = 0; i < 5; i++) {
			Thread.sleep(100l);
			if (executor.getCompletedTaskCount() == 100) {
				break;
			}
		}
		Assert.assertEquals(executor.getCompletedTaskCount(), 100);
		Assert.assertEquals(Task.counter.get(), 100);
	}

	@Test
	public void testPausableExecutor() throws InterruptedException {
		Task.resetCounter();
		Assert.assertEquals("pauseableExecutor", pauseableExecutor.getName());
		Assert.assertEquals(pauseableExecutorConfig.getCorePoolSize(), pauseableExecutor.getCorePoolSize());
		Assert.assertEquals(pauseableExecutorConfig.getKeepAliveTime(), pauseableExecutor.getKeepAliveTime(pauseableExecutorConfig.getKeepAliveTimeUnit()));

		pauseableExecutor.pause();
		Assert.assertTrue(pauseableExecutor.isPaused());
		for (int i = 0; i < 100; i++) {
			pauseableExecutor.execute(new Task());
		}
		Assert.assertEquals(pauseableExecutor.getCompletedTaskCount(), 0);
		log.info("testPausableExecutor(): paused: {}", pauseableExecutor);
		pauseableExecutor.resume();
		Assert.assertFalse(pauseableExecutor.isPaused());

		Thread.sleep(100l);
		Assert.assertEquals(pauseableExecutor.getCompletedTaskCount(), 100);
		Assert.assertEquals(Task.counter.get(), 100);
	}

	@Test
	public void testThreadPoolConfigEquals() {
		Assert.assertEquals(new ThreadPoolConfig("azap", true), new ThreadPoolConfig("azap", true));
		Assert.assertEquals(new ThreadPoolConfig("azap"), new ThreadPoolConfig("azap"));
		Assert.assertEquals(new ThreadPoolConfig(), new ThreadPoolConfig());
		Assert.assertNotEquals(new ThreadPoolConfig(), new ThreadPoolConfig("azap"));

		final ThreadPoolConfig[] configs = new ThreadPoolConfig[2];
		for (int i = 0; i < 2; i++) {
			configs[i] = new ThreadPoolConfig("azap", 10, 50, true);
			configs[i].setAllowCoreThreadTimeOut(true);
			configs[i].setWorkQueue(new LinkedBlockingQueue<Runnable>());
		}

		Assert.assertEquals(configs[0], configs[1]);
	}

	@Test
	public void testThreadPoolConfigHashCode() {
		Assert.assertEquals(new ThreadPoolConfig("azap", true).hashCode(), new ThreadPoolConfig("azap", true).hashCode());
		Assert.assertEquals(new ThreadPoolConfig("azap").hashCode(), new ThreadPoolConfig("azap").hashCode());
		Assert.assertEquals(new ThreadPoolConfig().hashCode(), new ThreadPoolConfig().hashCode());
		Assert.assertNotEquals(new ThreadPoolConfig().hashCode(), new ThreadPoolConfig("azap").hashCode());

		final ThreadPoolConfig[] configs = new ThreadPoolConfig[2];
		for (int i = 0; i < 2; i++) {
			configs[i] = new ThreadPoolConfig("azap", 10, 50, true);
			configs[i].setAllowCoreThreadTimeOut(true);
			configs[i].setWorkQueue(new LinkedBlockingQueue<Runnable>());
		}

		Assert.assertEquals(configs[0].hashCode(), configs[1].hashCode());
	}

}
