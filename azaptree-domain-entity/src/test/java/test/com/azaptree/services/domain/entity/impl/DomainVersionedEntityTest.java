package test.com.azaptree.services.domain.entity.impl;

/*
 * #%L
 * AZAPTREE-DOMAIN-ENTITY
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.domain.entity.EntityAuditLogRecord.AuditAction;
import com.azaptree.services.domain.entity.impl.DomainEntityAuditLogRecord;
import com.azaptree.services.domain.entity.impl.DomainVersionedEntity;
import com.azaptree.services.json.JsonUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

public class DomainVersionedEntityTest {
	public static class Address {
		private String street;
		private String city;
		private String state;

		public Address() {
		};

		public Address(final JsonParser parser) throws IOException {
			JsonToken tk;
			while ((tk = parser.nextToken()) != null) {
				switch (tk) {
				case FIELD_NAME:
					final String fieldName = parser.getCurrentName();
					if (fieldName.equals("street")) {
						this.street = parser.nextTextValue();
					} else if (fieldName.equals("city")) {
						this.city = parser.nextTextValue();
					} else if (fieldName.equals("state")) {
						this.state = parser.nextTextValue();
					}
				}
			}
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
			final Address other = (Address) obj;
			if (city == null) {
				if (other.city != null) {
					return false;
				}
			} else if (!city.equals(other.city)) {
				return false;
			}
			if (state == null) {
				if (other.state != null) {
					return false;
				}
			} else if (!state.equals(other.state)) {
				return false;
			}
			if (street == null) {
				if (other.street != null) {
					return false;
				}
			} else if (!street.equals(other.street)) {
				return false;
			}
			return true;
		}

		public String getCity() {
			return city;
		}

		public String getState() {
			return state;
		}

		public String getStreet() {
			return street;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (city == null ? 0 : city.hashCode());
			result = prime * result + (state == null ? 0 : state.hashCode());
			result = prime * result + (street == null ? 0 : street.hashCode());
			return result;
		}

		public void setCity(final String city) {
			this.city = city;
		}

		public void setState(final String state) {
			this.state = state;
		}

		public void setStreet(final String street) {
			this.street = street;
		}
	}

	public static class User extends DomainVersionedEntity {
		private String fname;
		private String lname;

		private int age;

		private String[] notes;

		private Address address;

		public User() {
			super();
		}

		public User(final InputStream json) throws IOException {
			super(json);
		}

		public User(final String json) {
			super(json);
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

			if (!super.equals(obj)) {
				return false;
			}

			final User other = (User) obj;
			if (address == null) {
				if (other.address != null) {
					return false;
				}
			} else if (!address.equals(other.address)) {
				return false;
			}
			if (age != other.age) {
				return false;
			}
			if (fname == null) {
				if (other.fname != null) {
					return false;
				}
			} else if (!fname.equals(other.fname)) {
				return false;
			}
			if (lname == null) {
				if (other.lname != null) {
					return false;
				}
			} else if (!lname.equals(other.lname)) {
				return false;
			}
			if (!Arrays.equals(notes, other.notes)) {
				return false;
			}
			return true;
		}

		public Address getAddress() {
			return address;
		}

		public int getAge() {
			return age;
		}

		public String getFname() {
			return fname;
		}

		public String getLname() {
			return lname;
		}

		public String[] getNotes() {
			return notes;
		}

		@Override
		protected void init(final JsonParser parser) throws IOException {
			JsonToken tk = parser.getCurrentToken();
			do {
				switch (tk) {
				case FIELD_NAME:
					final String fieldName = parser.getCurrentName();
					if (fieldName.equals("fname")) {
						setFname(parser.nextTextValue());
					} else if (fieldName.equals("lname")) {
						setLname(parser.nextTextValue());
					} else if (fieldName.equals("age")) {
						setAge(parser.nextIntValue(0));
					} else if (fieldName.equals("notes")) {
						NOTES: while ((tk = parser.nextToken()) != null) {
							switch (tk) {
							case START_ARRAY:
								final java.util.List<String> notes = new ArrayList<>();
								while ((tk = parser.nextToken()) != null) {
									switch (tk) {
									case VALUE_STRING:
										notes.add(parser.getValueAsString());
										break;
									case END_ARRAY:
										setNotes(notes.toArray(new String[notes.size()]));
										break NOTES;
									}
								}
							}
						}
					} else if (fieldName.equals("address")) {
						setAddress(new Address(parser));
					}
				}
			} while ((tk = parser.nextToken()) != null);
		}

		public void setAddress(final Address address) {
			this.address = address;
		}

		public void setAge(final int age) {
			this.age = age;
		}

		public void setFname(final String fname) {
			this.fname = fname;
		}

		public void setLname(final String lname) {
			this.lname = lname;
		}

		public void setNotes(final String[] notes) {
			this.notes = notes;
		}
	}

	static {
		JsonUtils.register(new GuavaModule());
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	public DomainVersionedEntityTest() {
	}

	@Test
	public void test_JsonObject() throws IOException {
		final Address address = new Address();
		address.setCity("Rochester");
		address.setStreet("10 Main St");
		address.setState("NY");

		final User user = new User();
		user.setAddress(address);
		user.setEntityId(UUID.randomUUID());
		user.setFname("Alfio");
		user.setLname("Zappala");
		user.setAge(39);
		user.setNotes(new String[] { "NOTE 1", "NOTE 2" });
		user.created(UUID.randomUUID());

		log.info("user : {}", user);

		final String json = user.toJson();
		log.info("user json: {}", json);

		final User user2 = new User();
		user2.init(json);

		log.info("user2 : {}", user2);

		Assert.assertEquals(user2.hashCode(), user.hashCode());
		Assert.assertEquals(user2, user);

		address.setStreet("315 Luddington");
		user.updated(UUID.randomUUID());

		final User user3 = new User(user.toJson());
		Assert.assertEquals(user3, user);

		final ByteArrayInputStream bis = new ByteArrayInputStream(user.toJson().getBytes("UTF-8"));
		final User user4 = new User(bis);
		Assert.assertEquals(user4, user);

		final DomainEntityAuditLogRecord auditLogRecord = new DomainEntityAuditLogRecord(user4, AuditAction.UPDATED);
		log.info("auditLogRecord: {}", auditLogRecord);
		Assert.assertEquals(auditLogRecord.getAuditAction(), auditLogRecord.getAuditAction());
		Assert.assertEquals(auditLogRecord.getAuditedEntityId(), user4.getEntityId());
		Assert.assertEquals(auditLogRecord.getEntityType(), User.class.getName());
		Assert.assertEquals(new User(auditLogRecord.getEntityJson()), user4);
		Assert.assertTrue(auditLogRecord.getEntityAuditlogRecordCreatedOn() > 0);
		Assert.assertNotNull(auditLogRecord.getEntityId());

		final DomainEntityAuditLogRecord auditLogRecord2 = new DomainEntityAuditLogRecord();
		auditLogRecord2.init(auditLogRecord.toJson());
		Assert.assertEquals(auditLogRecord2.getAuditAction(), auditLogRecord.getAuditAction());
		Assert.assertEquals(auditLogRecord2.getAuditedEntityId(), user4.getEntityId());
		Assert.assertEquals(auditLogRecord2.getEntityType(), User.class.getName());
		Assert.assertEquals(new User(auditLogRecord2.getEntityJson()), user4);
		Assert.assertTrue(auditLogRecord2.getEntityAuditlogRecordCreatedOn() > 0);
		Assert.assertNotNull(auditLogRecord2.getEntityId());
	}
}
