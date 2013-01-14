package test.com.azaptree.services.commons.xml.jaxb;

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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.xml.bind.JAXBContext;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.command.messages.ObjectFactory;
import com.azaptree.services.commons.xml.jaxb.JAXBContextCache;

public class JAXBContextCacheTest {

	@Test
	public void testGet() {
		final String contextPath = ObjectFactory.class.getPackage().getName();

		final JAXBContext ctx = JAXBContextCache.get(contextPath);
		Assert.assertNotNull(ctx);
	}
}
