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

public enum RequestMessageHeaders {
	/**
	 * Application specific request message id that is provided by the client.
	 */
	MSG_ID("AZAPTREE_MSG_ID"),
	/**
	 * epoch timestamp - the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC
	 */
	CREATED_TIMESTAMP("AZAPTREE_REQ_TIMESTAMP");

	public final String header;

	private RequestMessageHeaders(final String header) {
		this.header = header;
	}
}
