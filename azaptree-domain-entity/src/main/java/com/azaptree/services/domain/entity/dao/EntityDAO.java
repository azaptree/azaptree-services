package com.azaptree.services.domain.entity.dao;

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

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.azaptree.services.domain.entity.Entity;

@Repository
public interface EntityDAO<T extends Entity> {

	T create(T entity);

	/**
	 * 
	 * @param id
	 * @return false if there was no entity found with specified id
	 */
	boolean delete(UUID id);

	SearchResults<T> findAll(Page page, SortField... sort);

	SearchResults<T> findByExample(T example, Page page, SortField... sort);

	T findById(UUID id);

	void update(T entity);
}
