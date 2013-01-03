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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.azaptree.services.spring.application.config.SpringApplicationService.ConfigurationClasses;
import com.azaptree.services.spring.application.config.SpringApplicationService.ConfigurationPackages;
import com.azaptree.services.spring.application.config.SpringApplicationService.JvmSystemProperties;
import com.azaptree.services.spring.application.config.SpringApplicationService.JvmSystemProperties.Prop;

public class SpringApplicationServiceConfig {

	private Class<?>[] configurationClasses;

	private Package[] configurationPackages;

	private Properties jvmSystemProperties;

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
		if (ArrayUtils.isNotEmpty(configurationClasses))
			for (final Class<?> c : configurationClasses) {
				ctx.register(c);
			}
		if (ArrayUtils.isNotEmpty(configurationPackages)) {
			for (final Package p : configurationPackages) {
				ctx.scan(p.getName());
			}
		}

		ctx.refresh();
		ctx.registerShutdownHook();
		return ctx;
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
	 * Packages to scan for Spring Configuration classes - used to configure a Spring ApplicationContext using Java based configuration
	 * 
	 * @return
	 */
	public Package[] getConfigurationPackages() {
		return configurationPackages;
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
		loadConfigurationClasses(config);
		loadConfigurationPackages(config);
		loadJvmSystemProperties(config);
	}

	private void loadConfigurationClasses(final SpringApplicationService config) throws ClassNotFoundException {
		final ClassLoader cl = getClass().getClassLoader();
		final ConfigurationClasses configurationClasses = config.getConfigurationClasses();
		if (configurationClasses != null && !CollectionUtils.isEmpty(configurationClasses.getClazz())) {
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

	private void loadConfigurationPackages(final SpringApplicationService config) {
		final ConfigurationPackages configurationPackages = config.getConfigurationPackages();
		if (configurationPackages != null && !CollectionUtils.isEmpty(configurationPackages.getPackage())) {
			final List<Package> packages = new ArrayList<>(configurationPackages.getPackage().size());
			for (final String pkg : configurationPackages.getPackage()) {
				packages.add(Package.getPackage(pkg.trim()));
			}
			this.configurationPackages = packages.toArray(new Package[packages.size()]);
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

	private SpringApplicationService parse(final InputStream xml) throws JAXBException {
		Assert.notNull(xml);
		final String packageName = SpringApplicationService.class.getPackage().getName();
		final JAXBContext jc = JAXBContext.newInstance(packageName);
		final Unmarshaller u = jc.createUnmarshaller();
		return (SpringApplicationService) u.unmarshal(xml);
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
		if (ArrayUtils.isNotEmpty(configurationPackages)) {
			final String[] names = new String[configurationPackages.length];
			for (int i = 0; i < names.length; i++) {
				names[i] = configurationPackages[i].getName();
			}
			sb.append("configurationPackages", Arrays.toString(names));
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
