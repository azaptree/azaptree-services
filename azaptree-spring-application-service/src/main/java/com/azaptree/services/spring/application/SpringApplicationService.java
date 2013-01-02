package com.azaptree.services.spring.application;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;

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

public class SpringApplicationService {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		validate(args);
	}

	private static void validate(String[] args) {
		if (args.length == 0) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			pw.println();
			pw.println(StringUtils.repeat("=", 120));
			pw.println("Usage: java com.azaptree.services.spring.application.SpringApplicationService <config.xml>");
			pw.println();
			pw.println("       where <config.xml> = classpath resource XML file that must validate against spring-application-service.xsd");
			pw.println(StringUtils.repeat("=", 120));
			throw new IllegalArgumentException(sw.toString());
		}
	}

}
