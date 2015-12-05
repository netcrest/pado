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

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * DbThreadConnectionPoolManager maintains named DbThreadConnectionPool objects
 * allowing access to multiple connection pools with different data sources in
 * a single JVM.
 * @author dpark
 *
 */
public class DbThreadConnectionPoolManager
{
	private static DbThreadConnectionPoolManager manager = new DbThreadConnectionPoolManager();
	
	private Map<String, DbThreadConnectionPool> poolMap = new HashMap<String, DbThreadConnectionPool>();
	
	public static DbThreadConnectionPoolManager getDbConnectionPoolManager()
	{
		return manager;
	}

	/**
	 * Creates a pool of JDBC connections for the specified connection
	 * parameters. The returned pool is mapped by the specified pool name and
	 * contains connections mapped by threads.
	 * 
	 * @param poolName
	 *            Pool name uniquely identifying the connection pool.
	 * @param dbUrl
	 *            Database URL
	 * @param driverClassName
	 *            JDBC driver class name.
	 * @param userName
	 *            User name.
	 * @param passwd
	 *            Password
	 * @return Connection pool
	 */
	public synchronized DbThreadConnectionPool createPool(String poolName, String dbUrl, String driverClassName,
			String userName, String passwd)
	{
		DbThreadConnectionPool pool = poolMap.get(poolName);
		if (pool == null) {
			pool = new DbThreadConnectionPool(poolName, dbUrl, driverClassName, userName, passwd);
			poolMap.put(poolName, pool);
		}
		return pool;
	}

	/**
	 * Returns the connection pool mapped by the specified pool name.
	 * 
	 * @param poolName Pool name uniquely identifying a pool.
	 * @return Returns null if the pool does not exist. A pool must be created
	 *         first by invoking
	 *         {@link #createPool(String, String, String, String, String)}.
	 */
	public DbThreadConnectionPool getPool(String poolName)
	{
		return poolMap.get(poolName);
	}

	/**
	 * Closes all of the connections in the specified pool.
	 * @param poolName Pool name uniquely identifying a pool.
	 * @throws SQLException if a database access error occurs
	 */
	public synchronized void closePool(String poolName) throws SQLException
	{
		DbThreadConnectionPool pool = poolMap.remove(poolName);
		if (pool != null) {
			pool.close();
		}
	}
	
	/**
	 * Closes all of the connections in all pools. This call does not 
	 * clear the connection pools.
	 * @throws SQLException Thrown if unable to close the DB connections
	 */
	public synchronized void close() throws SQLException
	{
		Collection <DbThreadConnectionPool> col = poolMap.values();
		for (DbThreadConnectionPool pool : col) {
			pool.close();
		}
	}
}
