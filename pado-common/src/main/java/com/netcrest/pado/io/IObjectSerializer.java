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
import java.io.Serializable;

/**
 * IObjectSerializer provides the means to serialize objects using a third party
 * serialization mechanism without exposing the vendor API. Typically, the
 * underlying data grid product provides an efficient way to serialize objects
 * by leveraging its data management facility, which may require the use of its
 * API to implement the object model leading to a vendor locked-in solution.
 * IObjectSerialzer solves this problem by providing the ability to plug in one
 * or more object model implementations by simply adding serialization
 * mechanisms in the form of IObjectSerializer.
 * <p>
 * To add an IObjectSerializer plug-in, implement this interface and add an
 * instance of the implementation by invoking
 * {@link ObjectSerializer#addSerializer(IObjectSerializer)}. The implementation
 * class should also implement {@link Object#equals(Object)} to ensure only one
 * instance can be added in ObjectSerializer. For example, equals() should
 * return true if the object has the same type.
 * 
 * @author dpark
 * 
 */
public interface IObjectSerializer extends Serializable
{
	/**
	 * Serializes the specified object.
	 * 
	 * @param object
	 *            Object to be serialized
	 * @return Byte array of the serialized object
	 * @throws IOException
	 *             Thrown if a stream-level error occurs
	 */
	public byte[] serialize(Object object) throws IOException;

	/**
	 * Deserializes the specfied blob into an object.
	 * 
	 * @param blob
	 *            Byte array of serialized object
	 * @return Deserialized object
	 * @throws IOException
	 *             Thrown if a stream-level error occurs
	 * @throws ClassNotFoundException
	 *             Thrown if a required class for deserialzation is not found
	 */
	public Object deserialize(byte[] blob) throws IOException, ClassNotFoundException;
}
