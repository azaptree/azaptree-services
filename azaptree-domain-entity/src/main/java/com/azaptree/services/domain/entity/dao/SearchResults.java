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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;

public class SearchResults<T> {

	private Page page;

	private int returnCount;

	private int totalCount;

	private List<T> data;

	public SearchResults() {
	}

	public SearchResults(final Page page, final int returnCount, final int totalCount, final List<T> data) {
		super();
		this.page = page;
		this.returnCount = returnCount;
		this.totalCount = totalCount;
		this.data = data;
	}

	public List<T> getData() {
		return data;
	}

	public Page getPage() {
		return page;
	}

	public int getReturnCount() {
		return returnCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setData(final List<T> data) {
		this.data = data;
	}

	public void setPage(final Page page) {
		this.page = page;
	}

	public void setReturnCount(final int returnCount) {
		this.returnCount = returnCount;
	}

	public void setTotalCount(final int totalCount) {
		this.totalCount = totalCount;
	}

}
