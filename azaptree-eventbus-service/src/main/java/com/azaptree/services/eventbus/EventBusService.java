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
package com.azaptree.services.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper service around <a href"http://code.google.com/p/guava-libraries/wiki/EventBusExplained">Guava's EventBus</a>
 * 
 * 
 * @author Alfio Zappala
 * 
 */
public interface EventBusService {
	public static final Logger log = LoggerFactory.getLogger(EventBusService.class);

	String getEventBusName();

	/**
	 * Indicates whether events are dispatched to handlers synchronously on the same thread or asynchronously via an Executor
	 * 
	 * @return
	 */
	boolean isAsynchronous();

	/**
	 * 
	 * @param event
	 */
	void post(Object event);

	/**
	 * Each of the object's methods that are annotated with the com.google.common.eventbus.Subscribe annotation will be
	 * registered as handler methods.
	 * 
	 * Immediately upon invoking register(Object) , the listener being registered is checked for the well-formedness of its handler methods. Specifically, any
	 * methods marked with @Subscribe must take only a single argument.
	 * 
	 * Any violations of this rule will cause an IllegalArgumentException to be thrown.
	 * 
	 * @param eventHandler
	 * @exception IllegalArgumentException
	 */
	void register(Object eventHandler);

	void unregister(Object eventHandler);
}
