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
package com.netcrest.pado.data.jsonlite;

import java.io.StringWriter;
import java.util.Map;

public class JsonLiteArray
{
	private Object array[];

	public JsonLiteArray(String jsonArrayString)
	{
		if (jsonArrayString == null) {
			return;
		}
		JsonLiteTokenizer tokenizer = new JsonLiteTokenizer(jsonArrayString,
				(Map<String, JsonLiteSchemaManager.KeyInfo>) null);
		array = tokenizer.getArray();
	}
	
	public JsonLiteArray(Object...args)
	{
		this.array = args;
	}

	public Object[] getArray()
	{
		return array;
	}

	public String toString()
	{
		return toString(0, false, false);
	}
	
	@SuppressWarnings("rawtypes")
	public String toString(int indentFactor, boolean isReference, boolean isHeader) throws JsonLiteException
	{
		JsonLiteStringWriter jlsw = new JsonLiteStringWriter();
		StringWriter w = new StringWriter();
		return jlsw.writeArray(w, indentFactor, 0, array, isReference, isHeader).toString();
	}
}
