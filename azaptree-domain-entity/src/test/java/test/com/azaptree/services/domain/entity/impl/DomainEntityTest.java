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

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.domain.entity.impl.DomainEntity;

public class DomainEntityTest {
	public static class Address {
		private String street;
		private String city;
		private String state;

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

	public static class User extends DomainEntity {
		private String fname;
		private String lname;

		private int age;

		private Date lastUpdatedOn;

		private String[] notes;

		private Address address;

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
			if (lastUpdatedOn == null) {
				if (other.lastUpdatedOn != null) {
					return false;
				}
			} else if (!lastUpdatedOn.equals(other.lastUpdatedOn)) {
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

		public Date getLastUpdatedOn() {
			return lastUpdatedOn;
		}

		public String getLname() {
			return lname;
		}

		public String[] getNotes() {
			return notes;
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

		public void setLastUpdatedOn(final Date lastUpdatedOn) {
			this.lastUpdatedOn = lastUpdatedOn;
		}

		public void setLname(final String lname) {
			this.lname = lname;
		}

		public void setNotes(final String[] notes) {
			this.notes = notes;
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	public DomainEntityTest() {
	}

	@Test
	public void test_JsonObject() {
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
		user.setLastUpdatedOn(new Date());
		user.setNotes(new String[] { "NOTE 1", "NOTE 2" });

		log.info("user : {}", user);

		final String json = user.toJson();
		log.info("user json: {}", json);

		final User user2 = new User();
		user2.init(json);

		Assert.assertEquals(user2.hashCode(), user.hashCode());

		Assert.assertEquals(user2, user);
	}

}
