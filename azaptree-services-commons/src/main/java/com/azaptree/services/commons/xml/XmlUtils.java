package com.azaptree.services.commons.xml;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public abstract class XmlUtils {

	public static void prettyFormatXml(final InputStream xml, final OutputStream os, final int indent) {
		try {
			final Source xmlInput = new StreamSource(xml);
			final StreamResult xmlOutput = new StreamResult(os);
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			final Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String prettyFormatXml(final String xml, final int indent) {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream((int) (xml.length() * 1.5));
		try {
			prettyFormatXml(new ByteArrayInputStream(xml.getBytes("UTF-8")), bos, indent);
			return bos.toString();
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

}
