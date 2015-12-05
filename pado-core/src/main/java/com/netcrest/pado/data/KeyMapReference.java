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
package com.netcrest.pado.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.data.jsonlite.internal.JsonLiteSerializer;

public class KeyMapReference<V> implements Externalizable
{
	private static final long serialVersionUID = 1L;

	private KeyMap<V> keyMap;
	private Map<String, Object> referenceMap;

	public KeyMapReference()
	{
	}

	public KeyMapReference(KeyMap<V> keyMap, Map<String, Object> referenceMap)
	{
		init(keyMap, referenceMap);
	}

	protected void init(KeyMap<V> keyMap, Map<String, Object> referenceMap)
	{
		this.keyMap = keyMap;
		this.referenceMap = referenceMap;
	}

	@SuppressWarnings("unchecked")
	private void stitch(KeyMapReference<V> keyMapReference)
	{
		if (keyMapReference.referenceMap == null || keyMapReference.keyMap == null) {
			return;
		}
		Set<Map.Entry<String, Object>> set = keyMapReference.referenceMap.entrySet();
		for (Map.Entry<String, Object> entry : set) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof KeyMapReference) {
				KeyMapReference<V> kmr = (KeyMapReference<V>) value;
				keyMapReference.keyMap.putReference(key, kmr.keyMap, Thread.currentThread().getId());
				stitch(kmr);
			} else {
				keyMapReference.keyMap.putReference(key, value, Thread.currentThread().getId());
			}
		}
	}

	/**
	 * Stitches the references found in the reference map top-down.
	 */
	public void stitch()
	{
		stitch(this);
	}

	public KeyMap<V> getKeyMap()
	{
		return keyMap;
	}

	public void setKeyMap(KeyMap<V> keyMap)
	{
		this.keyMap = keyMap;
	}

	public Map<String, Object> getReferenceMap()
	{
		return referenceMap;
	}

	public void setReferenceMap(Map<String, Object> referenceMap)
	{
		this.referenceMap = referenceMap;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void writeExternal(ObjectOutput output) throws IOException
	{
		JsonLiteSerializer.writeJsonLite((JsonLite) keyMap, output);
		JsonLiteSerializer.writeHashMap((HashMap<String, Object>) referenceMap, output);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException
	{
		keyMap = JsonLiteSerializer.readJsonLite(input);
		referenceMap = (HashMap<String, Object>) JsonLiteSerializer.readHashMap(input);
	}
}
