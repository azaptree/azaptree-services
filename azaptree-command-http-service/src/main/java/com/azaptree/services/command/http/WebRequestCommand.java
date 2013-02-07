package com.azaptree.services.command.http;

/*
 * #%L
 * AZAPTREE-COMMAND-HTTP-SERVICE
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

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.chain.Context;
import org.springframework.util.Assert;

import com.azaptree.services.command.impl.CommandSupport;
import com.azaptree.wadl.Method;
import com.azaptree.wadl.Resource;
import com.google.common.base.Optional;

/**
 * Context must be of type WebCommandContext<T, V>
 * 
 * @author alfio
 * 
 * @param <T>
 *            request message class
 * @param <V>
 *            response message class
 */
public abstract class WebRequestCommand<T, V> extends CommandSupport {

	protected final Class<T> requestClass;
	protected final Class<V> responseClass;

	/**
	 * No request or response message is required.
	 * 
	 */
	public WebRequestCommand() {
		this(null, null);
	}

	/**
	 * 
	 * 
	 * @param requestClass
	 *            OPTIONAL
	 * @param responseClass
	 *            OPTIONAL
	 * @throws JAXBException
	 */
	public WebRequestCommand(final Class<T> requestClass, final Class<V> responseClass) {
		this.requestClass = requestClass;
		this.responseClass = responseClass;
	}

	/**
	 * 
	 * @param name
	 *            REQUIRED
	 * @param requestClass
	 *            OPTIONAL
	 * @param responseClass
	 *            OPTIONAL
	 */
	public WebRequestCommand(final String name, final Class<T> requestClass, final Class<V> responseClass) {
		super(name);
		this.requestClass = requestClass;
		this.responseClass = responseClass;
	}

	/**
	 * Creates a relative resource for this command. The resource path is set to the command name, and a POST method is added:
	 * 
	 * <code>
		<resource path="command-name">
            <method name="POST"></method>
        </resource>
	 * </code>
	 * 
	 * Override this method to further describe the REST API for the is command. For example:
	 * 
	 * <ul>
	 * <li>you may want add documentation, via Doc elements</li>
	 * <li>describe further HTTP error responses</li>
	 * </ul>
	 * 
	 * @return
	 */
	public Resource createCommandResourceWadl() {
		final Resource resource = new Resource();
		resource.setPath(getName());

		final Method method = new Method();
		method.setName("POST");
		resource.getMethodOrResource().add(method);

		definePostMethodWadl(method);

		return resource;
	}

	/**
	 * Override to define the POST method
	 * 
	 * @param method
	 */
	protected void definePostMethodWadl(final Method method) {
		// no-op
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean executeCommand(final Context ctx) {
		return executeCommand((WebCommandContext<T, V>) ctx);
	}

	protected abstract boolean executeCommand(WebCommandContext<T, V> ctx);

	/**
	 * Map generate an XML schema or JSON schema.
	 * 
	 * @param os
	 */
	public void generateSchema(final OutputStream os) {
		// no-op
	}

	public Optional<Class<T>> getRequestClass() {
		if (requestClass == null) {
			return Optional.absent();
		}
		return Optional.of(requestClass);
	}

	public Optional<Class<V>> getResponseClass() {
		if (responseClass == null) {
			return Optional.absent();
		}
		return Optional.of(responseClass);
	}

	/**
	 * Knows how to marshal the the message to the HTTP response outputstream
	 * 
	 * @param httpResponse
	 * @param message
	 */
	protected abstract void writeResponseMessage(final HttpServletResponse httpResponse, final Object message);

	protected void writeResponseMessage(final WebCommandContext<T, V> ctx) {
		Assert.notNull(ctx, "ctx is required");
		writeResponseMessage(ctx.getHttpServletResponse(), ctx.getResponseMessage());
	}

}
