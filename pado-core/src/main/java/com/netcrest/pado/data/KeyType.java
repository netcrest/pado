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

import java.util.Set;

/**
 * KeyType represents the schema definitions for predefining keys for
 * lightweight self-describing message classes such as JsonLite provided in this
 * package.
 * 
 * @author dpark
 * 
 * @param <T>
 */
public interface KeyType
{
	/**
	 * Returns the universal ID that uniquely represents the key type across
	 * space and time. The underlying message class implementation must
	 * guarantee the uniqueness of this ID to properly marshal objects crossing
	 * network and language boundaries. This ID is static and permanent for the
	 * life of the key type class.
	 */
	public Object getId();

	/**
	 * Returns the merge point, i.e., the merged-at version number. It returns
	 * -1 if merge has not occurred.
	 */
	public int getMergePoint();

	/**
	 * Returns the version number.
	 */
	public int getVersion();

	/**
	 * Returns the key count.
	 */
	public int getKeyCount();

	/**
	 * Returns the index of the key.
	 */
	public int getIndex();

	/**
	 * Returns the name of the key.
	 */
	public String getName();

	/**
	 * Returns the class of the key.
	 */
	public Class getType();

	/**
	 * Returns the entire keys.
	 */
	public KeyType[] getValues();

	/**
	 * Returns the entire keys of the specified version.
	 * 
	 * @param version
	 *            The version number.
	 */
	public KeyType[] getValues(int version);

	/**
	 * Returns the key of the specified key name.
	 * 
	 * @param name
	 *            The key name.
	 */
	public KeyType getKeyType(String name);

	/**
	 * Returns true if delta propagation is enabled.
	 */
	public boolean isDeltaEnabled();

	/**
	 * Returns true if the key has been deprecated.
	 */
	public boolean isDeprecated();

	/**
	 * Returns all deprecated indexes in this key type. It returs an empty array
	 * if there are no deprecated key types.
	 */
	public int[] getDeprecatedIndexes();

	/**
	 * Returns true if the key value is to be kept serialized until it is
	 * accessed. This applies per key instance.
	 */
	public boolean isKeyKeepSerialized();

	/**
	 * Returns true if the network payload is to be compressed.
	 */
	public boolean isCompressionEnabled();

	/**
	 * Returns true if any of the key values is to be kept serialized.
	 */
	public boolean isPayloadKeepSerialized();

	/**
	 * Returns the key name set.
	 */
	public Set<String> getNameSet();

	/**
	 * Returns true if the specified key is defined.
	 * 
	 * @param name
	 *            The key to check.
	 */
	public boolean containsKey(String name);

	/**
	 * Returns the data class that represents this key type.
	 * 
	 * @return Null if a data class has not been specified.
	 */
	public Class<?> getDomainClass();

	/**
	 * Returns the references. Returns an empty array if references are not
	 * defined.
	 */
	public KeyType[] getReferences();

	/**
	 * Sets references.
	 * 
	 * @param references
	 *            References. If null, an empty array is assigned.
	 */
	public void setReferences(KeyType[] references);

	/**
	 * Returns true if this constant is configured to hold an object reference.
	 */
	public boolean isReference();

	/**
	 * Returns the query that resolves this key's referenced object(s).
	 */
	public String getQuery();

	/**
	 * Sets the query that resolves this key's referenced object(s).
	 * 
	 * @param query
	 *            Query string supported by the underlying data service
	 *            mechenism. For Pado, its data service supports PQL (Pado Query
	 *            Language) which is a hybrid query language with support for
	 *            Lucene and GemFir OQL.
	 */
	public void setQuery(String query);

	/**
	 * Returns the depth of the referenced object(s). 0 indicates no reference
	 * and therefore the query is not performed. 1 or a larger value indicates
	 * the level of nested objects to stitch by the query service.
	 */
	public int getDepth();

	/**
	 * Sets the depth of the referenced object(s). 0 indicates no reference and
	 * therefore the query is not performed. 1 or a larger value indicates the
	 * level of nested objects to stitch by the query service.
	 * 
	 * @param depth
	 */
	public void setDepth(int depth);
}
