package test.com.azaptree.services.http.impl;

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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.http.impl.ExecutorThreadPoolWithGracefulShutdown;

public class ExecutorThreadPoolWithGracefulShutdownTest {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void test_start_stop() throws Exception {
		ExecutorService executor = Executors.newCachedThreadPool();
		ExecutorThreadPoolWithGracefulShutdown threadPool = new ExecutorThreadPoolWithGracefulShutdown(executor, 1);
		Assert.assertEquals(threadPool.getShutdownTimeoutSecs(), 1);
		threadPool.start();

		final int threadCount = 10;
		final AtomicInteger counter = new AtomicInteger();
		final CountDownLatch countdownLatch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			threadPool.dispatch(new Runnable() {

				@Override
				public void run() {
					try {
						log.info("SLEEPING ...");
						Thread.sleep(3000l);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					log.info("counter : {}", counter.incrementAndGet());
					countdownLatch.countDown();
				}
			});
		}
		threadPool.stop();
		log.info("STOPPED THREADPOOL");

		countdownLatch.await();

		Assert.assertEquals(counter.get(), threadCount);
	}
}
