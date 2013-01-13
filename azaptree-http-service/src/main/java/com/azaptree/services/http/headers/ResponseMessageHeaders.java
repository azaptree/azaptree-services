package com.azaptree.services.http.headers;

/*
 * #%L
 * AZAPTREE-HTTP-SERVICE
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

public enum ResponseMessageHeaders {
	/**
	 * Application specific response status code
	 */
	STATUS_CODE("AZAPTREE_STATUS_CODE"),
	/**
	 * Application specific response status message, which is more human readable than simply the status code
	 */
	STATUS_MSG("AZAPTREE_STATUS_MSG"),
	/**
	 * epoch timestamp - the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC
	 */
	RECEIVED_TIMESTAMP("AZAPTREE_RECEIVED_TIMESTAMP"),
	/**
	 * The number of milliseconds it took to process the request on the server side. Thus, this excludes network latency.
	 */
	EXECUTION_TIME_MILLIS("AZAPTREE_EXEC_TIME");

	public final String header;

	private ResponseMessageHeaders(final String header) {
		this.header = header;
	}
}
