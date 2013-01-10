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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.chain.Context;

import com.azaptree.services.command.impl.CommandSupport;

/**
 * Context must be of type WebCommandContext<T, V>.
 * 
 * @author alfio
 * 
 * @param <T>
 *            JAXB class for request message
 * @param <V>
 *            JAXB class for response message
 */
public abstract class WebRequestCommand<T, V> extends CommandSupport {

	private volatile JAXBContext jaxbContext;

	private final Class<T> requestClass;
	private final Class<V> responseClass;

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

	@SuppressWarnings("unchecked")
	@Override
	protected boolean executeCommand(final Context ctx) {
		return executeCommand((WebCommandContext<T, V>) ctx);
	}

	protected abstract boolean executeCommand(WebCommandContext<T, V> ctx);

	public JAXBContext getJaxbContext() {
		if (jaxbContext != null) {
			return jaxbContext;
		}

		if (requestClass != null || responseClass != null) {
			synchronized (this) {
				if (jaxbContext != null) {
					return jaxbContext;
				}

				final List<Class<?>> classes = new ArrayList<>(2);
				if (requestClass != null) {
					classes.add(requestClass);
				}
				if (responseClass != null) {
					classes.add(responseClass);
				}
				final Class<?>[] jaxbClasses = classes.toArray(new Class<?>[classes.size()]);
				try {
					jaxbContext = JAXBContext.newInstance(jaxbClasses);
				} catch (final JAXBException e) {
					throw new IllegalStateException("Failed to create JAXContext for: " + Arrays.toString(jaxbClasses));
				}
			}
		}
		return jaxbContext;
	}

	public Class<T> getRequestClass() {
		return requestClass;
	}

	public Class<V> getResponseClass() {
		return responseClass;
	}

	public void generateSchema(final OutputStream os) {
		final JAXBContext jaxbContext = getJaxbContext();
		if (jaxbContext == null) {
			return;
		}
		try {
			jaxbContext.generateSchema(new SchemaOutputResolver() {

				@Override
				public Result createOutput(final String namespaceUri, final String suggestedFileName) throws IOException {
					final StreamResult result = new StreamResult(os);
					result.setSystemId("");
					return result;
				}
			});
		} catch (final IOException e) {
			throw new RuntimeException("generateSchema() failed", e);
		}
	}
}
