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
package com.netcrest.pado.data.jsonlite.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gemstone.gemfire.DataSerializer;
import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.gemfire.util.DataSerializerEx;

public class JsonLiteDataSerializer extends DataSerializer
{
	private static Class<?> wrapperClasses[];
	private static List<Class<?>> wrapperClassList = new ArrayList<Class<?>>();
	private static boolean isServer = Boolean.getBoolean("pado.server");

	public static synchronized void addJsonLiteWrapperClass(Class<?> wrapperClass)
	{
		if (isServer) {
			return;
		}

		wrapperClassList.add(wrapperClass);
		wrapperClasses = (Class<?>[]) wrapperClassList.toArray(new Class[wrapperClassList.size()]);

		// Must register here to ensure there is at least one class
		// supported. Otherwise, GemFire fails.
		if (wrapperClasses.length == 1) {
			DataSerializer.register(JsonLiteDataSerializer.class);
		}
	}

	public static synchronized void removeJsonLiteWrapper(Class<?> wrapperClass)
	{
		if (isServer) {
			return;
		}
		int size = wrapperClassList.size();
		wrapperClassList.remove(wrapperClass);
		if (wrapperClassList.size() != size) {
			wrapperClasses = (Class<?>[]) wrapperClassList.toArray(new Class[wrapperClassList.size()]);
		}
	}

	public static boolean isServer()
	{
		return isServer;
	}

	@Override
	public Class<?>[] getSupportedClasses()
	{
		if (isServer) {
			return new Class<?>[] { JsonLite.class };
		}
		return wrapperClasses;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean toData(Object o, DataOutput out) throws IOException
	{
		if (o instanceof IJsonLiteWrapper) {
			IJsonLiteWrapper wrapper = (IJsonLiteWrapper) o;
			DataSerializerEx.writeJsonLite(wrapper.toJsonLite(), out);
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object fromData(DataInput in) throws IOException, ClassNotFoundException
	{
		JsonLite jl = (JsonLite) DataSerializerEx.readJsonLite(in);
		return DataSerializerEx.checkDomainObject(jl);
	}

	@Override
	public int getId()
	{
		return 1010;
	}

}
