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

import java.util.Map;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.data.jsonlite.JsonLiteException;

/**
 * KeyMap extends @{link Map} for implementing schema-based message classes.
 * 
 * @author dpark
 * 
 * @param <V>
 * @see JsonLite
 */
public interface KeyMap<V> extends Map<String, V>
{
	/**
	 * Returns the key type ID that is universally unique. This call is
	 * equivalent to <code>getKeyType().getId()</code>.
	 */
	public Object getId();

	/**
	 * Returns the count of all keys defined by the key type. This method call
	 * is equivalent to invoking
	 * <code>{@link #getKeyType()}.getKeyCount()</code>. It returns 0 if KeyMap
	 * is not initialized, i.e., key type is not defined.
	 */
	public int getKeyCount();

	/**
	 * Returns the key type constant used to initialize this object.
	 */
	public KeyType getKeyType();

	/**
	 * Returns the fully qualified class name of the key type. It returns null
	 * if the key type is not defined.
	 */
	public String getKeyTypeName();

	/**
	 * Returns the key type version. There are one or more key type versions per
	 * ID. This method call is equivalent to invoking
	 * <code>getKeyType().getVersion()</code>.
	 */
	public int getKeyTypeVersion();

	/**
	 * Returns the simple (short) class name of the key type. It returns null if
	 * the key type is not defined.
	 */
	public String getName();

	/**
	 * Returns true if GemFire delta propagation is enabled and there are
	 * changes in values.
	 */
	public boolean hasDelta();

	/**
	 * Returns true if there are changes made in values.
	 */
	public boolean isDirty();

	/**
	 * Merges this object to the specified key type version. Note that for both
	 * upgrade and downgrade drop attributes that are not part of the specified
	 * key type.
	 * 
	 * @param toKeyType
	 *            Key type to merge to.
	 */
	public void merge(KeyType toKeyType);

	/**
	 * Puts the specified value mapped by the specified key type into this
	 * object. If the default constructor is used to create this object then
	 * this method implicitly initializes itself with the specified key type if
	 * it has not been initialized previously.
	 * <p>
	 * Note that for KeyMap to be language neutral, the value type must be a
	 * valid type. It must be strictly enforced by the application. For Java
	 * only applications, any Serializable objects are valid.
	 * 
	 * @param keyType
	 *            The key type constant to lookup the mapped value.
	 * @param value
	 *            The value to put into the KeyMap object.
	 * @return Returns the old value. It returns null if the old value does not
	 *         exist or has been explicitly set to null.
	 * @throws InvalidKeyException
	 *             A runtime exception thrown if the passed in value type does
	 *             not match the key type.
	 */
	public V put(KeyType keyType, V value) throws InvalidKeyException;

	/**
	 * Returns the value of the specified key type. If the default constructor
	 * is used to create this object then this method implicitly initializes
	 * itself with the specified key type if it has not been initialized
	 * previously.
	 * 
	 * @param keyType
	 *            The key type constant to lookup the mapped value.
	 * @return Returns the mapped value. It returns null if the value does not
	 *         exist or it was explicitly set to null.
	 */
	public V get(KeyType keyType);

	/**
	 * Puts the specified reference object.
	 * 
	 * @param key
	 *            Key name
	 * @param value
	 *            Reference object
	 * @throws InvalidKeyException
	 *             Thrown if the specified key is not a reference key.
	 */
	public void putReference(String key, Object value, Object keyMapReferenceId) throws InvalidKeyException;

	/**
	 * Puts the specified reference object.
	 * 
	 * @param keyType
	 *            Key type
	 * @param value
	 *            Reference object
	 * @throws InvalidKeyException
	 *             Thrown if the specified key is not a reference key.
	 */
	public void putReference(KeyType keyType, Object value, Object keyMapReferenceId) throws InvalidKeyException;

	/**
	 * Returns the reference object mapped by the specified key type.
	 * 
	 * @param refKeyType
	 *            Reference key type
	 * @return null if the reference object does not exist
	 */
	public Object getReference(KeyType refKeyType);

	/**
	 * Returns the reference object mapped by the specified key name.
	 * 
	 * @param refKeyType
	 *            Reference key type
	 * @return null if the reference object does not exist
	 */
	public Object getReference(String refKeyType);
	
	/**
	 * Returns true if reference objects exists. It returns false if references
	 * are not defined in KeyType.
	 */
	public boolean hasReferences();
	
	/**
	 * Returns indented string representation of JsonLite for display purposes.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, portable, transmittable representation
	 *         of the object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JsonLiteException
	 *             If the object contains an invalid number.
	 */
	public String toString(int indentFactor, boolean isReference, boolean isHeader);
}
