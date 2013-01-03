package test.com.azaptree.services.spring.application.config;

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

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.spring.application.config.SpringApplicationServiceConfig;

public class SpringApplicationServiceConfigTest {

	final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testValidXmlConfig1() throws IOException, ClassNotFoundException, JAXBException {
		try (final InputStream is = getClass().getResourceAsStream("/spring-application-service.xml")) {
			final SpringApplicationServiceConfig config = new SpringApplicationServiceConfig(is);
			log.info(config.toString());

			Assert.assertEquals(config.getConfigurationClasses().length, 2);
			final Class<?>[] configClasses = config.getConfigurationClasses();
			Assert.assertTrue(ArrayUtils.contains(configClasses, ApplicationSpringConfig.class));
			Assert.assertTrue(ArrayUtils.contains(configClasses, WebApplicationSpringConfig.class));

			Assert.assertNull(config.getConfigurationPackages());
		}
	}

	@Test
	public void testValidXmlConfig2() throws IOException, ClassNotFoundException, JAXBException {
		try (final InputStream is = getClass().getResourceAsStream("/spring-application-service2.xml")) {
			final SpringApplicationServiceConfig config = new SpringApplicationServiceConfig(is);
			log.info(config.toString());

			Assert.assertEquals(config.getConfigurationPackages().length, 1);
			Assert.assertTrue(ArrayUtils.contains(config.getConfigurationPackages(), ApplicationSpringConfig.class.getPackage()));

			Assert.assertNull(config.getConfigurationClasses());
		}
	}

	@Test
	public void testValidXmlConfig3() throws IOException, ClassNotFoundException, JAXBException {
		try (final InputStream is = getClass().getResourceAsStream("/spring-application-service3.xml")) {
			final SpringApplicationServiceConfig config = new SpringApplicationServiceConfig(is);
			log.info(config.toString());

			Assert.assertEquals(config.getConfigurationPackages().length, 1);
			Assert.assertTrue(ArrayUtils.contains(config.getConfigurationPackages(), ApplicationSpringConfig.class.getPackage()));

			Assert.assertEquals(config.getConfigurationClasses().length, 2);
			final Class<?>[] configClasses = config.getConfigurationClasses();
			Assert.assertTrue(ArrayUtils.contains(configClasses, ApplicationSpringConfig.class));
			Assert.assertTrue(ArrayUtils.contains(configClasses, WebApplicationSpringConfig.class));
		}
	}

	@Test
	public void testValidXmlConfig4() throws IOException, ClassNotFoundException, JAXBException {
		try (final InputStream is = getClass().getResourceAsStream("/spring-application-service4.xml")) {
			final SpringApplicationServiceConfig config = new SpringApplicationServiceConfig(is);
			log.info(config.toString());

			Assert.assertEquals(config.getConfigurationPackages().length, 1);
			Assert.assertTrue(ArrayUtils.contains(config.getConfigurationPackages(), ApplicationSpringConfig.class.getPackage()));

			Assert.assertEquals(config.getConfigurationClasses().length, 2);
			final Class<?>[] configClasses = config.getConfigurationClasses();
			Assert.assertTrue(ArrayUtils.contains(configClasses, ApplicationSpringConfig.class));
			Assert.assertTrue(ArrayUtils.contains(configClasses, WebApplicationSpringConfig.class));

			Assert.assertNotNull(config.getJvmSystemProperties());
			Assert.assertEquals(config.getJvmSystemProperties().get("app.env"), "DEV");
			Assert.assertEquals(config.getJvmSystemProperties().get("app.profile"), "PROFILE_A");
		}
	}

	@Test
	public void testValidXmlConfig4_fileResource() throws IOException, ClassNotFoundException, JAXBException {
		final SpringApplicationServiceConfig config = new SpringApplicationServiceConfig("file:src/test/resources/spring-application-service4.xml");
		log.info(config.toString());

		Assert.assertEquals(config.getConfigurationPackages().length, 1);
		Assert.assertTrue(ArrayUtils.contains(config.getConfigurationPackages(), ApplicationSpringConfig.class.getPackage()));

		Assert.assertEquals(config.getConfigurationClasses().length, 2);
		final Class<?>[] configClasses = config.getConfigurationClasses();
		Assert.assertTrue(ArrayUtils.contains(configClasses, ApplicationSpringConfig.class));
		Assert.assertTrue(ArrayUtils.contains(configClasses, WebApplicationSpringConfig.class));

		Assert.assertNotNull(config.getJvmSystemProperties());
		Assert.assertEquals(config.getJvmSystemProperties().get("app.env"), "DEV");
		Assert.assertEquals(config.getJvmSystemProperties().get("app.profile"), "PROFILE_A");
	}

	@Test
	public void testValidXmlConfig4_ClasspathResource() throws IOException, ClassNotFoundException, JAXBException {
		final SpringApplicationServiceConfig config = new SpringApplicationServiceConfig("classpath:spring-application-service4.xml");
		log.info(config.toString());

		Assert.assertEquals(config.getConfigurationPackages().length, 1);
		Assert.assertTrue(ArrayUtils.contains(config.getConfigurationPackages(), ApplicationSpringConfig.class.getPackage()));

		Assert.assertEquals(config.getConfigurationClasses().length, 2);
		final Class<?>[] configClasses = config.getConfigurationClasses();
		Assert.assertTrue(ArrayUtils.contains(configClasses, ApplicationSpringConfig.class));
		Assert.assertTrue(ArrayUtils.contains(configClasses, WebApplicationSpringConfig.class));

		Assert.assertNotNull(config.getJvmSystemProperties());
		Assert.assertEquals(config.getJvmSystemProperties().get("app.env"), "DEV");
		Assert.assertEquals(config.getJvmSystemProperties().get("app.profile"), "PROFILE_A");
	}

	@SuppressWarnings("unused")
	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testInvalidXmlConfig() throws IOException, ClassNotFoundException, JAXBException {
		try (final InputStream is = getClass().getResourceAsStream("/spring-application-service-invalidConfigClass.xml")) {
			new SpringApplicationServiceConfig(is);
		}
	}

}
