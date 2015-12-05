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
 * AbstractDBObject provides the upsert service to write over existing entries
 * in the database.
 * @author dpark
 *
 */
public abstract class AbstractDBObject implements IDBObject
{   
    /**
     * Upserts the specified data object into the database. This implementation
     * first check to see if the row exists in the table to determine whether
     * to insert or update. As such it may fail if it encounters a race condition
     * in this two step process. To prevent this, the subclass must overwrite 
     * this method.
     * 
     * @param dataObject data object.
     * @throws DbManagerException Thrown if there is a DB error. This exception
     *                         may also be thrown if it encounters a race condtion.
     */
    public void upsert(Object dataObject) throws DbManagerException
    {
    	if (isExist(dataObject)) {
    		update(dataObject);
    	} else {
    		try {
    			insert(dataObject);
    		} catch (DbManagerException ex) {
    			// duplicate exception
    			update(dataObject);
    		}
    	}
    }
}
