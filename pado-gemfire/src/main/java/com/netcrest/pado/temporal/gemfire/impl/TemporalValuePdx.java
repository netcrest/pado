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
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.internal.HeapDataOutputStream;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializable;
import com.gemstone.gemfire.pdx.PdxWriter;
import com.gemstone.gemfire.pdx.internal.PdxOutputStream;
import com.gemstone.gemfire.pdx.internal.PdxReaderImpl;
import com.gemstone.gemfire.pdx.internal.PdxType;
import com.gemstone.gemfire.pdx.internal.PdxWriterImpl;
import com.gemstone.gemfire.pdx.internal.TypeRegistry;
import com.netcrest.pado.gemfire.factory.GemfireVersionSpecifics;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.gemfire.ITemporalDeltaPdx;
import com.netcrest.pado.temporal.gemfire.ITemporalPdxSerializable;

/**
 * TemporalValuePdx contains the value and the list of attachment identity keys. The
 * value is always kept serialized in the server. It is deserialized when
 * getValue() is invoked.
 * 
 * @author dpark
 * 
 */
public class TemporalValuePdx<K> implements ITemporalValue<K>, PdxSerializable
{
	private static final long serialVersionUID = 1L;

	private transient ITemporalKey<K> temporalKey;
	private ITemporalData<K> data;
	private byte[] serializedAttachments;
	private Map<String, AttachmentSet<K>> attachmentMap;
	private LinkedList<byte[]> deltaList;

	private boolean isDelta = false;

	public TemporalValuePdx()
	{
	}

	public TemporalValuePdx(ITemporalData<K> data, ITemporalKey<K> tkey)
	{
		this.data = data;
		this.temporalKey = tkey;
	}

	public TemporalValuePdx(ITemporalData<K> data, ITemporalData<K> copyFromData, LinkedList<byte[]> deltaList)
	{
		this(data, null);

		this.temporalKey = copyFromData.__getTemporalValue().getTemporalKey();
		this.deltaList = deltaList;
		this.attachmentMap = copyFromData.__getTemporalValue().getAttachmentMap();
		this.isDelta = deltaList != null && deltaList.size() > 0;
	}

	public ITemporalKey<K> getTemporalKey()
	{
		return temporalKey;
	}

	public void setTemporalKey(ITemporalKey<K> temporalKey)
	{
		this.temporalKey = temporalKey;
	}

	public void addAttachmentIdentityKey(K attachmentIdentityKey)
	{
		if (this.attachmentMap == null) {
			AttachmentSetFactory<K> factory = new AttachmentSetFactory<K>();
			this.attachmentMap = new HashMap<String, AttachmentSet<K>>(3);
			this.attachmentMap.put("TemporalValue.default", factory.createAttachmentSet(new HashSet<K>(10)));
		}
		this.attachmentMap.get("TemporalValue.default").getAttachments().add(attachmentIdentityKey);
	}

	public void setDeltaList(LinkedList<byte[]> deltaList)
	{
		this.deltaList = deltaList;
	}

	public LinkedList<byte[]> getDeltaList()
	{
		return deltaList;
	}

	public void deserializeData()
	{
		try {
			// Get and return the PdxType
			GemFireCacheImpl gfci = GemFireCacheImpl.getExisting("Could not access existing cache");
			TypeRegistry registry = gfci.getPdxRegistry();
			PdxType pdxType = registry.getExistingType(data);

			if (isDelta && data instanceof ITemporalDeltaPdx) {
				ITemporalDeltaPdx base = (ITemporalDeltaPdx) data;
				for (byte[] delta : deltaList) {
					ByteArrayInputStream bais = new ByteArrayInputStream(delta);
					DataInputStream dis = new DataInputStream(bais);
					PdxReader reader = new PdxReaderImpl(pdxType, dis, delta.length);
					base.readDelta(reader);
					bais.close();
					dis.close();
				}
				deltaList.clear();
				deltaList = null;
				isDelta = false;
			}
		} catch (Exception ex) {
			CacheFactory.getAnyInstance().getLogger().error("TemporalDataValue.deserializeData()", ex);
		}
	}
	
