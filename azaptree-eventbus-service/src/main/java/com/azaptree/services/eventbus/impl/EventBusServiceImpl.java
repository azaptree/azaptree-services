package com.azaptree.services.eventbus.impl;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.azaptree.services.eventbus.EventBusService;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * By default, the Spring bean name is used as the EventBus name.
 * 
 * Registers a DeadEvent event handler. Dead events are events that posted, but there are no registered subscribers for that
 * event type. When a DeadEvent is received, it logs a WARNING message.
 * 
 * @author alfio
 * 
 */
@Component
public class EventBusServiceImpl implements EventBusService, BeanNameAware {

	private EventBus eventBus;
	private String beanName;

	public EventBusServiceImpl() {
	}

	public EventBusServiceImpl(final String eventBusName) {
		setBeanName(eventBusName);
		init();
	}

	@Override
	public String getEventBusName() {
		return beanName;
	}

	@PostConstruct
	public void init() {
		if (eventBus == null) {
			eventBus = new EventBus(beanName);
		}

		eventBus.register(this);
		log.info("Created EventBus: {}", beanName);
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
