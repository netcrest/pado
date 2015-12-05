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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.netcrest.pado.log.Logger;

/**
 * DbThreadConnectionPool maintains a pool of JDBC connections for individual
 * threads. It provides a connection for the current thread. 
 * @author dpark
 *
 */
public class DbThreadConnectionPool
{
	private Map<Thread, Connection> connectionMap = new HashMap<Thread, Connection>();
	private String poolName;
	private String dbUrl;
	private String driverClassName;
	private Driver driver;
	
	/** user name to use for establishing JDBC connection */
	protected String userName;

	/** password for the user to use for establishing JDBC connection */
	protected String passwd;

	/**
	 * Creates a DbThreadConnectionPool object for the specified parameters.
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
	 */
	DbThreadConnectionPool(String poolName, String dbUrl, String driverClassName, String userName, String passwd)
	{
		this.poolName = poolName;
		this.dbUrl = dbUrl;
		this.driverClassName = driverClassName;
		this.userName = userName;
		this.passwd = passwd;
		init();
	}

	/**
	 * Initializes the parameters.
	 */
	private void init()
	{
		String maskedPasswordDbUrl = null;
		if (dbUrl != null) {
			maskedPasswordDbUrl = maskPassword(this.dbUrl);
		}
		try {
			Class.forName(this.driverClassName).newInstance();
			this.driver = DriverManager.getDriver(this.dbUrl);
		} catch (Exception e) {
			throw new RuntimeException("Invalid JDBC driver: " + driverClassName + ", " +  maskedPasswordDbUrl, e);
		}
	}
	
	/**
	 * Returns the pool name.
	 */
	public String getPoolName()
	{
		return poolName;
	}
	
	/**
	 * Returns the DB URL.
	 */
	public String getDbUrl()
	{
		return dbUrl;
	}
	
	/**
	 * Returns the fully-qualified driver class name.
	 */
	public String getDriverClassName()
	{
		return driverClassName;
	}

	/**
	 * Returns the connection that belongs to the current thread. If the connection
	 * is closed it tries to reconnect. The caller must catch the exception to handle
	 * connection failure.
	 * @throws SQLException if a database access error occurs
	 */
	public Connection getConnection() throws SQLException
	{
		Connection conn = connectionMap.get(Thread.currentThread());
		if (conn == null || conn.isClosed()) {
			conn = this.instantiateConnection();
			Logger.info("JDBC connection established: " + driverClassName);
			connectionMap.put(Thread.currentThread(), conn);
		}
		return conn;
	}

	/**
	 * Removes and closes the connection belonging to the current thread.
	 * 
	 * @throws SQLException if a database access error occurs
	 */
	public void closeConnection() throws SQLException
	{
		Connection conn = connectionMap.remove(Thread.currentThread());
		conn.close();
	}

	/**
	 * Closes all connections and clears the pool. This call should be made only
	 * after all threads have been stopped from accessing the pool.
	 * 
	 * @throws SQLException
	 */
	synchronized void close() throws SQLException
	{
		Collection<Connection> col = connectionMap.values();
		for (Connection connection : col) {
			connection.close();
		}
		connectionMap.clear();
	}

	/** mask the known password patterns from URL for exception/log messages */
	protected static final String maskPassword(final String dbUrl)
	{
		String maskedPasswordDbUrl = Pattern.compile("(password|passwd|pwd|secret)=[^;]*", Pattern.CASE_INSENSITIVE)
				.matcher(dbUrl).replaceAll("$1=***");
		return maskedPasswordDbUrl;
	}

	protected synchronized Connection instantiateConnection()
	{
		// if (this.driver == null) {
		// initConnection();
		// return;
		// }
		String maskedPasswordDbUrl = null;
		Connection conn = null;
		try {
			// use Driver directly for connect instead of looping through all
			// drivers as DriverManager.getConnection() would do, to avoid
			// hitting any broken drivers in the process (vertica driver is
			// known to
			// fail in acceptsURL with this set of properties)
			final Properties props = new Properties();
			// the user/password property names are standard ones also used by
			// DriverManager.getConnection(String, String, String) itself, so
			// will work for all drivers
			if (this.userName != null) {
				props.put("user", this.userName);
			}
			if (this.passwd != null) {
				// password is now stored encrypted
				// String decPasswd =
				// AsyncEventHelper.decryptPassword(this.userName,
				// this.passwd, this.transformation, this.keySize);
				// props.put("password", decPasswd);
				// decPasswd = null;
				props.put("password", passwd);
			}

			conn = this.driver.connect(this.dbUrl, props);
			// null to GC password as soon as possible
			props.clear();
			try {
				// try to set the default isolation to at least READ_COMMITTED
				// need it for proper HA handling
				if (conn.getTransactionIsolation() < Connection.TRANSACTION_READ_COMMITTED
						&& conn.getMetaData().supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED)) {
					conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
					if (this.dbUrl != null) {
						maskedPasswordDbUrl = maskPassword(this.dbUrl);
					}
					Logger.info("explicitly set the transaction isolation level to " + "READ_COMMITTED for URL: "
							+ maskedPasswordDbUrl);
				}
			} catch (SQLException sqle) {
				// ignore any exception here
			}
			conn.setAutoCommit(true);
		} catch (Exception e) {
			if (this.dbUrl != null) {
				maskedPasswordDbUrl = maskPassword(this.dbUrl);
			}
			// throttle retries for connection failures
			try {
				Thread.sleep(200);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException("JDBC connection error: " + this.driverClassName + maskedPasswordDbUrl, e);
		}

		return conn;
	}
}
