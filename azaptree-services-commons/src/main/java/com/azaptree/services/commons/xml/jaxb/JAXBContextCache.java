package com.azaptree.services.commons.xml.jaxb;

/*
 * #%L
 * AZAPTREE-SERVICES-COMMONS
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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Used to cache JAXBContexts at the JVM level
 * 
 * @author alfio
 * 
 */
public abstract class JAXBContextCache {

	private static final Map<String /* JAXB context path */, JAXBContext> jaxbContexts = new HashMap<>();

	/**
	 * If the JAXBContext is not cached, then it will create a new instance and cache it.
	 * 
	 * @param contextPath
	 *            REQUIRED
	 * @return
	 */
	public static JAXBContext get(final String contextPath) {
		Assert.hasText(contextPath, "contextPath is required");
		JAXBContext ctx = jaxbContexts.get(contextPath);
		if (ctx == null) {
			try {
				ctx = JAXBContext.newInstance(contextPath);
			} catch (final JAXBException e) {
				throw new IllegalArgumentException("Failed to create JAXBContext - invalid JAXB context path: " + contextPath, e);
			}
			jaxbContexts.put(contextPath, ctx);
			LoggerFactory.getLogger(JAXBContextCache.class).info("cached : {}", contextPath);
		}
		return ctx;
	}

}
