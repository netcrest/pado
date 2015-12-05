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
package com.netcrest.pado.temporal.gemfire.impl;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.internal.HeapDataOutputStream;
import com.netcrest.pado.gemfire.factory.GemfireVersionSpecifics;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalDataSerializable;
import com.netcrest.pado.temporal.ITemporalDelta;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalValue;

/**
 * TemporalValue contains the value and the list of attachment identity keys.
 * The value is always kept serialized in the server. It is deserialized when
 * getValue() is invoked.
 * 
 * @author dpark
 * 
 */
public class GemfireTemporalValue<K> extends TemporalValue<K> implements DataSerializable
{
	private static final long serialVersionUID = 1L;

	public GemfireTemporalValue()
	{
	}

	public GemfireTemporalValue(ITemporalKey<K> tkey, ITemporalData<K> data)
	{
		super(tkey, data);
	}

	public GemfireTemporalValue(ITemporalData<K> data, ITemporalData<K> copyFromData, LinkedList<byte[]> deltaList)
	{
		super(data, copyFromData, deltaList);
	}

	/**
	 * Copies fromData to toData.
	 * 
	 * @param toDeltaData
	 *            Data object with delta. Not a whole object.
	 * @param fromData
	 *            Whole object to copy and apply the delta from toDeltaData. 
	 *            The end result is toDeltaData with fromData's contents with
	 *            its delta applied.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GemfireTemporalValue(ITemporalData<K> toDeltaData, ITemporalData<K> fromData)
	{
		super(toDeltaData, fromData);

		// toDeltaData.serializedData is delta. Set it to deltaList and
		// copy formData.getSerializedData() as its data. This allows
		// the delta to be applied to the copied fromData content.
		deltaList = new LinkedList<byte[]>();
		toDeltaData.__setTemporalValue(this);
		GemfireTemporalValue toTemporalValue = (GemfireTemporalValue) toDeltaData.__getTemporalValue();
		deltaList.add(toTemporalValue.serializedData);
		toTemporalValue.serializedData = fromData.__getTemporalValue().getSerializedData();
	}

	public byte[] getSerializedAttributes()
	{
		try {
			if (serializedAttributes == null) {
				serializedAttributes = serializeAttributes();
			}
		} catch (IOException ex) {
			CacheFactory.getAnyInstance().getLogger().error("GemfireTemporalValue.getSerializedAttributes()", ex);
		}
		return serializedAttributes;
	}

	public byte[] getSerializedData()
	{
		try {
			if (serializedData == null) {
				serializedData = serializeData();
			}
		} catch (IOException ex) {
			CacheFactory.getAnyInstance().getLogger().error("GemfireTemporalValue.getSerializedData()", ex);
		}
		return serializedData;
	}

	public byte[] getSerializedAttachments()
	{
		try {
			if (serializedAttachments == null) {
				serializedAttachments = serializeAttachments();
			}
		} catch (IOException ex) {
			CacheFactory.getAnyInstance().getLogger().error("GemfireTemporalValue.getSerializedAttachments()", ex);
		}
		return serializedAttachments;
	}

	/**
	 * Inflates the temporal attributes, i.e., invokes {@link ITemporalDataSerializable#readTemporalAttributes()},
	 * if not already inflated.
	 */
	@SuppressWarnings("rawtypes")
	public void deserializeAttributes()
	{
		if (serializedAttributes != null) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(serializedAttributes);
				DataInputStream dis = new DataInputStream(bais);
				if (deltaList != null && data instanceof ITemporalDelta) {
					ITemporalDelta base = (ITemporalDelta) data;
					for (byte[] delta : deltaList) {
						bais = new ByteArrayInputStream(delta);
						dis = new DataInputStream(bais);
						base.readDelta(dis);
						bais.close();
						dis.close();
					}
					deltaList.clear();
					deltaList = null;
					setDelta(false);
				} else {
					((ITemporalDataSerializable) data).readTemporalAttributes(dis);
				}
				serializedAttributes = null;
			} catch (Exception ex) {
				CacheFactory.getAnyInstance().getLogger().error("GemfireTemporalValue.deserializeAttributes()", ex);
			}
		}
	}

	/**
	 * Inflates the temporal data, i.e., invokes readTemporal(), if not already
	 * inflated.
	 */
	@SuppressWarnings("rawtypes")
	public void deserializeData()
	{
		if (serializedData != null) {
			try {
				if (isDelta()) {
					// if deltaList is set then serializedData contains the
					// base object. deltaList must be applied to that base
					// object.
					if (deltaList != null) {
						// Contains deltalist to construct a whole object
						ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
						DataInputStream dis = new DataInputStream(bais);
						((ITemporalDataSerializable) data).readTemporal(dis);
						bais.close();
						dis.close();
						serializedData = null;
						if (data instanceof ITemporalDelta) {
							ITemporalDelta base = (ITemporalDelta) data;
							for (byte[] delta : deltaList) {
								ByteArrayInputStream bais2 = new ByteArrayInputStream(delta);
								DataInputStream dis2 = new DataInputStream(bais2);
								base.readDelta(dis2);
								bais2.close();
								dis2.close();
							}
							deltaList.clear();
							deltaList = null;
						}
						// Set isDelta to false since this object is now
						// a whole object.
						setDelta(false);
					} else {
						// Contains only delta information
						ITemporalDelta base = (ITemporalDelta) data;
						ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
						DataInputStream dis = new DataInputStream(bais);
						base.readDelta(dis);
						bais.close();
						dis.close();
						serializedData = null;
					}
				} else {
					// Whole object
					ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
					DataInputStream dis = new DataInputStream(bais);
					((ITemporalDataSerializable) data).readTemporal(dis);
					bais.close();
					dis.close();
					serializedData = null;
				}
			} catch (Exception ex) {
				CacheFactory.getAnyInstance().getLogger().error("GemfireTemporalValue.deserializeData()", ex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void deserializeAttachments()
	{
		if (serializedAttachments != null) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(serializedAttachments);
				DataInputStream dis = new DataInputStream(bais);
				this.attachmentMap = (Map<String, AttachmentSet<K>>) DataSerializer.readObject(dis);
				serializedAttachments = null;
			} catch (Exception ex) {
				CacheFactory.getAnyInstance().getLogger().error("GemfireTemporalValue.deserializeData()", ex);
			}
		}
	}

	public void deserializeAll()
	{
		deserializeData();
		deserializeAttachments();
		deserializeAttributes();
	}

	public void serializeAll() throws IOException
	{
		this.serializedData = serializeData();
		this.serializedAttachments = serializeAttachments();
		this.serializedAttributes = serializeAttributes();
	}

	private byte[] getBlob(ITemporalDelta delta) throws IOException
	{
		if (delta == null) {
			return null;
		}

		HeapDataOutputStream hdos = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
		delta.writeDelta(hdos);
		byte[] blob = hdos.toByteArray();
		return blob;
	}

	private byte[] serializeData() throws IOException
	{
		if (data == null) {
			return null;
		}
		if (serializedData != null) {
			return serializedData;
		}
		HeapDataOutputStream hdos = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
		((ITemporalDataSerializable<K>) data).writeTemporal(hdos);
		byte[] blob = hdos.toByteArray();
		return blob;
	}

	private byte[] serializeAttributes() throws IOException
	{
		if (data == null) {
			return null;
		}
		if (serializedAttributes != null) {
			return serializedAttributes;
		}
		HeapDataOutputStream hdos = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
		((ITemporalDataSerializable<K>) data).writeTemporalAttributes(hdos);
		byte[] blob = hdos.toByteArray();
		return blob;
	}

	private byte[] serializeAttachments() throws IOException
	{
		if (data == null) {
			return null;
		}
		if (serializedAttachments != null) {
			return serializedAttachments;
		}
		HeapDataOutputStream hdos = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
		DataSerializer.writeObject(attachmentMap, hdos);
		byte[] blob = hdos.toByteArray();
		return blob;
	}
	
	public void setSerializedData(byte[] serializedData)
	{
		this.serializedData = serializedData;
	}

	public void toData(DataOutput out) throws IOException
	{
		// The server keeps all objects serialized.
		// The client keeps the object serialized until it is
		// accessed via getValue(). For deltas, they are kept
		// in a chain (deltaList) that the client traverses to construct the
		// final object.
		byte[] sd = serializedData;
		if (sd == null) {
			if (data instanceof ITemporalDelta) {
				ITemporalDelta delta = (ITemporalDelta) data;
				if (isDelta()) {
					sd = getBlob(delta);
				} else {
					sd = serializeData();
				}
			} else {
				sd = serializeData();
			}
		}
		byte[] sattr = serializedAttributes;
		if (sattr == null) {
			sattr = serializeAttributes();
		}
		byte[] sa = serializedAttachments;
		if (sa == null) {
			sa = serializeAttachments();
		}
		DataSerializer.writeByte(flag, out);
		DataSerializer.writeByteArray(sd, out);
		DataSerializer.writeByteArray(sattr, out);
		DataSerializer.writeByteArray(sa, out);
		DataSerializer.writeLinkedList(deltaList, out);
	}

	public void fromData(DataInput in) throws IOException, ClassNotFoundException
	{
		this.flag = DataSerializer.readByte(in);
		this.serializedData = DataSerializer.readByteArray(in);
		this.serializedAttributes = DataSerializer.readByteArray(in);
		this.serializedAttachments = DataSerializer.readByteArray(in);
		this.deltaList = DataSerializer.readLinkedList(in);
	}
}
