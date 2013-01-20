package com.azaptree.services.json;

/*
 * #%L
 * AZAPTREE-SERVICES-JSON
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
import java.io.OutputStream;

/**
 * 
 * Standardized interface for classes to implement that want to also be mapped to JSON.
 * 
 * @author alfio
 * 
 */
public interface JsonObject {
	void init(InputStream json) throws IOException;

	void init(String json);

	String toJson();

	void writeJson(OutputStream os) throws IOException;

}
