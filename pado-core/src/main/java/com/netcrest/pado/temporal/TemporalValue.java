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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * TemporalValue contains the value and the list of attachment identity keys.
 * The value is always kept serialized in the server. It is deserialized when
 * getValue() is invoked.
 * 
 * @author dpark
 * 
 */
public abstract class TemporalValue<K> implements ITemporalValue<K>
{
	/**
	 * Checkpoint flag bit
	 */
	private final static byte BIT_CHECKPOINT = 0;
	
	/**
	 * Delta flag bit
	 */
	private final static byte BIT_DELTA = 1;

	/**
	 * Temporal key
	 */
	protected transient ITemporalKey<K> temporalKey;
	
	/**
	 * Temporal data
	 */
	protected ITemporalData<K> data;
	
	/**
	 * Serialized temporal data (ITemporal data)
	 */
	protected byte[] serializedData;
	
	/**
	 * Serialized attributes
	 */
	protected byte[] serializedAttributes;
	
	/**
	 * Serialized attachments
	 */
	protected byte[] serializedAttachments;
	
	/**
	 * Attachment set map that contains &lt;name, AttachmentSet&gt;
	 * pairs.
	 */
	protected Map<String, AttachmentSet<K>> attachmentMap;
	
	/**
	 * Linked list (chain) of all deltas
	 */
	protected LinkedList<byte[]> deltaList;

	/**
	 * Bit flag for storing boolean values
	 */
	protected byte flag;

	/**
	 * Constructs an empty TemporalValue object.
	 */
	public TemporalValue()
	{
	}

	/**
	 * Constructs a TemporalValue object with the specified key/data pair.
	 * 
	 * @param tkey
	 *            Temporal key
	 * @param data
	 *            Temporal data
	 */
	public TemporalValue(ITemporalKey<K> tkey, ITemporalData<K> data)
	{
		this.data = data;
		this.temporalKey = tkey;
	}

	/**
	 * Constructs a TemporalValue object with the specified data and copy from
	 * data.
	 * 
	 * @param data
	 *            Temporal data
	 * @param copyFromData
	 *            Temporal data object from which attributes are copied
	 */
	public TemporalValue(ITemporalData<K> data, ITemporalData<K> copyFromData)
	{
		this(data, copyFromData, null);
	}

	/**
	 * Constructs a new TemporalValue object by configuring with the specified
	 * data, copyFromData and deltaList objects.
	 * 
	 * @param data
	 *            Temporal data object
	 * @param copyFromData
	 *            Temporal data object from which attributes are copied
	 * @param deltaList
	 *            Delta list
	 */
	public TemporalValue(ITemporalData<K> data, ITemporalData<K> copyFromData, LinkedList<byte[]> deltaList)
	{
		this((ITemporalKey<K>) null, data);

		this.temporalKey = copyFromData.__getTemporalValue().getTemporalKey();
		this.serializedData = copyFromData.__getTemporalValue().getSerializedData();
		this.serializedAttributes = copyFromData.__getTemporalValue().getSerializedAttributes();
		this.serializedAttachments = copyFromData.__getTemporalValue().getSerializedAttachments();
		this.deltaList = deltaList;
		this.attachmentMap = copyFromData.__getTemporalValue().getAttachmentMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITemporalKey<K> getTemporalKey()
	{
		return temporalKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTemporalKey(ITemporalKey<K> temporalKey)
	{
		this.temporalKey = temporalKey;
	}

	/**
	 * Adds the specified attachment identity key.
	 * 
	 * @param attachmentIdentityKey Attachment identity key
	 */
	public void addAttachmentIdentityKey(K attachmentIdentityKey)
	{
		if (this.attachmentMap == null) {
			this.attachmentMap = new HashMap<String, AttachmentSet<K>>(3);
			AttachmentSetFactory<K> factory = new AttachmentSetFactory<K>();
			this.attachmentMap.put("default", factory.createAttachmentSet(new HashSet<K>(10)));
		}
		this.attachmentMap.get("default").getAttachments().add(attachmentIdentityKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDeltaList(LinkedList<byte[]> deltaList)
	{
		this.deltaList = deltaList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkedList<byte[]> getDeltaList()
	{
		return deltaList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getSerializedAttributes()
	{
		return serializedAttributes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getSerializedData()
	{
		return serializedData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getSerializedAttachments()
	{
		return serializedAttachments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setData(ITemporalData<K> data)
	{
		this.data = data;
	}

	/**
	 * Returns true if the specified bit is dirty.
	 * @param bit Bit number 
	 */
	private boolean isBitDirty(int bit)
	{
		return ((flag >> bit) & 1) == 1;
	}

	/**
	 * Sets or unsets the specified bit dirty.
	 * @param bit Bit number
	 * @param isDirty true to set, false to unset
	 */
	private void setBitDirty(int bit, boolean isDirty)
	{
		if (isDirty) {
			flag |= 1 << bit;
		} else {
			flag &= ~(1 << bit);
		}
	}

	/**
	 * Returns true if the entity is a checkpoint, i.e., a whole object.
	 */
	public boolean isCheckpoint()
	{
		return isBitDirty(BIT_CHECKPOINT);
	}

	/**
	 * Sets or unsets the checkpoint.
	 * @param isCheckpoint true to set, false to unset
	 */
	public void setCheckpoint(boolean isCheckpoint)
	{
		setBitDirty(BIT_CHECKPOINT, isCheckpoint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDelta()
	{
		return isBitDirty(BIT_DELTA);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDelta(boolean isDelta)
	{
		setBitDirty(BIT_DELTA, isDelta);
	}

	/**
	 * Returns a string representation of this object.
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer(100).append("TemporalValue[").append("data=").append(this.data)
				.append("; attachmentMap=").append(this.attachmentMap).append("; attachmentMap=")
				.append(this.attachmentMap).append("]");
		return buffer.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentSet<K> getAttachmentSet(String name)
	{
		if (attachmentMap == null) {
			return null;
		}

		return attachmentMap.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAttachmentSet(String name, AttachmentSet<K> attachmentSet)
	{
		if (attachmentMap == null) {
			attachmentMap = new HashMap(3);
		}
		attachmentMap.put(name, attachmentSet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, AttachmentSet<K>> getAttachmentMap()
	{
		deserializeAttachments();
		return attachmentMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAttachmentMap(Map<String, AttachmentSet<K>> attachmentMap)
	{
		this.attachmentMap = attachmentMap;
	}
}
