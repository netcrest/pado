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
package com.netcrest.pado.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ObjectSerializer serializes objects by invoking IObjectSerializer plug-in
 * that supports the object serialization API. It iteratively applies each
 * plug-in in the ordered list until the one that can serialize the object. As
 * such, for performance conscience applications, the plug-in that is most often
 * used should be placed in the top of the list.
 * 
 * @author dpark
 * 
 */
public class ObjectSerializer
{
	private static List<IObjectSerializer> serializerList = new ArrayList<IObjectSerializer>(3);
	private static IObjectSerializer serializers[];

	/**
	 * Adds all of IObjectSerializer objects in the specified list.
	 * 
	 * @param serializerList
	 *            List of IObjectSerialzier objects
	 */
	public static void addSerializerList(List<IObjectSerializer> serializerList)
	{
		if (serializerList == null) {
			return;
		}
		for (IObjectSerializer serializer : serializerList) {
			if (ObjectSerializer.serializerList.contains(serializer) == false) {
				ObjectSerializer.serializerList.add(serializer);
			}
		}
		serializers = ObjectSerializer.serializerList.toArray(new IObjectSerializer[ObjectSerializer.serializerList
				.size()]);
	}

	/**
	 * Inserts the specified IObjectSerialzier objects at the specified index.
	 * 
	 * @param index
	 *            List position. Valid range is [0, size()-1].
	 * @param serializer
	 *            Serializer
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (
	 *             <tt>index &lt; 0 || index &gt; size()</tt>)
	 */
	public static void addSerializer(int index, IObjectSerializer serializer)
	{
		if (serializer == null) {
			return;
		}
		if (ObjectSerializer.serializerList.contains(serializer) == false) {
			serializerList.add(index, serializer);
			serializers = ObjectSerializer.serializerList.toArray(new IObjectSerializer[ObjectSerializer.serializerList
					.size()]);
		}
	}

	/**
	 * Appends the specifier serialzer.
	 * 
	 * @param serializer
	 *            Serializer
	 */
	public static void addSerializer(IObjectSerializer serializer)
	{
		if (serializer == null) {
			return;
		}
		if (ObjectSerializer.serializerList.contains(serializer) == false) {
			serializerList.add(serializer);
			serializers = ObjectSerializer.serializerList.toArray(new IObjectSerializer[ObjectSerializer.serializerList
					.size()]);
		}
	}

	/**
	 * Returns the IObjectSerializer object count.
	 */
	public static int size()
	{
		return ObjectSerializer.serializerList.size();
	}

	/**
	 * Serializes the specified object. The object must conform to at least
	 * one of the IObjectSerializer plug-ins in the ObjectSerializer list.
	 * 
	 * @param object
	 *            Object to serialize
	 * @return Serialized object in byte array
	 * @throws IOException
	 *             Thrown if a stream-level error occurs
	 */
	public static byte[] serialize(Object object) throws IOException
	{
		byte[] blob = null;
		IOException ioEx = null;
		for (IObjectSerializer serializer : serializers) {
			try {
				blob = serializer.serialize(object);
			} catch (IOException ex) {
				ioEx = ex;
			}
		}
		if (blob == null && ioEx != null) {
			throw ioEx;
		}
		return blob;
	}

	/**
	 * Deserializes the specified blob (byte array). The blob must be created by
	 * one of the IObjectSerializer plug-ins in the ObjectSerializer list.
	 * 
	 * @param blob
	 *            Byte array to deserailize
	 * @return Deserialized object
	 * @throws IOException
	 *             Thrown if a stream-level error occurs
	 * @throws ClassNotFoundException
	 *             Thrown if a required class is not found
	 */
	public static Object deserialize(byte[] blob) throws IOException, ClassNotFoundException
	{
		if (blob == null) {
			return null;
		}
		Object obj = null;
		IOException ioEx = null;
		for (IObjectSerializer serializer : serializers) {
			try {
				obj = serializer.deserialize(blob);
				if (obj != null) {
					break;
				}
			} catch (IOException ex) {
				ioEx = ex;
			}
		}
		if (obj == null && ioEx != null) {
			throw ioEx;
		}
		return obj;
	}
}
