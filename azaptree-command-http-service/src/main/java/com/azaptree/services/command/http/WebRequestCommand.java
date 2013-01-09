package com.azaptree.services.command.http;

import org.apache.commons.chain.Context;

import com.azaptree.services.command.impl.CommandSupport;

/**
 * Context must be of type WebCommandContext<T, V>
 * 
 * @author alfio
 * 
 * @param <T>
 *            JAXB class for request message
 * @param <V>
 *            JAXB class for response message
 */
public abstract class WebRequestCommand<T, V> extends CommandSupport {

	public WebRequestCommand() {
	}

	public WebRequestCommand(final String name) {
		super(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean executeCommand(final Context ctx) {
		return executeCommand((WebCommandContext<T, V>) ctx);
	}

	protected abstract boolean executeCommand(WebCommandContext<T, V> ctx);

}
