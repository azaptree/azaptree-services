package com.azaptree.services.executor;

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

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

public interface ThreadPoolExecutorJmxApi {

	@ManagedOperation(
	        description = "Sets the policy governing whether core threads may time out and terminate if no tasks arrive within the keep-alive time, being replaced if needed when new tasks arrive.")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "value", description = "true if should time out, else false") })
	void allowCoreThreadTimeOut(boolean value);

	@ManagedAttribute(description = "Returns the approximate number of threads that are actively executing tasks.")
	int getActiveCount();

	@ManagedAttribute(description = "Returns the approximate total number of tasks that have completed execution.")
	long getCompletedTaskCount();

	@ManagedAttribute(description = "Returns the core number of threads.")
	int getCorePoolSize();

	@ManagedAttribute(description = "Returns the largest number of threads that have ever simultaneously been in the pool.")
	int getLargestPoolSize();

	@ManagedAttribute(description = "Returns the maximum allowed number of threads.")
	int getMaximumPoolSize();

	@ManagedAttribute
	String getName();

	@ManagedAttribute(description = "Returns the current number of threads in the pool.")
	int getPoolSize();

	@ManagedAttribute(description = "Returns the approximate total number of tasks that have ever been scheduled for execution.")
	long getTaskCount();

	@ManagedAttribute(
	        description = "Returns true if this pool allows core threads to time out and terminate if no tasks arrive within the keepAlive time, being replaced if needed when new tasks arrive.")
	boolean isCoreThreadTimeOutAllowed();

	@ManagedAttribute
	boolean isPaused();

	@ManagedOperation
	void pause();

	@ManagedOperation(description = "Tries to remove from the work queue all Future tasks that have been cancelled.")
	void purge();

	@ManagedOperation
	void resume();

	@ManagedOperation(
	        description = "Sets the core number of threads. If the new value is smaller than the current value, excess existing threads will be terminated when they next become idle. If larger, new threads will, if needed, be started to execute any queued tasks")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "corePoolSize", description = "the new core size") })
	void setCorePoolSize(int corePoolSize);

	@ManagedOperation(description = "Sets the time limit in seconds for which threads may remain idle before being terminated.")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "time",
	        description = "the time in seconds to wait. A time value of zero will cause excess threads to terminate immediately after executing tasks.") })
	void setKeepAliveTimeSecs(int time);

	@ManagedOperation(description = "Sets the maximum allowed number of threads.")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "maximumPoolSize", description = "the new maximum") })
	void setMaximumPoolSize(int maximumPoolSize);

}