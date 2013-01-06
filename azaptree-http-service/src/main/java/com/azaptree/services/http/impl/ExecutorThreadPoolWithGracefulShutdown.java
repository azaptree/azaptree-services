package com.azaptree.services.http.impl;

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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 
 * Enhances Jetty's ExecutorThreadPool to shutdown gracefully.
 * 
 * If there are any executor tasks still processing or in the executor queue, then the server will wait up to the specified time limit.
 * 
 * @author alfio
 * 
 */
public class ExecutorThreadPoolWithGracefulShutdown extends ExecutorThreadPool {
	private final ExecutorService executor;
	private final int shutdownTimeoutSecs;

	public ExecutorThreadPoolWithGracefulShutdown(final ExecutorService executor, final int shutdownTimeoutSecs) {
		super(executor);
		Assert.notNull(executor);
		this.executor = executor;
		this.shutdownTimeoutSecs = shutdownTimeoutSecs;
	}

	@Override
	protected void doStop() throws Exception {
		executor.shutdown();

		final Logger log = LoggerFactory.getLogger(getClass());
		final int waitTime = 5;
		int totalWaitTime = 0;
		while (!executor.awaitTermination(waitTime, TimeUnit.SECONDS)) {
			totalWaitTime += waitTime;
			if (totalWaitTime >= shutdownTimeoutSecs) {
				log.error("Executor tasks failed to shutdown within specified max wait time: {}", shutdownTimeoutSecs);
				final List<Runnable> tasks = executor.shutdownNow();
				if (!CollectionUtils.isEmpty(tasks)) {
					log.error("Number of tasks left in the executor queue that were not processed = {}", tasks.size());
					for (final Runnable task : tasks) {
						log.error("Unprocessed task: {}", task);
					}
				}
				break;
			}
			log.warn("Waiting for executor tasks to complete - Current Wait Time / Max Wait Time = {}/{}", totalWaitTime, shutdownTimeoutSecs);
		}

		super.doStop();
	}

	public int getShutdownTimeoutSecs() {
		return shutdownTimeoutSecs;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
		        .append("executor.class", executor.getClass().getName())
		        .append("shutdownTimeoutSecs", shutdownTimeoutSecs)
		        .toString();
	}
}
