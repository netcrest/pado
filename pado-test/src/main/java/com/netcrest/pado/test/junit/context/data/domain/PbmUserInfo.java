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
package com.netcrest.pado.test.junit.context.data.domain;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class PbmUserInfo implements IJsonLiteWrapper<Object> {

	private transient JsonLite<Object> jl;

	public PbmUserInfo() {
		this.jl = new JsonLite<Object>(
				com.netcrest.pado.test.junit.context.data.PbmUserInfoKey
						.getKeyType());
	}

	public PbmUserInfo(JsonLite<Object> jl) {
		this.jl = jl;
	}

	public void setFUserId(String FUserId) {
		this.jl.put(
				com.netcrest.pado.test.junit.context.data.PbmUserInfoKey.KFUserId,
				FUserId);
	}

	public String getFUserId() {
		return (String) this.jl
				.get(com.netcrest.pado.test.junit.context.data.PbmUserInfoKey.KFUserId);
	}

	public void setFLocation(String FLocation) {
		this.jl.put(
				com.netcrest.pado.test.junit.context.data.PbmUserInfoKey.KFLocation,
				FLocation);
	}

	public String getFLocation() {
		return (String) this.jl
				.get(com.netcrest.pado.test.junit.context.data.PbmUserInfoKey.KFLocation);
	}

	public void setFOrg(String FOrg) {
		this.jl.put(
				com.netcrest.pado.test.junit.context.data.PbmUserInfoKey.KFOrg,
				FOrg);
	}

	public String getFOrg() {
		return (String) this.jl
				.get(com.netcrest.pado.test.junit.context.data.PbmUserInfoKey.KFOrg);
	}

	public void setFIsOnBehalf(boolean FIsOnBehalf) {
		this.jl.put(
				com.netcrest.pado.test.junit.context.data.PbmUserInfoKey.KFIsOnBehalf,
				FIsOnBehalf);
	}

	public boolean getFIsOnBehalf() {
		return (Boolean) this.jl
				.get(com.netcrest.pado.test.junit.context.data.PbmUserInfoKey.KFIsOnBehalf);
	}

	public JsonLite<Object> toJsonLite() {
		return this.jl;
	}

	public void fromJsonLite(JsonLite<Object> jl) {
		this.jl = jl;
	}
}