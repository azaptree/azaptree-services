package com.azaptree.services.spring.application;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.azaptree.services.spring.application.config.SpringApplicationServiceConfig;

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

/**
 * Used to launch a Spring Application.
 * 
 * Command Line Usage: java com.azaptree.services.spring.application.SpringApplicationService [config.xml]
 * 
 * where [config.xml] = XML file location (loaded using Spring ResourceLoader) that must validate against spring-application-service.xsd
 * 
 * Example [config.xml]:
 * 
 * <pre>
 * 	classpath:config.xml	- loads config.xml from the classpath
 *  file:config.xml			- loads config.xml from the file system
 * </pre>
 * 
 * The process may be killed via sending the process a SIGTERM signal, i.e., kill [pid]
 * 
 * @author alfio
 * 
 */
public class SpringApplicationService {

	private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

	private static ApplicationContext applicationContext;

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	private static Logger getLogger() {
		return LoggerFactory.getLogger(SpringApplicationService.class);
	}

	private static void logConfig(final SpringApplicationServiceConfig config) {
		final Logger log = getLogger();
		log.info(config.toString());
		logDebugSystemProperties(log);
		logEnvironment(log);
	}

	private static void logDebugSystemProperties(final Logger log) {
		Assert.notNull(log);
		if (log.isDebugEnabled()) {
			final TreeMap<String, String> sysProps = new TreeMap<>();
			for (final Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
				sysProps.put(entry.getKey().toString(), entry.getValue().toString());
			}
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(bos);
			for (final Map.Entry<String, String> entry : sysProps.entrySet()) {
				ps.print(entry.getKey());
				ps.print('=');
				ps.println(entry.getValue());
			}
			log.debug("System Properties:\n{}", bos);
		}
	}

	private static void logEnvironment(final Logger log) {
		if (log.isDebugEnabled()) {
			final TreeMap<String, String> sysProps = new TreeMap<>(System.getenv());
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(bos);
			for (final Map.Entry<String, String> entry : sysProps.entrySet()) {
				ps.print(entry.getKey());
				ps.print('=');
				ps.println(entry.getValue());
			}
			log.debug("Environment:\n{}", bos);
		}
	}

	private static void logSpringBeans() {
		final ToStringBuilder sb = new ToStringBuilder(applicationContext, ToStringStyle.MULTI_LINE_STYLE);
		sb.append("beanDefinitionCount", applicationContext.getBeanDefinitionCount());
		for (final String beanName : applicationContext.getBeanDefinitionNames()) {
			sb.append(beanName, applicationContext.getBean(beanName).getClass().getName());
		}
		getLogger().info(sb.toString());
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception {
		validate(args);
		final SpringApplicationServiceConfig config = new SpringApplicationServiceConfig(args[0]);
		setSystemProperties(config.getJvmSystemProperties());
		logConfig(config);
		try (final AnnotationConfigApplicationContext applicationContext = config.createAnnotationConfigApplicationContext()) {
			SpringApplicationService.applicationContext = applicationContext;
			logSpringBeans();
			shutdownLatch.await();
			getLogger().info("SHUTDOWN SIGNALLED");
		} finally {
			getLogger().info("SHUTDOWN COMPLETE");
		}
	}

	private static void setSystemProperties(final Properties props) {
		if (CollectionUtils.isEmpty(props)) {
			return;
		}
		System.getProperties().putAll(props);
	}

	/**
	 * Triggers an orderly application shutdown
	 */
	public static void shutdown() {
		shutdownLatch.countDown();
	}

	private static void validate(final String[] args) {
		if (args.length == 0) {
			final StringWriter sw = new StringWriter(256);
			final PrintWriter pw = new PrintWriter(sw);
			pw.println();
			pw.println(StringUtils.repeat("=", 160));
			pw.println("Usage: java com.azaptree.services.spring.application.SpringApplicationService <config.xml>");
			pw.println();
			pw.println("       where <config.xml> = XML file location (loaded using Spring ResourceLoader) that must validate against spring-application-service.xsd");
			pw.println(StringUtils.repeat("=", 160));
			throw new IllegalArgumentException(sw.toString());
		}
	}

}
