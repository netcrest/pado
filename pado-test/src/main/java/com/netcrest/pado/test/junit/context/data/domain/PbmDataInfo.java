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

public class PbmDataInfo implements IJsonLiteWrapper<Object> {

	private transient JsonLite<Object> jl;

	public PbmDataInfo() {
		this.jl = new JsonLite<Object>(
				com.netcrest.pado.test.junit.context.data.PbmDataInfoKey
						.getKeyType());
	}

	public PbmDataInfo(JsonLite<Object> jl) {
		this.jl = jl;
	}

	public void setFMedication(byte FMedication) {
		this.jl.put(
				com.netcrest.pado.test.junit.context.data.PbmDataInfoKey.KFMedication,
				FMedication);
	}

	public byte getFMedication() {
		return (Byte) this.jl
				.get(com.netcrest.pado.test.junit.context.data.PbmDataInfoKey.KFMedication);
	}

	public void setFBusinessConfidentialType(byte FBusinessConfidentialType) {
		this.jl.put(
				com.netcrest.pado.test.junit.context.data.PbmDataInfoKey.KFBusinessConfidentialType,
				FBusinessConfidentialType);
	}

	public byte getFBusinessConfidentialType() {
		return (Byte) this.jl
				.get(com.netcrest.pado.test.junit.context.data.PbmDataInfoKey.KFBusinessConfidentialType);
	}

	public void setFRxNumber(int FRxNumber) {
		this.jl.put(
				com.netcrest.pado.test.junit.context.data.PbmDataInfoKey.KFRxNumber,
				FRxNumber);
	}

	public int getFRxNumber() {
		return (Integer) this.jl
				.get(com.netcrest.pado.test.junit.context.data.PbmDataInfoKey.KFRxNumber);
	}

	public void setFOrderNumber(int FOrderNumber) {
		this.jl.put(
				com.netcrest.pado.test.junit.context.data.PbmDataInfoKey.KFOrderNumber,
				FOrderNumber);
	}

	public int getFOrderNumber() {
		return (Integer) this.jl
				.get(com.netcrest.pado.test.junit.context.data.PbmDataInfoKey.KFOrderNumber);
	}

	public JsonLite<Object> toJsonLite() {
		return this.jl;
	}

	public void fromJsonLite(JsonLite<Object> jl) {
		this.jl = jl;
	}
}