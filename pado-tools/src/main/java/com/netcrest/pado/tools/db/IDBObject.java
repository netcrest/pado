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
package com.netcrest.pado.tools.db;

/**
 * IDBObject is an interface to DB tables. It transforms objects to tables
 * and vice versa.
 * @author dpark
 *
 */
public interface IDBObject
{   
	/**
	 * Initializes this object. This method is useful when the object
	 * must be created by the default no-arg constructor and is in
	 * need of initialization thereafter.
	 * @param args Arguments determined by the underlying protocol.
	 */
	public void initialize(Object...args);
	
	/**
	 * Returns true if this object has been properly initialized. It normally
	 * returns true if the method {@link #initialize(Object...)} has been invoked.
	 */
	public boolean isInitialized();
	
    /**
     * Returns true if the specified data object exists in the database.
     * 
     * @param dataObject data object
     * @throws DbManagerException Thrown if there is a DB error.
     * @see AbstractDBObject
     */
    public boolean isExist(Object dataObject) throws DbManagerException;

    /**
     * Upserts the specified data object into the database.
     * 
     * @param dataObject data object.
     * @throws DbManagerException Thrown if there is a DB error. This exception
     *                         may also be thrown if it encounters a race condtion.
     */
    public void upsert(Object dataObject) throws DbManagerException;
   
    /**
     * Inserts the specified data object into the database.
     * 
     * @param dataObject data object.
     * @throws DbManagerException Thrown if there is a DB error.
     */
	public void insert(Object dataObject) throws DbManagerException;
	
	/**
     * Updates the specified data object in the database.
     * 
     * @param dataObject data object
     * @throws DbManagerException Thrown if there is a DB error
     */
	public void update(Object dataObject) throws DbManagerException;
	
	/**
     * Deletes the specified data object from the database.
     * 
     * @param key key object
     * @param dataObject data object
     * @throws DbManagerException Thrown if there is a DB error
     */
	public void delete(Object key, Object dataObject) throws DbManagerException;
	
	/**
	 * Returns the object that is mapped by the specified primary key.
	 * 
	 * @param primaryKey Primary key
	 * @throws DbManagerException Thrown if there is a DB error
	 */
	public Object get(Object primaryKey) throws DbManagerException;
}
