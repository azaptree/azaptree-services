package com.azaptree.services.security.commands;

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

import javax.xml.namespace.QName;

import com.azaptree.services.command.http.WebCommandContext;
import com.azaptree.services.command.http.WebRequestCommand;
import com.google.common.base.Optional;

public class CreateSubject extends WebRequestCommand<Object, Object> {

	@Override
	protected boolean executeCommand(WebCommandContext<Object, Object> ctx) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<QName> getRequestXmlElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<QName> getResponseXmlElement() {
		// TODO Auto-generated method stub
		return null;
	}

}
