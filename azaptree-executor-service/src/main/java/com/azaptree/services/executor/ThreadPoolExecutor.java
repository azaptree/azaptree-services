package com.azaptree.services.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class ThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor implements PausableThreadPoolExecutor, BeanNameAware {
	private String name;

	private volatile boolean paused;

	private final ReentrantLock pauseLock = new ReentrantLock();
	private final Condition unpaused = pauseLock.newCondition();

	public ThreadPoolExecutor() {
		super(10, 50,
		        1, TimeUnit.MINUTES,
		        new SynchronousQueue<Runnable>(),
		        new CallerRunsPolicy());
		logInfo();
	}

	public ThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
	        final BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		logInfo();
	}

	public ThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
	        final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
		logInfo();
	}

	public ThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
	        final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		logInfo();
	}

	public ThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
	        final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
		logInfo();
	}

	public ThreadPoolExecutor(final String name) {
		super(10, 50,
		        1, TimeUnit.MINUTES,
		        new SynchronousQueue<Runnable>(),
		        new ThreadFactory() {
			        private final AtomicInteger threadCounter = new AtomicInteger(0);

			        @Override
			        public Thread newThread(final Runnable r) {
				        return new Thread(r, String.format("%s-%d", name, threadCounter.incrementAndGet()));
			        }
		        },
		        new CallerRunsPolicy());
		logInfo();
	}

	public ThreadPoolExecutor(final ThreadPoolConfig config) {
		super(config.getCorePoolSize(), config.getMaximumPoolSize(),
		        config.getKeepAliveTime(), config.getKeepAliveTimeUnit(),
		        config.getWorkQueue(), config.getThreadFactory(), config.getHandler());
		allowCoreThreadTimeOut(config.isAllowCoreThreadTimeOut());
		logInfo();
	}

	@ManagedOperation(
	        description = "Sets the policy governing whether core threads may time out and terminate if no tasks arrive within the keep-alive time, being replaced if needed when new tasks arrive.")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "value", description = "true if should time out, else false") })
	@Override
	public void allowCoreThreadTimeOut(final boolean value) {
		super.allowCoreThreadTimeOut(value);
	}

	@Override
	protected void beforeExecute(final Thread t, final Runnable r) {
		super.beforeExecute(t, r);
		while (paused) {
			pauseLock.lock();
			try {
				while (paused) {
					unpaused.await();
				}
			} catch (final InterruptedException ie) {
				t.interrupt();
			} finally {
				pauseLock.unlock();
			}
		}
	}

	@PreDestroy
	public void destroy() {
		final Logger log = LoggerFactory.getLogger(ThreadPoolExecutor.class);
		log.info("SHUTTING DOWN : {}", name);
		shutdown();
		try {
			while (!awaitTermination(60l, TimeUnit.SECONDS)) {
				log.info("WAITING FOR SHUTDOWN TO COMPLETE : {}", name);
			}
		} catch (final InterruptedException e) {
			log.info("Interrupted while waiting for shutdown to complete : {}", name);
		}
	}

	@ManagedAttribute(description = "Returns the approximate number of threads that are actively executing tasks.")
	@Override
	public int getActiveCount() {
		return super.getActiveCount();
	}

	@Override
	@ManagedAttribute(description = "Returns the approximate total number of tasks that have completed execution.")
	public long getCompletedTaskCount() {
		return super.getCompletedTaskCount();
	}

	@ManagedAttribute(description = "Returns the core number of threads.")
	@Override
	public int getCorePoolSize() {
		return super.getCorePoolSize();
	}

	@Override
	@ManagedAttribute(description = "Returns the largest number of threads that have ever simultaneously been in the pool.")
	public int getLargestPoolSize() {
		return super.getLargestPoolSize();
	}

	@Override
	@ManagedAttribute(description = "Returns the maximum allowed number of threads.")
	public int getMaximumPoolSize() {
		return super.getMaximumPoolSize();
	}

	@ManagedAttribute
	public String getName() {
		return name;
	}

	@Override
	@ManagedAttribute(description = "Returns the current number of threads in the pool.")
	public int getPoolSize() {
		return super.getPoolSize();
	}

	@Override
	@ManagedAttribute(description = "Returns the approximate total number of tasks that have ever been scheduled for execution.")
	public long getTaskCount() {
		return super.getTaskCount();
	}

	@ManagedAttribute(
	        description = "Returns true if this pool allows core threads to time out and terminate if no tasks arrive within the keepAlive time, being replaced if needed when new tasks arrive.")
	public boolean isCoreThreadTimeOutAllowed() {
		return super.allowsCoreThreadTimeOut();
	}

	@Override
	@ManagedAttribute
	public boolean isPaused() {
		return paused;
	}

	public void logInfo() {
		LoggerFactory.getLogger(ThreadPoolExecutor.class).info("{}", this);
	}

	@Override
	@ManagedOperation
	public void pause() {
		paused = true;
	}

	@ManagedOperation(description = "Tries to remove from the work queue all Future tasks that have been cancelled.")
	@Override
	public void purge() {
		super.purge();
	}

	@Override
	@ManagedOperation
	public void resume() {
		paused = false;
		pauseLock.lock();
		try {
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	@Override
	public void setBeanName(final String beanName) {
		name = beanName;
	}

	@ManagedOperation(
	        description = "Sets the core number of threads. If the new value is smaller than the current value, excess existing threads will be terminated when they next become idle. If larger, new threads will, if needed, be started to execute any queued tasks")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "corePoolSize", description = "the new core size") })
	@Override
	public void setCorePoolSize(final int corePoolSize) {
		super.setCorePoolSize(corePoolSize);
	}

	@ManagedOperation(description = "Sets the time limit in seconds for which threads may remain idle before being terminated.")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "time",
	        description = "the time in seconds to wait. A time value of zero will cause excess threads to terminate immediately after executing tasks.") })
	public void setKeepAliveTimeSecs(final int time) {
		super.setKeepAliveTime(time, TimeUnit.SECONDS);
	}

	@ManagedOperation(description = "Sets the maximum allowed number of threads.")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "maximumPoolSize", description = "the new maximum") })
	@Override
	public void setMaximumPoolSize(final int maximumPoolSize) {
		super.setMaximumPoolSize(maximumPoolSize);
	}

	@Override
	public String toString() {
		final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
		sb.append("activeCount", getActiveCount());
		sb.append("corePoolSize", getCorePoolSize());
		sb.append("largestPoolSize", getLargestPoolSize());
		sb.append("maximumPoolSize", getMaximumPoolSize());
		sb.append("poolSize", getPoolSize());
		sb.append("completedTaskCount", getCompletedTaskCount());
		sb.append("keepAliveTimeSecs", getKeepAliveTime(TimeUnit.SECONDS));
		sb.append("name", getName());
		sb.append("taskCount", getTaskCount());
		sb.append("allowsCoreThreadTimeOut", allowsCoreThreadTimeOut());
		return sb.toString();
	}

}
