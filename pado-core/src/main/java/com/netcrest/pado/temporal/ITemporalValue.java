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
import java.util.LinkedList;
import java.util.Map;

/**
 * ITemporalValue is used internally for storing temporal value and its
 * attachment identity key list. It also provides access to the mapped temporal
 * key.
 * 
 * This interface is provided for administrative purposes only.
 * 
 * @author dpark
 * 
 * @param <K>
 *            Identity key type
 */
public interface ITemporalValue<K> extends Serializable
{
	/**
	 * Returns the temporal key.
	 */
	public ITemporalKey<K> getTemporalKey();

	/**
	 * Sets the specified temporal key.
	 * 
	 * @param temporalKey
	 */
	public void setTemporalKey(ITemporalKey<K> temporalKey);

	/**
	 * Sets the specified temporal data.
	 * 
	 * @param data
	 *            Temporal data
	 */
	public void setData(ITemporalData<K> data);

	/**
	 * Returns true if the temporal entity is a delta.
	 */
	public boolean isDelta();

	/**
	 * Sets delta flag
	 * 
	 * @param isDelta
	 *            true if delta, false if whole object
	 */
	public void setDelta(boolean isDelta);

	/**
	 * Returns the serialized data in binary
	 */
	public byte[] getSerializedData();

	/**
	 * Returns the serialized attributes in binary
	 */
	public byte[] getSerializedAttributes();

	/**
	 * Returns the serialized attachments in binary
	 */
	public byte[] getSerializedAttachments();

	/**
	 * Deserializes attributes.
	 */
	public void deserializeAttributes();

	/**
	 * Deserializes data.
	 */
	public void deserializeData();

	/**
	 * Deserializes attachments.
	 */
	public void deserializeAttachments();

	/**
	 * Deserializes all including data, attributes, and attachments.
	 */
	public void deserializeAll();

	// public Set<K> getAttachmentSet(String name);
	// public void setAttachmentSet(String name, String regionPath, Set<K>
	// attachmentSet);

	/**
	 * Returns the attachment set pertaining to the specified attachment name.
	 * 
	 * @param name
	 *            Attachment name
	 */
	public AttachmentSet<K> getAttachmentSet(String name);

	/**
	 * Sets a attachment set.
	 * 
	 * @param name
	 *            Attachment name that identitfies the attachment set
	 * @param attachmentSet
	 *            Attachment set
	 */
	public void setAttachmentSet(String name, AttachmentSet<K> attachmentSet);

	/**
	 * Returns the attachment set map that contains &lt;name, AttachmentSet&gt;
	 * pairs.
	 */
	public Map<String, AttachmentSet<K>> getAttachmentMap();

	/**
	 * Sets the attachment map that contains &lt;name, AttachmentSet&gt; pairs.
	 * 
	 * @param attachmentMap
	 *            Attachment map
	 */
	public void setAttachmentMap(Map<String, AttachmentSet<K>> attachmentMap);

	/**
	 * Returns a linked list (chain) of all deltas in binary. The delta list
	 * contains all of the changes made to the entity in sequential order. The
	 * list can be applied to the entity to construct the whole object.
	 */
	public LinkedList<byte[]> getDeltaList();

	/**
	 * Sets the delta list.
	 * 
	 * @param deltaList
	 *            Linked list (chain) of all deltas in binary.
	 */
	public void setDeltaList(LinkedList<byte[]> deltaList);
}
