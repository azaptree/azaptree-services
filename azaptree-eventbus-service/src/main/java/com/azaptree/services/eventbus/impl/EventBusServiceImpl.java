package com.azaptree.services.eventbus.impl;

/*
 * #%L
 * EventBus Service
 * %%
 * Copyright (C) 2012 AZAPTREE.COM
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

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.azaptree.services.eventbus.EventBusService;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * By default, the Spring bean name is used as the EventBus name.
 * 
 * Registers a DeadEvent event handler. Dead events are events that posted, but there are no registered subscribers for that
 * event type. When a DeadEvent is received, it logs a WARNING message.
 * 
 * @author Alfio Zappala
 * 
 */
@Service
@ManagedResource
public class EventBusServiceImpl implements EventBusService, BeanNameAware {

	private EventBus eventBus;
	private Executor executor;
	private String beanName;
	private String eventBusName;

	/**
	 * Creates a synchronous EventBus, i.e., events are dispatched on the same thread.
	 * 
	 * The beanName is used as the EventBus identifier.
	 */
	public EventBusServiceImpl() {
	}

	/**
	 * Creates an <a href="http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/index.html">AsyncEventBus</a>
	 * 
	 * @param executor
	 */
	public EventBusServiceImpl(final Executor executor) {
		Assert.notNull(executor);
		this.executor = executor;
	}

	public EventBusServiceImpl(final String eventBusName) {
		init(eventBusName, null);
	}

	public EventBusServiceImpl(final String eventBusName, final Executor executor) {
		init(eventBusName, executor);
	}

	private void checkSubscribeMethodExists(final Object eventHandler) {
		for (final Method m : eventHandler.getClass().getMethods()) {
			if (m.getAnnotation(Subscribe.class) != null) {
				return;
			}
		}

		throw new IllegalArgumentException("eventHandler has no methods annotated with @Subscribe");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.azaptree.services.eventbus.impl.EventBusServiceJmxApi#getEventBusName()
	 */
	@Override
	@ManagedAttribute
	public String getEventBusName() {
		return eventBusName;
	}

	@PostConstruct
	public void init() {
		init(beanName, executor);
	}

	public void init(final String eventBusName, final Executor executor) {
		if (eventBus != null) {
			log.debug("eventBus has already been created");
			return;
		}

		Assert.hasText(eventBusName, "eventBusName is required");
		this.eventBusName = eventBusName;

		if (executor != null) {
			eventBus = new AsyncEventBus(eventBusName, executor);
		} else {
			eventBus = new EventBus(eventBusName);
		}

		eventBus.register(this);
		log.info("Created EventBus: {} -> {}", beanName, eventBus.getClass().getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.azaptree.services.eventbus.impl.EventBusServiceJmxApi#isAsynchronous()
	 */
	@Override
	@ManagedAttribute
	public boolean isAsynchronous() {
		if (eventBus == null) {
			throw new IllegalStateException("EventBus has not yet been created");
		}
		return eventBus instanceof AsyncEventBus;
	}

	/**
	 * Logs dead event as warnings. Dead events are events that posted, but there are no registered subscribers for that
	 * event type.
	 * 
	 * @param deadEvent
	 */
	@Subscribe
	public void logDeadEvent(final DeadEvent deadEvent) {
		final Object event = deadEvent.getEvent();
		log.warn("{} : DeadEvent : {} : {}", beanName, event.getClass().getName(), event);
	}

	@Override
	public void post(final Object event) {
		Assert.notNull(event, "event is required");
		eventBus.post(event);
	}

	@Override
	public void register(final Object eventHandler) {
		Assert.notNull(eventHandler, "eventHandler is required");
		checkSubscribeMethodExists(eventHandler);
		eventBus.register(eventHandler);
		log.info("{} : registered event handler: {} ", beanName, eventHandler.getClass().getName());
	}

	@Override
	public void setBeanName(final String name) {
		Assert.hasText(name);
		beanName = name;
	}

	@Override
	public void unregister(final Object eventHandler) {
		Assert.notNull(eventHandler, "eventHandler is required");
		eventBus.unregister(eventHandler);
		log.info("{} : unregistered event handler: {} ", beanName, eventHandler.getClass().getName());
	}
}
