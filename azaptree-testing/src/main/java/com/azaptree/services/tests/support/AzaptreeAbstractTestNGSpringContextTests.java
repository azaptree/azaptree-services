package com.azaptree.services.tests.support;

/*
 * #%L
 * AZAPTREE SECURITY SERVICE
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

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;

/**
 * Extended Spring's AbstractTestNGSpringContextTests because the ApplicationContext was not being closed after each test class was run.
 * 
 * 
 * @author alfio
 * 
 */
public class AzaptreeAbstractTestNGSpringContextTests extends AbstractTestNGSpringContextTests {
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Ensures that the application context is closed after the test class is done
	 */
	@Override
	@AfterClass(alwaysRun = true)
	protected void springTestContextAfterTestClass() throws Exception {
		super.springTestContextAfterTestClass();

		try {
			MethodUtils.invokeExactMethod(applicationContext, "close");
			log.info("closed the application context");
		} catch (final Exception e) {
			log.error("Failed to close application context", e);
		}
	}

}
