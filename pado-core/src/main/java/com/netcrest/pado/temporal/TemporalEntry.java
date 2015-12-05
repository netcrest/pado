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
package com.netcrest.pado.temporal;

import java.io.Serializable;
import java.util.Map;

/**
 * TemporalEntry contains a temporal key and data pair.
 * 
 * @author dpark
 * 
 * @param <K>
 *            Temporal key object
 * @param <V>
 *            Temporal data object
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class TemporalEntry<K, V> implements Comparable<TemporalEntry<K, V>>, Serializable
{
	private static final long serialVersionUID = 1L;

	protected ITemporalKey<K> tkey;
	protected ITemporalData<K> data;

	/**
	 * Constructs an empty TemporalEntry object.
	 */
	public TemporalEntry()
	{
	}

	/**
	 * Constructs a TemporalEntry object with the specified temporal key and
	 * data pair.
	 * 
	 * @param tkey
	 * @param data
	 */
	public TemporalEntry(ITemporalKey<K> tkey, ITemporalData<K> data)
	{
		this.tkey = tkey;
		this.data = data;
	}

	/**
	 * Returns the temporal key used in the GemFire region.
	 */
	public ITemporalKey<K> getTemporalKey()
	{
		return tkey;
	}

	/**
	 * Returns the temporal data. It can be null for invalidate and remove
	 * operations. Note that the temporal data object is the object directly
	 * mapped in the GemFire region. It could be a wrapper object if
	 * non-temporal object is put in the region. For the actual data object
	 * invoke {@link #getValue()}.
	 */
	public ITemporalData<K> getTemporalData()
	{
		return data;
	}

	/**
	 * Returns the actual value object. The returned object is the same object
	 * returned by {@link #getTemporalData()} only if the object's class is not
	 * a wrapper class.
	 */
	public V getValue()
	{
		if (data instanceof TemporalData) {
			return (V) ((TemporalData) data).getValue();
		} else {
			return (V) data;
		}
	}
	
	/**
	 * Returns a map of all attachments.
	 */
	public  Map<String, AttachmentSet<K>> getAttachments()
	{
		return data.__getTemporalValue().getAttachmentMap();
	}

	/**
	 * Returns a string representation of this object.
	 */
	public String toString()
	{
		return "key=" + getTemporalKey() + ", value=" + getValue();
	}

	/**
	 * Compares the specified temporal entry with this object.
	 */
	@Override
	public int compareTo(TemporalEntry anotherTemporalEntity)
	{
		if (anotherTemporalEntity == null) {
			return 1;
		}
		if (this == anotherTemporalEntity) {
			return 0;
		}
		if (tkey instanceof Comparable) {
			return ((Comparable) tkey).compareTo(anotherTemporalEntity.tkey);
		}
		if (tkey.getIdentityKey() instanceof Comparable) {
			return ((Comparable) tkey.getIdentityKey()).compareTo(anotherTemporalEntity.tkey.getIdentityKey());
		}
		return tkey.getIdentityKey().toString().compareTo(anotherTemporalEntity.tkey.getIdentityKey().toString());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((tkey == null) ? 0 : tkey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemporalEntry other = (TemporalEntry) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (tkey == null) {
			if (other.tkey != null)
				return false;
		} else if (!tkey.equals(other.tkey))
			return false;
		return true;
	}

}