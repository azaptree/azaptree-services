package test.com.azaptree.services.json;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.azaptree.services.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Optional;

public class JsonUtilsTest {
	@JsonIgnore
	private final Logger log = LoggerFactory.getLogger(getClass());

	private String stringProperty;

	private long longProperty;

	private List<String> stringList;

	private Optional<Boolean> optionalBoolean = Optional.absent();

	static {
		final Module[] modules = { new GuavaModule(), new JodaModule() };
		for (final Module module : modules) {
			JsonUtils.register(module);
		}
	}

	@BeforeTest
	public void beforeTest() {
		stringProperty = "ABC";
		longProperty = 100l;
		stringList = Arrays.asList("Alfio", "Antonio", "Zappala");
		optionalBoolean = Optional.<Boolean> of(true);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final JsonUtilsTest other = (JsonUtilsTest) obj;
		if (longProperty != other.longProperty) {
			return false;
		}
		if (optionalBoolean == null) {
			if (other.optionalBoolean != null) {
				return false;
			}
		} else if (other.optionalBoolean == null) {
			if (optionalBoolean != null) {
				return false;
			}
		} else if (other.optionalBoolean.isPresent() != other.optionalBoolean.isPresent()) {
			return false;
		} else if (optionalBoolean.isPresent() && other.optionalBoolean.isPresent() && !optionalBoolean.get().equals(other.optionalBoolean.get())) {
			return false;
		}
		if (stringList == null) {
			if (other.stringList != null) {
				return false;
			}
		} else if (!stringList.equals(other.stringList)) {
			return false;
		}
		if (stringProperty == null) {
			if (other.stringProperty != null) {
				return false;
			}
		} else if (!stringProperty.equals(other.stringProperty)) {
			return false;
		}
		return true;
	}

	public long getLongProperty() {
		return longProperty;
	}

	public Optional<Boolean> getOptionalBoolean() {
		return optionalBoolean;
	}

	public List<String> getStringList() {
		return stringList;
	}

	public String getStringProperty() {
		return stringProperty;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (longProperty ^ longProperty >>> 32);
		result = prime * result + (optionalBoolean == null ? 0 : optionalBoolean.hashCode());
		result = prime * result + (stringList == null ? 0 : stringList.hashCode());
		result = prime * result + (stringProperty == null ? 0 : stringProperty.hashCode());
		return result;
	}

	public void setLongProperty(final long longProperty) {
		this.longProperty = longProperty;
	}

	public void setOptionalBoolean(final Boolean optionalBoolean) {
		if (optionalBoolean == null) {
			this.optionalBoolean = Optional.absent();
		} else {
			this.optionalBoolean = Optional.of(optionalBoolean);
		}
	}

	public void setStringList(final List<String> stringList) {
		this.stringList = stringList;
	}

	public void setStringProperty(final String stringProperty) {
		this.stringProperty = stringProperty;
	}

	@Test
	public void test_convert_TypeReference() throws IOException {
		final String json = JsonUtils.serializeNormalized(this);
		final ByteArrayInputStream bis = new ByteArrayInputStream(json.getBytes("UTF-8"));
		final Map<String, Object> test2 = JsonUtils.parse(bis);
		Assert.assertEquals(JsonUtils.convert(test2, new TypeReference<JsonUtilsTest>() {
			// intentional empty block
		}), this);
	}

	@Test
	public void test_parse_InputStream() throws IOException {
		final String json = JsonUtils.serializeNormalized(this);
		final ByteArrayInputStream bis = new ByteArrayInputStream(json.getBytes("UTF-8"));
		final Map<String, Object> test2 = JsonUtils.parse(bis);
		Assert.assertEquals(JsonUtils.convert(test2, JsonUtilsTest.class), this);
	}

	@Test
	public void test_parse_InputStream_TypeReference() throws IOException {
		final String json = JsonUtils.serializeNormalized(this);
		final ByteArrayInputStream bis = new ByteArrayInputStream(json.getBytes("UTF-8"));
		final JsonUtilsTest test2 = JsonUtils.parse(bis, new TypeReference<JsonUtilsTest>() {
			// intentional empty block
		});
		Assert.assertEquals(test2, this);
	}

	@Test
	public void test_parse_JSONParser() throws IOException {
		final String json = JsonUtils.serializeNormalized(this);
		final ByteArrayInputStream bis = new ByteArrayInputStream(json.getBytes("UTF-8"));
		try (final JsonParser parser = JsonUtils.createJsonParser(bis)) {
			final JsonUtilsTest test2 = JsonUtils.parse(parser, JsonUtilsTest.class);
			Assert.assertEquals(test2, this);
		}
	}

	@Test
	public void test_serialize() throws JsonGenerationException, IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final JsonGenerator generator = JsonUtils.createJsonGenerator(bos);
		generator.writeStartObject();
		generator.writeStringField("stringField", "stringFieldValue");
		generator.writeNumberField("numberField", 5);
		generator.writeEndObject();
		generator.close();

