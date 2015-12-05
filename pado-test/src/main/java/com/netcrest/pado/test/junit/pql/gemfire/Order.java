/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.test.junit.pql.gemfire;

public class Order implements DomainObject {
	private static final long serialVersionUID = 1L;

	private String id;

	private String customerId;

	public Order() {
	}

	public Order(String id, String customerId) {
		this.id = id;
		this.customerId = customerId;
	}

	public String getId() {
		return this.id;
	}

	public String getCustomerId() {
		return this.customerId;
	}

	public int hashCode() {
		int result = 17;
		final int mult = 37;

		result = mult * result + this.id.hashCode();
		result = mult * result + this.customerId.hashCode();

		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || !(obj instanceof Order)) {
			return false;
		}

		Order that = (Order) obj;
		return this.id.equals(that.id) && this.customerId.equals(that.customerId);
	}

	public String toString() {
		return new StringBuilder().append("Order[").append("id=").append(this.id).append("; customerId=")
				.append(this.customerId).append("]").toString();
	}
}