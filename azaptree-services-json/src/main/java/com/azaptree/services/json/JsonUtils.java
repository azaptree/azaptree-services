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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

/**
 * Guava module is registered by default because it is a core library that is used by azaptree-services.
 * 
 * @author alfio
 * 
 */
public abstract class JsonUtils {
	public static final TypeReference<Map<String, Object>> TYPE_REF_PROPERTY_MAP = new TypeReference<Map<String, Object>>() {
		// PROPERTY MAP TYPE
	};

	private final static JsonFactory jsonFactory = new JsonFactory();

	private final static ObjectMapper objectMapper = new ObjectMapper(JsonUtils.jsonFactory);
	private final static ObjectMapper prettyPrintobjectMapper = new ObjectMapper(JsonUtils.jsonFactory);
	private final static ObjectMapper normalizingObjectMapper = new ObjectMapper(JsonUtils.jsonFactory);

	static {
		prettyPrintobjectMapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
		prettyPrintobjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		prettyPrintobjectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
		prettyPrintobjectMapper.setSerializationInclusion(Include.NON_NULL);
		prettyPrintobjectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		normalizingObjectMapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
		normalizingObjectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
		normalizingObjectMapper.setSerializationInclusion(Include.NON_NULL);
		normalizingObjectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		register(new GuavaModule());
	}

	public static <T> T convert(final Map<String, Object> propertyMap, final Class<T> typeRef) {
		Assert.notNull(propertyMap, "propertyMap is required");
		Assert.notNull(typeRef, "typeRef is required");
		return objectMapper.convertValue(propertyMap, typeRef);
	}

	public static <T> T convert(final Map<String, Object> propertyMap, final TypeReference<T> typeRef) {
		Assert.notNull(propertyMap, "propertyMap is required");
		Assert.notNull(typeRef, "typeRef is required");
		return (T) objectMapper.convertValue(propertyMap, typeRef);
	}

	public static final JsonGenerator createJsonGenerator(final OutputStream os) throws IOException {
		return JsonUtils.jsonFactory.createJsonGenerator(os);
	}

	public static final JsonGenerator createJsonGenerator(final OutputStream os, final JsonEncoding encoding)
	        throws IOException {
		Assert.notNull(encoding, "encoding is required");
		return JsonUtils.jsonFactory.createJsonGenerator(os, encoding);
	}

	public static JsonParser createJsonParser(final InputStream is) throws IOException {
		return JsonUtils.jsonFactory.createJsonParser(is);
	}

	public static Map<String, Object> parse(final InputStream is) throws IOException {
		return JsonUtils.objectMapper.readValue(is, TYPE_REF_PROPERTY_MAP);
	}

	public static <T> T parse(final InputStream is, final Class<T> clazz) throws IOException {
		return JsonUtils.objectMapper.readValue(is, clazz);
	}

	public static <T> T parse(final InputStream is, final TypeReference<T> typeReference) throws IOException {
		return JsonUtils.objectMapper.readValue(is, typeReference);
	}

	public static <T> T parse(final JsonParser is, final Class<T> clazz) throws IOException {
		return JsonUtils.objectMapper.readValue(is, clazz);
	}

	public static <T> T parse(final String json, final Class<T> clazz) {
		Assert.hasText(json, "json is required");
		Assert.notNull(clazz, "clazz is required");
		try {
			return JsonUtils.parse(new ByteArrayInputStream(json.getBytes("UTF-8")), clazz);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T parse(final String json, final TypeReference<T> typeReference) {
		Assert.hasText(json, "json is required");
		Assert.notNull(typeReference, "typeReference is required");
		try {
			return JsonUtils.parse(new ByteArrayInputStream(json.getBytes("UTF-8")), typeReference);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void register(final Module module) {
		objectMapper.registerModule(module);
		prettyPrintobjectMapper.registerModule(module);
		normalizingObjectMapper.registerModule(module);
	}

	public static void serialize(final JsonGenerator os, final Object obj) throws IOException {
		JsonUtils.objectMapper.writeValue(os, obj);
	}

	public static String serialize(final Object obj) {
		Assert.notNull(obj, "obj is required");
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
		try {
			JsonUtils.objectMapper.writeValue(bos, obj);
			return bos.toString("UTF-8");
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void serialize(final OutputStream os, final Object obj) throws IOException {
		JsonUtils.objectMapper.writeValue(os, obj);
	}

	public static String serializeNormalized(final Object obj) {
		Assert.notNull(obj, "obj is required");
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
		try {
			JsonUtils.normalizingObjectMapper.writeValue(bos, obj);
			return bos.toString("UTF-8");
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void serializeNormalized(final OutputStream os, final Object obj) throws IOException {
		JsonUtils.normalizingObjectMapper.writeValue(os, obj);
	}

	public static String serializePrettyPrint(final Object obj) {
		Assert.notNull(obj, "obj is required");
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
		try {
			JsonUtils.prettyPrintobjectMapper.writeValue(bos, obj);
			return bos.toString("UTF-8");
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