	public void deserializeAttachments()
	{
		if (serializedAttachments != null) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(serializedAttachments);
				DataInputStream dis = new DataInputStream(bais);
				this.attachmentMap = (Map<String, AttachmentSet<K>>)DataSerializer.readObject(dis);
				serializedAttachments = null;
			} catch (Exception ex) {
				CacheFactory.getAnyInstance().getLogger().error("TemporalValue.deserializeData()", ex);
			}
		}
	}
	
	// TODO: implement deserializeAttributes()
	public void deserializeAttributes()
	{
	}
	
	public void deserializeAll()
	{
		deserializeData();
		deserializeAttachments();
		deserializeAttributes();
	}

	private byte[] getBlob(ITemporalDeltaPdx delta) throws IOException
	{
		if (delta == null) {
			return null;
		}

		GemFireCacheImpl gfci = GemFireCacheImpl.getExisting("Could not access existing cache");
		TypeRegistry registry = gfci.getPdxRegistry();
		// PdxType pdxType = registry.getExistingType(delta);

		HeapDataOutputStream hdos = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
		PdxOutputStream pos = new PdxOutputStream(hdos);
		PdxWriterImpl writer = new PdxWriterImpl(registry, data, pos);
		delta.writeDelta(writer);
		byte[] blob = hdos.toByteArray();
		hdos.close();
		return blob;
	}

	/**
	 * This method is not used. PDX does not support serializing objects 
	 * independent of region.
	 * @return
	 * @throws IOException
	 */
	private byte[] serializeData() throws IOException
	{
		if (data == null) {
			return null;
		}

		// Get and return the PdxType
		GemFireCacheImpl gfci = GemFireCacheImpl.getExisting("Could not access existing cache");
		TypeRegistry registry = gfci.getPdxRegistry();
		// PdxType type = registry.getExistingType(data);

		HeapDataOutputStream hdos = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
		PdxOutputStream pos = new PdxOutputStream(hdos);
		PdxWriterImpl writer = new PdxWriterImpl(registry, data, pos);
		((ITemporalPdxSerializable<K>) data).writeTemporal(writer);
		byte[] blob = hdos.toByteArray();
		hdos.close();
		return blob;
	}
	
	private byte[] serializeAttachments() throws IOException
	{
		if (data == null) {
			return null;
		}
		HeapDataOutputStream hdos = GemfireVersionSpecifics.getGemfireVersionSpecifics().createHeapDataOutpuStream();
		DataSerializer.writeObject(attachmentMap, hdos);
		byte[] blob = hdos.toByteArray();
		return blob;
	}
	
	public byte[] getSerializedData()
	{
		return null;
	}
	
	public byte[] getSerializedAttributes()
	{
		return null;
	}
	
	public byte[] getSerializedAttachments()
	{
		return serializedAttachments;
	}

	public void setData(ITemporalData<K> data)
	{
		this.data = data;
	}
	
	public boolean isDelta()
	{
		return this.isDelta;
	}

	public void setDelta(boolean isDelta)
	{
		this.isDelta = isDelta;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer(100).append("TemporalValuePdx[").append("data=").append(this.data)
				.append("; attachmentSets=").append(this.attachmentMap).append("]");
		return buffer.toString();
	}
	
	public AttachmentSet<K> getAttachmentSet(String name)
	{
		if (attachmentMap == null) {
			return null;
		}
		
		return attachmentMap.get(name);
	}

	public void setAttachmentSet(String name, AttachmentSet<K> attachmentSet)
	{
		if (attachmentMap == null) {
			attachmentMap = new HashMap(3);
		}
		attachmentMap.put(name, attachmentSet);
	}
	
	@Override
	public Map<String, AttachmentSet<K>> getAttachmentMap()
	{
		return attachmentMap;
	}

	@Override
	public void setAttachmentMap(Map<String, AttachmentSet<K>> attachmentMap)
	{
		this.attachmentMap = attachmentMap;
	}

	public void toData(PdxWriter writer)
	{
		// The server keeps all objects serialized.
		// The client keeps the object serialized until it is
		// accessed via getValue(). For deltas, they are kept
		// in a chain (deltaList) that the client traverses to construct the
		// final object.
		
		writer.writeBoolean("isDelta", isDelta);
		byte[] sa = serializedAttachments;
		if (sa == null) {
			try {
				sa = serializeAttachments();
			} catch (IOException e) {
				// TODO: print exception to the log for now
				e.printStackTrace();
			}
		}
		writer.writeByteArray("attachmentMap", sa);
		byte[][] byteArrays = null;
		if (deltaList != null) {
			byteArrays = new byte[deltaList.size()][];
			for (int i = 0; i < byteArrays.length; i++) {
				byteArrays[i] = deltaList.get(i);
			}
		}
		writer.writeArrayOfByteArrays("deltaList", byteArrays);
	}

	@SuppressWarnings("unchecked")
	public void fromData(PdxReader reader)
	{
		this.attachmentMap = (Map<String, AttachmentSet<K>>)reader.readObject("attachmentMap");
		this.isDelta = reader.readBoolean("isDelta");
		this.serializedAttachments = reader.readByteArray("attachmentMap");
		byte[][] byteArrays = reader.readArrayOfByteArrays("deltaList");
		deltaList = new LinkedList<byte[]>();
		if (byteArrays != null) {
			for (int i = 0; i < byteArrays.length; i++) {
				deltaList.add(byteArrays[i]);
			}
		}
	}
}
