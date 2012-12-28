package com.azaptree.services.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public interface EventBusService {
	public static final Logger log = LoggerFactory.getLogger(EventBusService.class);

	String getEventBusName();

	/**
	 * 
	 * @param event
	 */
	void post(Object event);

	/**
	 * Each of the object's methods that are annotated with the com.google.common.eventbus.Subscribe annotation will be
	 * registered as handler methods.
	 * 
	 * @param eventHandler
	 */
	void register(Object eventHandler);

	void unregister(Object eventHandler);
}
