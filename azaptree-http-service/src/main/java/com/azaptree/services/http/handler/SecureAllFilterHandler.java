package com.azaptree.services.http.handler;

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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Used to ensure that request is on a secure channel, such as HTTPS.
 * 
 * Add this as the first Handler in a HandlerList.
 * 
 * @author alfio
 * 
 */
public class SecureAllFilterHandler extends AbstractHandler {

	@Override
	public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException, ServletException {
		if (!baseRequest.isSecure()) {
			baseRequest.setHandled(true);
			response.sendError(HttpStatus.FORBIDDEN_403, "Request must be made on a secure channel");
		}
	}

}
