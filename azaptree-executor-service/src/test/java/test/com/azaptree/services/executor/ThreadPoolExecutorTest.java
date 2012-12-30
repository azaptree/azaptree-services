package test.com.azaptree.services.executor;

import java.util.concurrent.LinkedBlockingQueue;
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
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Configuration
	public static class Config {

		@Bean
		public ThreadPoolExecutor defaultExecutor() {
			return new ThreadPoolExecutor(defaultExecutorConfig());
		}

		@Bean
		public ThreadPoolExecutor daemonExecutor() {
			return new ThreadPoolExecutor(daemonExecutorConfig());
		}

		@Bean
		public ThreadPoolExecutor pauseableExecutor() {
			return new ThreadPoolExecutor(pauseableExecutorConfig());
		}

		@Bean
		public ThreadPoolConfig defaultExecutorConfig() {
			return new ThreadPoolConfig();
		}

		@Bean
		public ThreadPoolConfig daemonExecutorConfig() {
			final ThreadPoolConfig config = new ThreadPoolConfig();
			config.setDaemon(true);
			return config;
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

		@Override
		public void run() {
			log.info("counter = {}", counter.incrementAndGet());
		}

		static void resetCounter() {
			counter.set(0);
		}

	}

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

	@Test
	public void test_defaultExecutor() throws InterruptedException {
		testExecutor(defaultExecutor, "defaultExecutor", defaultExecutorConfig);
	}

	@Test
	public void test_daemonExecutor() throws InterruptedException {
		testExecutor(daemonExecutor, "daemonExecutor", daemonExecutorConfig);
	}

	public void testExecutor(ThreadPoolExecutor executor, String name, ThreadPoolConfig config) throws InterruptedException {
		Task.resetCounter();
		Assert.assertEquals(name, executor.getName());
		Assert.assertEquals(config.getCorePoolSize(), executor.getCorePoolSize());
		Assert.assertEquals(config.getKeepAliveTime(), executor.getKeepAliveTime(config.getKeepAliveTimeUnit()));

		for (int i = 0; i < 100; i++) {
			executor.execute(new Task());
		}

		Thread.sleep(100l);
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
		for (int i = 0; i < 100; i++) {
			pauseableExecutor.execute(new Task());
		}
		Assert.assertEquals(pauseableExecutor.getCompletedTaskCount(), 0);
		log.info("testPausableExecutor(): paused: {}", pauseableExecutor);
		pauseableExecutor.resume();

		Thread.sleep(100l);
		Assert.assertEquals(pauseableExecutor.getCompletedTaskCount(), 100);
		Assert.assertEquals(Task.counter.get(), 100);
	}

}
