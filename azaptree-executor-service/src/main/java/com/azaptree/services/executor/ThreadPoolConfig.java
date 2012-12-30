package com.azaptree.services.executor;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.LoggerFactory;

public class ThreadPoolConfig {
	private String name;

	private int corePoolSize = 3;

	private int maximumPoolSize = 20;

	private long keepAliveTime = 1;
	private TimeUnit keepAliveTimeUnit = TimeUnit.MINUTES;

	private BlockingQueue<Runnable> workQueue = new SynchronousQueue<>();
	private RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

	private boolean daemon = false;

	private boolean allowCoreThreadTimeOut = false;

	/**
	 * DEFAULT CONFIG:
	 * 
	 * <code>
	 * int corePoolSize = 3;
	 * 
	 * int maximumPoolSize = 20;
	 * 
	 * long keepAliveTime = 1;
	 * TimeUnit keepAliveTimeUnit = TimeUnit.MINUTES;
	 * 
	 * BlockingQueue<Runnable> workQueue = new SynchronousQueue<>();
	 * RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
	 * 
	 * boolean daemon = false;
	 * 
	 * boolean allowCoreThreadTimeOut = false;
	 * </code>
	 * 
	 */
	public ThreadPoolConfig() {
	}

	public ThreadPoolConfig(final String name) {
		setName(name);
	}

	public ThreadPoolConfig(final String name, final boolean daemon) {
		this(name);
		setDaemon(daemon);
	}

	public ThreadPoolConfig(final String name, final int corePoolSize, final int maximumPoolSize) {
		this(name);
		setCorePoolSize(corePoolSize);
		setMaximumPoolSize(maximumPoolSize);
	}

	public ThreadPoolConfig(final String name, final int corePoolSize, final int maximumPoolSize, final boolean daemon) {
		this(name, corePoolSize, maximumPoolSize);
		setDaemon(daemon);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ThreadPoolConfig other = (ThreadPoolConfig) obj;
		return Objects.equals(getName(), other.getName())
		        && Objects.equals(getCorePoolSize(), other.getCorePoolSize())
		        && Objects.equals(getMaximumPoolSize(), other.getMaximumPoolSize())
		        && Objects.equals(getKeepAliveTime(), other.getKeepAliveTime())
		        && Objects.equals(getKeepAliveTimeUnit(), other.getKeepAliveTimeUnit())
		        && Objects.equals(isDaemon(), other.isDaemon())
		        && Objects.equals(isAllowCoreThreadTimeOut(), other.isAllowCoreThreadTimeOut())
		        && Objects.equals(getHandler() != null ? getHandler().getClass().getName() : null, other.getHandler() != null ? other.getHandler().getClass()
		                .getName() : null)
		        && Objects.equals(getWorkQueue() != null ? getWorkQueue().getClass().getName() : null, other.getWorkQueue() != null ? other.getWorkQueue()
		                .getClass().getName() : null);
	}

	@Min(1)
	public int getCorePoolSize() {
		return corePoolSize;
	}

	public RejectedExecutionHandler getHandler() {
		return handler;
	}

	@Min(1)
	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	@NotNull
	public TimeUnit getKeepAliveTimeUnit() {
		return keepAliveTimeUnit;
	}

	@Min(1)
	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public String getName() {
		return name;
	}

	@NotNull
	public ThreadFactory getThreadFactory() {
		if (StringUtils.isBlank(name) && !daemon) {
			return Executors.defaultThreadFactory();
		}

		return new ThreadFactory() {
			private final AtomicInteger threadCounter = new AtomicInteger(0);

			@Override
			public Thread newThread(final Runnable r) {
				final Thread t = new Thread(r, String.format("%s-%d", name, threadCounter.incrementAndGet()));
				t.setDaemon(daemon);
				return t;
			}
		};
	}

	@NotNull
	public BlockingQueue<Runnable> getWorkQueue() {
		return workQueue;
	}

	@Override
	public int hashCode() {
		final String handlerClass = getHandler() != null ? getHandler().getClass().getName() : "null";
		final String workQueueClass = getWorkQueue() != null ? getWorkQueue().getClass().getName() : "null";
		return Objects.hash(name,
		        corePoolSize, maximumPoolSize,
		        keepAliveTime, keepAliveTimeUnit,
		        daemon, allowCoreThreadTimeOut,
		        handlerClass, workQueueClass);
	}

	@PostConstruct
	public void init() {
		LoggerFactory.getLogger(getClass()).info("{}", this);
	}

	public boolean isAllowCoreThreadTimeOut() {
		return allowCoreThreadTimeOut;
	}

	public boolean isDaemon() {
		return daemon;
	}

	public void setAllowCoreThreadTimeOut(final boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
	}

	public void setCorePoolSize(final int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public void setDaemon(final boolean daemon) {
		this.daemon = daemon;
	}

	public void setHandler(final RejectedExecutionHandler handler) {
		this.handler = handler;
	}

	public void setKeepAliveTime(final long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public void setKeepAliveTimeUnit(final TimeUnit unit) {
		keepAliveTimeUnit = unit;
	}

	public void setMaximumPoolSize(final int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setWorkQueue(final BlockingQueue<Runnable> workQueue) {
		this.workQueue = workQueue;
	}

	@Override
	public String toString() {
		final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
		sb.append("name", name);
		sb.append("corePoolSize", corePoolSize);
		sb.append("keepAliveTime", keepAliveTime);
		sb.append("keepAliveTimeUnit", keepAliveTimeUnit);
		sb.append("daemon", daemon);
		sb.append("allowCoreThreadTimeOut", allowCoreThreadTimeOut);
		if (workQueue != null) {
			sb.append("workQueue", workQueue.getClass().getName());
		}
		if (handler != null) {
			sb.append("handler", handler.getClass().getName());
		}
		return sb.toString();
	}

}