		final Map<String, Object> test = JsonUtils.parse(new ByteArrayInputStream(bos.toByteArray()));
		Assert.assertEquals(test.get("stringField"), "stringFieldValue");
		Assert.assertEquals(test.get("numberField"), 5);
	}

	@Test
	public void test_serialize_JSONEncoding() throws JsonGenerationException, IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final JsonGenerator generator = JsonUtils.createJsonGenerator(bos, JsonEncoding.UTF8);
		generator.writeStartObject();
		generator.writeStringField("stringField", "stringFieldValue");
		generator.writeNumberField("numberField", 5);
		generator.writeEndObject();
		generator.close();

		final Map<String, Object> test = JsonUtils.parse(new ByteArrayInputStream(bos.toByteArray()));
		Assert.assertEquals(test.get("stringField"), "stringFieldValue");
		Assert.assertEquals(test.get("numberField"), 5);
	}

	@Test
	public void test_serialize_JSONGenerator_Object() throws JsonGenerationException, IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (final JsonGenerator generator = JsonUtils.createJsonGenerator(bos)) {
			JsonUtils.serialize(generator, this);
		}

		final JsonUtilsTest test2 = JsonUtils.parse(new ByteArrayInputStream(bos.toByteArray()), JsonUtilsTest.class);
		Assert.assertEquals(test2, this);
	}

	@Test
	public void test_serializeNormalized() {
		final String json = JsonUtils.serializeNormalized(this);
		final JsonUtilsTest test2 = JsonUtils.parse(json, JsonUtilsTest.class);
		Assert.assertEquals(test2, this);
	}

	@Test
	public void testConvert() {
		final String json = JsonUtils.serializePrettyPrint(this);
		log.info("testConvert():\n{}", json);
		final Map<String, Object> propertyMap = JsonUtils.parse(json, JsonUtils.TYPE_REF_PROPERTY_MAP);

		final JsonUtilsTest test2 = JsonUtils.convert(propertyMap, JsonUtilsTest.class);
		final String json2 = JsonUtils.serializePrettyPrint(test2);
		Assert.assertEquals(test2, this);
		Assert.assertEquals(json2, json);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testConvertInterfaceFails() {
		final Map<String, Object> pojoMap = new HashMap<>();
		pojoMap.put("value", "value-1");
		JsonUtils.convert(pojoMap, IPojo.class);
	}

	@Test
	public void testConvertNullFields() {
		setOptionalBoolean(null);
		setStringList(null);
		setStringProperty(null);
		final String json = JsonUtils.serializePrettyPrint(this);
		log.info("testConvertNullFields(): json\n{}", json);
		final Map<String, Object> propertyMap = JsonUtils.parse(json, JsonUtils.TYPE_REF_PROPERTY_MAP);

		final JsonUtilsTest test2 = JsonUtils.convert(propertyMap, JsonUtilsTest.class);
		final String json2 = JsonUtils.serializePrettyPrint(test2);
		log.info("testConvertNullFields(): json2\n{}", json2);

		Assert.assertNull(test2.getStringList());
		Assert.assertNull(test2.getStringProperty());
	}

	@Test
	public void testSerializeExceptions() {
		final Exception e = new Exception("ERROR OCCURRED");
		final String json = JsonUtils.serializePrettyPrint(e);
		log.info("exception:\n{}", json);
		final Exception e2 = JsonUtils.parse(json, Exception.class);
		final String json2 = JsonUtils.serializePrettyPrint(e2);
		log.info("exception2:\n{}", json2);
		Assert.assertEquals(json2, json);
	}

	@Test
	public void testSerializeParse() {
		final String json = JsonUtils.serialize(this);
		log.info("testSerializeParse():\n{}", json);

		final JsonUtilsTest test2 = JsonUtils.parse(json, JsonUtilsTest.class);
		Assert.assertEquals(test2, this);
	}

	@Test
	public void testSerializeParseStreamBased() throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JsonUtils.serialize(bos, this);
		final String json = bos.toString();
		log.info("testSerializeParseStreamBased():\n{}", json);

		final JsonUtilsTest test2 = JsonUtils.parse(new ByteArrayInputStream(bos.toByteArray()), JsonUtilsTest.class);
		Assert.assertEquals(test2, this);
	}

	@Test
	public void testSerializePrettyPrintParse() {
		final String json = JsonUtils.serializePrettyPrint(this);
		log.info("testSerializePrettyPrintParse():\n{}", json);

		final JsonUtilsTest test2 = JsonUtils.parse(json, JsonUtilsTest.class);
		Assert.assertEquals(test2, this);
	}

	@Override
	public String toString() {
		return JsonUtils.serializePrettyPrint(this);
	}

	@Test
	public void test_serialized_normalized() throws IOException {
		final Map<String, Object> map1 = new LinkedHashMap<>();
		map1.put("A", "A");
		map1.put("a", "a");
		map1.put("B", "B");
		map1.put("b", "b");

		final Map<String, Object> map2 = new LinkedHashMap<>();
		map2.put("B", "B");
		map2.put("b", "b");
		map2.put("A", "A");
		map2.put("a", "a");

		final ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
		JsonUtils.serializeNormalized(bos1, map1);

		final ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		JsonUtils.serializeNormalized(bos2, map2);

		Assert.assertEquals(bos1.toString(), bos2.toString());

	}

}
