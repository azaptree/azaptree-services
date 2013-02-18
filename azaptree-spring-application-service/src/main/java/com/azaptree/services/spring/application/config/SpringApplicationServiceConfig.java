package com.azaptree.services.spring.application.config;

/*
 * #%L
 * AZAPTREE-SPRING-APPLICATION-SERVICE
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.xml.sax.SAXException;

import com.azaptree.services.commons.xml.XmlUtils;
import com.azaptree.services.spring.application.config.SpringApplicationService.ConfigurationClasses;
import com.azaptree.services.spring.application.config.SpringApplicationService.JvmSystemProperties;
import com.azaptree.services.spring.application.config.SpringApplicationService.JvmSystemProperties.Prop;
import com.azaptree.services.spring.application.config.SpringApplicationService.SpringProfiles;

public class SpringApplicationServiceConfig {

	private Class<?>[] configurationClasses;

	private Properties jvmSystemProperties;

	private String[] springProfiles;

	public SpringApplicationServiceConfig(final InputStream xml) throws JAXBException, ClassNotFoundException {
		init(xml);
	}

	public SpringApplicationServiceConfig(final String xmlLocation) throws IOException, ClassNotFoundException, JAXBException {
		Assert.hasText(xmlLocation, "xmlResourcePath is required");
		final PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
		final Resource resource = resourceResolver.getResource(xmlLocation);

		try (InputStream xml = resource.getInputStream()) {
			init(xml);
		}
	}

	public AnnotationConfigApplicationContext createAnnotationConfigApplicationContext() {
		final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		if (ArrayUtils.isNotEmpty(springProfiles)) {
			ctx.getEnvironment().setActiveProfiles(springProfiles);
		}

		if (ArrayUtils.isNotEmpty(configurationClasses)) {
			for (final Class<?> c : configurationClasses) {
				ctx.register(c);
			}
		}

		ctx.refresh();
		ctx.registerShutdownHook();
		return ctx;
	}

	private void generateSchema(final OutputStream os) {
		try {
			JAXBContext.newInstance(SpringApplicationService.class).generateSchema(new SchemaOutputResolver() {

				@Override
				public Result createOutput(final String namespaceUri, final String suggestedFileName) throws IOException {
					final StreamResult result = new StreamResult(os);
					result.setSystemId("");
					return result;
				}
			});
		} catch (final IOException | JAXBException e) {
			throw new RuntimeException("generateSchema() failed", e);
		}
	}

	/**
	 * Spring Configuration classes - used to configure a Spring ApplicationContext using Java based configuration
	 * 
	 * @return
	 */
	public Class<?>[] getConfigurationClasses() {
		return configurationClasses;
	}

	/**
	 * Before creating the Spring ApplicationContext, the specified JVM system properties will be set
	 * 
	 * @return
	 */
	public Properties getJvmSystemProperties() {
		return jvmSystemProperties;
	}

	private void init(final InputStream xml) throws JAXBException, ClassNotFoundException {
		Assert.notNull(xml, "xml is required");
		final SpringApplicationService config = parse(xml);
		log(config);
		loadConfigurationClasses(config);
		loadJvmSystemProperties(config);
		loadSpringProfiles(config);
	}

	private void loadSpringProfiles(SpringApplicationService config) {
		final SpringProfiles springProfiles = config.getSpringProfiles();
		if (springProfiles != null) {
			final List<String> profiles = springProfiles.getProfile();
			if (!CollectionUtils.isEmpty(profiles)) {
				this.springProfiles = profiles.toArray(new String[profiles.size()]);
			}
		}
	}

	private void loadConfigurationClasses(final SpringApplicationService config) throws ClassNotFoundException {
		final ConfigurationClasses configurationClasses = config.getConfigurationClasses();
		if (configurationClasses != null && !CollectionUtils.isEmpty(configurationClasses.getClazz())) {
			final ClassLoader cl = getClass().getClassLoader();
			final List<Class<?>> configClasses = new ArrayList<>(configurationClasses.getClazz().size());
			for (final String clazz : configurationClasses.getClazz()) {
				final Class<?> c = cl.loadClass(clazz.trim());
				if (c.getAnnotation(Configuration.class) == null) {
					throw new IllegalArgumentException(String.format("Class must be annotated with @org.springframework.context.annotation.Configuration: {}",
					        clazz));
				}
				configClasses.add(c);
			}
			this.configurationClasses = configClasses.toArray(new Class<?>[configClasses.size()]);
		}

	}

	private void loadJvmSystemProperties(final SpringApplicationService config) {
		final JvmSystemProperties props = config.getJvmSystemProperties();
		if (props != null && !CollectionUtils.isEmpty(props.getProp())) {
			jvmSystemProperties = new Properties();
			for (final Prop prop : props.getProp()) {
				jvmSystemProperties.setProperty(prop.getName(), prop.getValue().trim());
			}
		}
	}

	private void log(final SpringApplicationService config) {
		final StringWriter sw = new StringWriter();
		final Marshaller m;
		try {
			final JAXBContext ctx = JAXBContext.newInstance(SpringApplicationService.class.getPackage().getName());
			m = ctx.createMarshaller();
		} catch (final JAXBException e) {
			throw new IllegalStateException("Failed to create marshller ", e);
		}

		try {
			m.marshal(config, sw);
		} catch (final JAXBException e) {
			throw new IllegalStateException("Failed to marshal SpringApplicationService", e);
		}

		LoggerFactory.getLogger(getClass()).info("XML config:\n{}", XmlUtils.prettyFormatXml(sw.toString(), 4));
	}

	private SpringApplicationService parse(final InputStream xml) throws JAXBException {
		Assert.notNull(xml);
		final JAXBContext jc = JAXBContext.newInstance(SpringApplicationService.class);
		final Unmarshaller u = jc.createUnmarshaller();
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
		generateSchema(bos);
		try {
			final SpringApplicationService springApplicationService = (SpringApplicationService) u.unmarshal(xml);
			final Schema schema = schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(bos.toByteArray())));
			final Validator validator = schema.newValidator();
			validator.validate(new JAXBSource(jc, springApplicationService));
			return springApplicationService;
		} catch (SAXException | IOException e) {
			throw new IllegalArgumentException("Failed to parse XML. The XML must conform to the following schema:\n" + bos, e);
		}
	}

	@Override
	public String toString() {
		final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
		if (ArrayUtils.isNotEmpty(configurationClasses)) {
			final String[] names = new String[configurationClasses.length];
			for (int i = 0; i < names.length; i++) {
				names[i] = configurationClasses[i].getName();
			}
			sb.append("configurationClasses", Arrays.toString(names));
		}
		if (ArrayUtils.isNotEmpty(springProfiles)) {
			sb.append("springProfiles", Arrays.toString(springProfiles));
		}

		if (!CollectionUtils.isEmpty(jvmSystemProperties)) {
			final StringWriter sw = new StringWriter(256);
			try {
				jvmSystemProperties.store(sw, "JVM System Propperties");
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			sb.append("jvmSystemProperties", sw.toString());
		}

		return sb.toString();
	}
}
