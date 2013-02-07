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
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jetty.http.HttpStatus;
import org.springframework.util.Assert;

import com.azaptree.services.commons.xml.jaxb.JAXBContextCache;
import com.azaptree.wadl.Method;
import com.azaptree.wadl.Representation;
import com.azaptree.wadl.Response;
import com.google.common.base.Optional;

/**
 * Context must be of type WebCommandContext<T, V>
 * 
 * Provides support for XML message based commands, i.e., the request and response messages are XML
 * 
 * @author alfio
 * 
 * @param <T>
 *            JAXB class for request message
 * @param <V>
 *            JAXB class for response message
 */
public abstract class WebXmlRequestCommand<T, V> extends WebRequestCommand<T, V> {

	protected volatile JAXBContext jaxbContext;

	/**
	 * No request or response message is required.
	 * 
	 */
	public WebXmlRequestCommand() {
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
	public WebXmlRequestCommand(final Class<T> requestClass, final Class<V> responseClass) {
		super(requestClass, responseClass);
		this.getJaxbContext();
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
	public WebXmlRequestCommand(final String name, final Class<T> requestClass, final Class<V> responseClass) {
		super(name, requestClass, responseClass);
		this.getJaxbContext();
	}

	@Override
	public void definePostMethodWadl(final Method method) {
		if (hasXmlSchema()) {
			if (getRequestClass().isPresent()) {
				final com.azaptree.wadl.Request request = new com.azaptree.wadl.Request();
				method.setRequest(request);
				final Representation representation = new Representation();
				representation.setMediaType("application/xml");
				final Optional<QName> elementName = getRequestXmlElement();
				if (elementName.isPresent()) {
					representation.setElement(elementName.get());
				}
				request.getRepresentation().add(representation);
			}

			if (getResponseClass().isPresent()) {
				final Response response = new Response();
				method.getResponse().add(response);
				response.getStatus().add(Long.valueOf(HttpStatus.OK_200));
				final Representation representation = new Representation();
				representation.setMediaType("application/xml");

				final Optional<QName> elementName = getResponseXmlElement();
				if (elementName.isPresent()) {
					representation.setElement(elementName.get());
				}
				response.getRepresentation().add(representation);
			}
		}
	}

	@Override
	public void generateSchema(final OutputStream os) {
		final Optional<JAXBContext> jaxbContext = getJaxbContext();
		if (!jaxbContext.isPresent()) {
			return;
		}
		try {
			jaxbContext.get().generateSchema(new SchemaOutputResolver() {

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

	public Optional<JAXBContext> getJaxbContext() {
		if (jaxbContext != null) {
			return Optional.of(jaxbContext);
		}

		if (hasXmlSchema()) {
			final Set<String> packageNames = new TreeSet<>();

			if (requestClass != null) {
				packageNames.add(requestClass.getPackage().getName());
			}

			if (responseClass != null) {
				packageNames.add(responseClass.getPackage().getName());
			}

			final StringBuilder sb = new StringBuilder(128);
			for (final String packageName : packageNames) {
				sb.append(packageName).append(':');
			}
			sb.delete(sb.length() - 1, sb.length());
			final String jaxbContextPath = sb.toString();
			log.info("jaxbContextPath : {}", jaxbContextPath);

			this.jaxbContext = JAXBContextCache.get(jaxbContextPath);
			return Optional.of(jaxbContext);
		}
		return Optional.absent();
	}

	/**
	 * Used to generate the WADL.
	 * 
	 * Not all commands require an XML request message
	 * 
	 * @return
	 */
	public abstract Optional<QName> getRequestXmlElement();

	/**
	 * Used to generate the WADL.
	 * 
	 * Not all commands generate an XML response message
	 * 
	 * @return
	 */
	public abstract Optional<QName> getResponseXmlElement();

	public boolean hasXmlSchema() {
		return requestClass != null || responseClass != null;
	}

	@Override
	protected void writeResponseMessage(final HttpServletResponse httpResponse, final Object message) {
		Assert.notNull(httpResponse, "httpResponse is required");
		Assert.notNull(message, "message is required");
		final Optional<JAXBContext> jaxbContext = getJaxbContext();
		Assert.isTrue(jaxbContext.isPresent(), "JAXBContext is required");
		httpResponse.setContentType("application/xml");
		httpResponse.setCharacterEncoding("UTF-8");
		Marshaller marshaller;
		try {
			marshaller = jaxbContext.get().createMarshaller();
		} catch (final JAXBException e) {
			throw new IllegalStateException("failed to create JAXB marshaller", e);
		}

		try {
			marshaller.marshal(message, httpResponse.getOutputStream());
		} catch (JAXBException | IOException e) {
			throw new RuntimeException("Failed to write JAXB message response", e);
		}
	}

}
