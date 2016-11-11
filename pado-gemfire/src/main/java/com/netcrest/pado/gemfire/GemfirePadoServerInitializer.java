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
package com.netcrest.pado.gemfire;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.exception.ConfigurationException;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.impl.PadoClientManager;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.internal.util.QueueDispatcherMultiplexerPool;
import com.netcrest.pado.internal.util.SystemClassPathManager;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.gemfire.impl.BucketRebalanceResourceObserver;

public class GemfirePadoServerInitializer implements Declarable
{
	@Override
	public void init(final Properties props)
	{
		// Register DataSerializable class IDs.Load the class.
		new DataSerializables();

		// Initialize pado.xml
		initConfig(props);

		// Initialize plug-in jars. This must be done before GemFire regions
		// are loaded with persistent data in order to register data classes.
		initPlugins();

		// Delay initialization of GemFire region and Pado internals if
		// specified. Do the initialization in a separate thread to
		// return immediately.
		long initDelay = GemfirePadoServerManager.getPadoServerManager().getInitDelay();
		Timer timer = new Timer("Pado-GemfirePadoServerInitializer", true);
		timer.schedule(new TimerTask() {
			public void run()
			{
				launchInit(props);
			}
		}, initDelay);
	}

	/**
	 * This method is invoked before initializing GemFire regions.
	 * 
	 * @param props
	 */
	private void initConfig(Properties props)
	{
		try {
			Logger.config("Initializing Pado...");

			// // Load properties files in the following order. One that follows
			// // overwrites the previous ones.
			// // 1. Load etc/plugins.properties -- the name is fixed and cannot
			// be changed
			// // 2. Load etc/pado.properties -- the name is fixed and cannot be
			// changed
			// // 3. Load etc/<grid-id>/plugins.properties
			// // 4. Load etc/<grid-id>/pado.properties
			//
			// String etcDir = PadoUtil.getProperty(PROP_ETC_DIR);
			// String etcGridDir = PadoUtil.getProperty(PROP_ETC_GRID_DIR);
			//
			// // 1. Load gemfire-plugins.properties deployed as resource.
			// loadResourceProperties();
			//
			// // 2. Load the default plugins.properties - overwrites default
			// values
			// loadProperties(null, DEFAULT_PADO_PLUGINS_PROPERTY_FILE_NAME,
			// etcDir);
			//
			// // 3. Load the default pado.properties - overwrites default
			// values
			// loadProperties(null, DEFAULT_PADO_PROPERTY_FILE_NAME, etcDir);
			//
			// // 4. Load user specified plugins.properties - overwrites the
			// default plugins.properties
			// loadProperties(PROP_PADO_PLUGINS_PROPERTY_FILE,
			// DEFAULT_PADO_PLUGINS_PROPERTY_FILE_NAME, etcGridDir);
			//
			// // 5. Load pado.properties - overwrites plugin.properties
			// loadProperties(PROP_PADO_PROPERTY_FILE,
			// DEFAULT_PADO_PROPERTY_FILE_NAME, etcGridDir);

			// Initialize PadoClientManager
			PadoClientManager.initialize();

			// Initialize Pado. Note that PadoManager defers initialization
			// until GemFire has fully been initialized.
			PadoServerManager.initializePadoManager(props);

		} catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new ConfigurationException(ex);
		}
	}

	/**
	 * This method must be invoked after the pado.xml configuration file has
	 * been read and validated.
	 * 
	 * @param props
	 */
	private void launchInit(Properties props)
	{
		try {
			// Initialize cache servers with cache server settings obtained from
			// the program arguments.
			CacheServerInitializer cacheServerInitializer = new CacheServerInitializer();
			cacheServerInitializer.postInit();

			PadoServerManager.getPadoServerManager().startPadoManager();

			// The following calls must be made after PadoServerManager is
			// started.

			// Initialize DQueue
			GemfireDQueueManager.initialize();

			// Initialize all app specific business classes.
			// IBiz class initialization was deferred till now.
			PadoServerManager.getPadoServerManager().__initAppBizClasses();
			Logger.config("Deferred IBiz class initialization complete.");

			// Register DQueue plugins - deferred call as a workaround
			// to resolve an initialization order conflict.
			GemfireDQueueManager.registerPlugins();

			// Initialize the temporal failover/rebalancing mechanism
			new BucketRebalanceResourceObserver().init(null);

			// Initialize Pado with its DB specifics
			initDb();

			// Invoke user-supplied startup initialization bean. This must
			// be invoked last.
			PadoServerManager.getPadoServerManager().__initStartup();

			Logger.config("Pado initialized.");
		} catch (Exception ex) {
			Logger.severe(ex);
		}
	}

	private void loadResourceProperties() throws IOException
	{
		Properties properties = new Properties();
		URL url = this.getClass().getClassLoader()
				.getResource("gemfire-" + Constants.DEFAULT_PADO_PLUGINS_PROPERTY_FILE_NAME);
		if (url != null) {
			FileReader reader = new FileReader(url.getFile());
			properties.load(reader);
			PadoUtil.getPadoProperties().putAll(properties);
			reader.close();
		}
	}

	private void loadProperties(String propPropertiesFile, String propDefaultFileName, String dirPath)
			throws IOException
	{
		Properties properties = new Properties();
		String propertiesFile = PadoUtil.getProperty(propPropertiesFile);
		File file;
		if (propertiesFile == null) {
			file = new File(dirPath, propDefaultFileName);
		} else {
			file = new File(propertiesFile);
		}
		if (file.exists()) {
			FileReader reader = new FileReader(file);
			properties.load(reader);
			PadoUtil.getPadoProperties().putAll(properties);
			reader.close();
		}
	}

	private void initPlugins()
	{
		// Include all plug-ins in the class path
		String pluginDir = PadoUtil.getProperty(Constants.PROP_PLUGINS_DIR);
		if (pluginDir == null) {
			String home = PadoUtil.getProperty(Constants.PROP_HOME_DIR);
			pluginDir = home + "/plugins";
		}

		// Remove the lock file first
		File lock = new File(pluginDir + "/.lock");
		try {
			lock.delete();
		} catch (Exception ex) {
			Logger.warning(
					"Unable to delete the plugins lock file during startup. You may need to delete it manually if jar files need to be deployed during run time. File path: "
							+ lock.getAbsolutePath(), ex);
		}
		// Defer initialization of IBiz classes till the persistent regions
		// have been fully loaded (isInitBiz=false)
		SystemClassPathManager.addJarsInDir(pluginDir, false /* isInitBiz */);
	}

	private void initDb()
	{
		String dbDir = PadoUtil.getProperty(Constants.PROP_DB_DIR);
		if (dbDir == null) {
			String home = PadoUtil.getProperty(Constants.PROP_HOME_DIR);
			dbDir = home + "/db";
		}
		// Remove the lock file first
		File lock = new File(dbDir + "/.lock");
		try {
			lock.delete();
		} catch (Exception ex) {
			CacheFactory
					.getAnyInstance()
					.getLogger()
					.warning(
							"Unable to delete the db lock file during startup. You may need to delete it manually if jar files need to be deployed during run time. File path: "
									+ lock.getAbsolutePath(), ex);
		}
		KeyTypeManager.resetDb(dbDir);
	}
}