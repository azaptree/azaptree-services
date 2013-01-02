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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.azaptree.services.spring.application.config.SpringApplicationService.ConfigurationClasses;
import com.azaptree.services.spring.application.config.SpringApplicationService.ConfigurationPackages;

public class SpringApplicationServiceConfig {

	private Class<?>[] configurationClasses;

	private Package[] configurationPackages;

	public SpringApplicationServiceConfig(final InputStream xml) throws JAXBException, ClassNotFoundException {
		Assert.notNull(xml);
		final String packageName = SpringApplicationService.class.getPackage().getName();
		final JAXBContext jc = JAXBContext.newInstance(packageName);
		final Unmarshaller u = jc.createUnmarshaller();
		final SpringApplicationService config = (SpringApplicationService) u.unmarshal(xml);

		final ClassLoader cl = getClass().getClassLoader();
		final ConfigurationClasses configurationClasses = config.getConfigurationClasses();
		if (configurationClasses != null && !CollectionUtils.isEmpty(configurationClasses.getClazz())) {
			final List<Class<?>> configClasses = new ArrayList<>(configurationClasses.getClazz().size());
			for (String clazz : configurationClasses.getClazz()) {
				final Class<?> c = cl.loadClass(clazz);
				if (c.getAnnotation(Configuration.class) == null) {
					throw new IllegalArgumentException(String.format("Class must be annotated with @org.springframework.context.annotation.Configuration: {}",
					        clazz));
				}
				configClasses.add(c);
			}
			this.configurationClasses = configClasses.toArray(new Class<?>[configClasses.size()]);
		}

		final ConfigurationPackages configurationPackages = config.getConfigurationPackages();
		if (configurationPackages != null && !CollectionUtils.isEmpty(configurationPackages.getPackage())) {
			final List<Package> packages = new ArrayList<>(configurationPackages.getPackage().size());
			for (String pkg : configurationPackages.getPackage()) {
				packages.add(Package.getPackage(pkg));
			}
			this.configurationPackages = packages.toArray(new Package[packages.size()]);
		}
	}

	public Class<?>[] getConfigurationClasses() {
		return configurationClasses;
	}

	public Package[] getConfigurationPackages() {
		return configurationPackages;
	}

	public void setConfigurationClasses(final Class<?>[] configurationClasses) {
		this.configurationClasses = configurationClasses;
	}

	public void setConfigurationPackages(final Package[] configurationPackages) {
		this.configurationPackages = configurationPackages;
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
		return sb.toString();
	}
}
