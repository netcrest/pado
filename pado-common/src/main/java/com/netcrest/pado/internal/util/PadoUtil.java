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
package com.netcrest.pado.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.joda.time.DateTime;

import com.netcrest.pado.exception.EncryptionException;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.security.AESCipher;
import com.netcrest.pado.io.ObjectSerializer;
import com.netcrest.pado.log.Logger;

/**
 * PadoUtil provides Pado specific convenience static methods.
 * 
 * @author dpark
 * 
 */
public class PadoUtil
{
	private static Properties padoProperties = new Properties();

	static {
		// Default values for clients
		padoProperties.setProperty(Constants.PROP_SECURITY_AES_USER_CERTIFICATE, "security/user.cer");
		padoProperties.setProperty(Constants.PROP_SECURITY_AES_KEY_SIZE, "128");

		try {
			initialize();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private static void initialize() throws IOException
	{
		// Load properties files in the following order. One that follows
		// overwrites the previous ones.
		// 1. Load etc/plugins-gemfire.properties as resource -- the name is
		// fixed and cannot be changed
		// 2. Load etc/pado.properties as resource -- the name is fixed and
		// cannot be changed.
		// 3. Load etc/plugins.properties as file -- the name is fixed and
		// cannot be changed.
		// 4. Load etc/pado.properties as file-- the name is fixed and
		// cannot be changed.
		// 5. Load etc/<grid-id>/plugins.properties as file
		// 6. Load etc/<grid-id>/pado.properties as file
		// 7. Load system properties

		String etcDir = PadoUtil.getProperty(Constants.PROP_ETC_DIR);
		if (etcDir == null) {
			padoProperties.setProperty(Constants.PROP_ETC_DIR, Constants.DEFAULT_ETC_DIR);
		}

		// No default dir for grid. Specified via Pado scripts.
		String etcGridDir = PadoUtil.getProperty(Constants.PROP_ETC_GRID_DIR);

		String dbDir = PadoUtil.getProperty(Constants.PROP_DB_DIR);
		if (dbDir == null) {
			padoProperties.setProperty(Constants.PROP_DB_DIR, Constants.DEFAULT_DB_DIR);
		}

		// 1. Load plugins-gemfire.properties deployed as resource.
		loadResourceProperties("plugins-gemfire.properties");

		// 2. Load pado.properties deployed as resource.
		loadResourceProperties(Constants.DEFAULT_PADO_PROPERTY_FILE_NAME);

		// 3. Load the default plugins.properties - overwrites default values
		loadProperties(null, Constants.DEFAULT_PADO_PLUGINS_PROPERTY_FILE_NAME, etcDir);

		// 4. Load the default pado.properties - overwrites default values
		loadProperties(null, Constants.DEFAULT_PADO_PROPERTY_FILE_NAME, etcDir);

		// 5. Load user specified plugins.properties - overwrites the default
		// plugins.properties
		loadProperties(Constants.PROP_PADO_PLUGINS_PROPERTY_FILE, Constants.DEFAULT_PADO_PLUGINS_PROPERTY_FILE_NAME,
				etcGridDir);

		// 6. Load pado.properties - overwrites plugin.properties
		loadProperties(Constants.PROP_PADO_PROPERTY_FILE, Constants.DEFAULT_PADO_PROPERTY_FILE_NAME, etcGridDir);

		// 7. System property. If not defined then working directory
		loadProperties(null, PadoUtil.getSystemProperty(Constants.PROP_PADO_PROPERTY_FILE,
				Constants.DEFAULT_PADO_PLUGINS_PROPERTY_FILE_NAME), null);
	}

	private static void loadResourceProperties(String propertiesFileName) throws IOException
	{
		Properties properties = new Properties();
		URL url = PadoUtil.class.getResource(propertiesFileName);
		if (url != null) {
			InputStream is = null;
			try {
				is = url.openStream();
				properties.load(is);
				PadoUtil.getPadoProperties().putAll(properties);
			} catch (IOException ex) {
				// ignore
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ex) {
						// ignore
					}
				}
			}
		}
	}

	private static void loadProperties(String propPropertiesFile, String propDefaultFileName, String dirPath)
			throws IOException
	{
		Properties properties = new Properties();
		String propertiesFile = PadoUtil.getProperty(propPropertiesFile);

		String codeBaseURLStr = getProperty("codeBaseURL");
		if (codeBaseURLStr == null) {
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
		} else {
			URL url = null;
			if (propertiesFile == null) {
				if (propDefaultFileName != null) {
					if (dirPath == null) {
						url = new URL(codeBaseURLStr + "/" + propDefaultFileName);
					} else {
						url = new URL(codeBaseURLStr + "/" + dirPath + "/" + propDefaultFileName);
					}
				}
			} else {
				url = new URL(codeBaseURLStr + "/" + propertiesFile);
			}
			if (url != null) {
				InputStream is = null;
				try {
					is = url.openStream();
					properties.load(is);
					PadoUtil.getPadoProperties().putAll(properties);
				} catch (IOException ex) {
					// ignore
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException ex) {
							// ignore
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the system property name of the specified property. If the
	 * specified property is null then it returns null.
	 * 
	 * @param prop
	 *            The property to convert to the system property.
	 */
	public static String getSystemPropertyName(String prop)
	{
		if (prop == null) {
			return null;
		}
		return Constants.PROP_SYSTEM_PADO + prop;
	}

	public static String getJnlpSystemPropertyName(String prop)
	{
		if (prop == null) {
			return null;
		}
		return Constants.PROP_JNLP_SYSTEM_PADO + prop;
	}

	/**
	 * Returns the Pado system property value if found, otherwise, it returns
	 * the specified default value. Note that the system property is prefixed
	 * with {@link Constants#PROP_SYSTEM_PADO}. For JNLP apps, the system
	 * property is prefixed with {@link Constants#PROP_JNLP_PADO}
	 * 
	 * @param prop
	 *            Property name. The Pado system property is automatically
	 *            prefixed with "pado."
	 * @param defaultValue
	 *            The default property value
	 */
	public static String getSystemProperty(String prop, String defaultValue)
	{
		if (prop == null) {
			return null;
		}
		return System.getProperty(getSystemPropertyName(prop), defaultValue);
	}

	/**
	 * Returns the Pado property value by searching in the order of system,
	 * Pado, and the specified default value. Note that the system property is
	 * prefixed with {@link Constants#PROP_SYSTEM_PADO}. For JNLP apps, the
	 * system property is prefixed with {@link Constants#PROP_JNLP_PADO}
	 * 
	 * @param prop
	 *            Property
	 * @param defaultValue
	 *            The default property value
	 */
	public static String getProperty(String prop, String defaultValue)
	{
		return getProperty(padoProperties, prop, defaultValue);
	}

	/**
	 * Returns the boolean property value.
	 * 
	 * @param prop
	 *            Pado property
	 * @param defaultValue
	 *            Default boolean value.
	 */
	public static boolean getBoolean(String prop, boolean defaultValue)
	{
		String value = getProperty(prop, Boolean.toString(defaultValue));
		return Boolean.parseBoolean(value);
	}
	
	/**
	 * Returns the int property value.
	 * 
	 * @param prop
	 *            Pado property
	 * @param defaultValue
	 *            Default int value.
	 */
	public static int getInteger(String prop, int defaultValue)
	{
		String value = getProperty(prop);
		if (value == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(value);
		}
	}

	/**
	 * Returns the Pado property. The system properties always override Pado
	 * properties. It returns the system property value if defined.
	 * 
	 * @param prop
	 *            A Pado property name
	 */
	public static String getProperty(String prop)
	{
		if (prop == null) {
			return null;
		}
		String value = getProperty(prop, null);
		if (value == null) {
			value = padoProperties.getProperty(prop);
		}
		return value;
	}

	/**
	 * Loads and returns the class of the class name mapped by the specified
	 * property.
	 * 
	 * @param prop
	 *            The property that maps a class name
	 * @param defaultClassName
	 *            The default class name if the property value is not found.
	 * @throws ClassNotFoundException
	 *             Thrown if the the class is not found.
	 */
	public static Class<?> getClass(String prop, String defaultClassName) throws ClassNotFoundException
	{
		return Class.forName(getProperty(prop, defaultClassName));
	}

	/**
	 * Loads and returns the class of the class name mapped by the specified
	 * property.
	 * 
	 * @param prop
	 *            The property that maps a class name
	 * @throws ClassNotFoundException
	 *             Thrown if the the class is not found.
	 */
	public static Class<?> getClass(String prop) throws ClassNotFoundException
	{
		return getClass(prop, null);
	}

	/**
	 * Returns true if the specified property has the value of "true". The value
	 * is case-insensitive.
	 * 
	 * @param prop
	 *            Property
	 */
	public static boolean isProperty(String prop)
	{
		String value = getProperty(prop);
		return value != null && value.equalsIgnoreCase("true") ? true : false;
	}

	/**
	 * Returns the property value by first checking the system property. If the
	 * system property exists then it returns its value, otherwise, it returns
	 * the property value found in the specified properties. Returns the
	 * specified default value if not found. Note that the system property is
	 * prefixed with {@link Constants#PROP_SYSTEM_PADO}. For JNLP apps, the
	 * system property is prefixed with {@link Constants#PROP_JNLP_PADO}
	 * 
	 * @param props
	 *            Properties to look up
	 * @param prop
	 *            Property
	 * @param defaultValue
	 *            The default value if not found.
	 */
	public static String getProperty(Properties props, String prop, String defaultValue)
	{
		if (prop == null) {
			return null;
		}
		String val = System.getProperty(getSystemPropertyName(prop));
		if (val == null) {
			val = System.getProperty(getJnlpSystemPropertyName(prop));
		}

		if (val == null) {
			if (props == null) {
				val = defaultValue;
			} else {
				val = props.getProperty(prop, defaultValue);
			}
		}
		return val;
	}

	/**
	 * Analogous to {@link #getProperty(Properties, String, String)} except that
	 * the system property is set with the value if it does not exist.
	 * 
	 * @param props
	 *            Properties to look up
	 * @param prop
	 *            Property
	 * @param defaultValue
	 *            The default value if not found.
	 */
	public static String getPropertyAndSetSystemPropertyIfNotFound(Properties props, String prop, String defaultValue)
	{
		if (prop == null) {
			return null;
		}
		String val = System.getProperty(Constants.PROP_SYSTEM_PADO + prop);
		if (val == null) {
			if (props == null) {
				val = defaultValue;
			} else {
				val = props.getProperty(prop, defaultValue);
			}
			System.setProperty(Constants.PROP_SYSTEM_PADO + prop, val);
		}
		return val;
	}

	/**
	 * Returns the underlying properties that hold all Pado specific properties.
	 */
	public static Properties getPadoProperties()
	{
		return padoProperties;
	}

	/**
	 * Returns the child path without the root path. The returned path does not
	 * begin with "/". It returns an empty string if fullPath contains only the
	 * root path. It returns null if fullPath is null.
	 * 
	 * @param fullPath
	 *            Absolute path that includes grid root path.
	 */
	public static String getChildPath(String fullPath)
	{
		if (fullPath == null) {
			return null;
		}
		String childPath = fullPath.replaceFirst("^?/.*?/", "");
		if (childPath.startsWith("/")) {
			childPath = "";
		}
		return childPath;
	}

	/**
	 * Returns the server ID. It returns null if it is a pure client
	 * application.
	 */
	public static String getServerId()
	{
		return getProperty(Constants.PROP_SERVER_ID);
	}
	
	/**
	 * Returns true if this VM is a pure client, i.e., has no server-side
	 * Pado mechanics enabled. Note that a server can act as both client 
	 * and server.
	 */
	public static boolean isPureClient()
	{
		return getServerId() == null;
	}

	/**
	 * Inflates (decompresses) the specified compressed data.
	 * 
	 * @param compressedData
	 *            Compressed (zipped) data
	 * @return Byte array of uncompressed data
	 * @throws DataFormatException
	 *             Thrown if the compressed data is malformed
	 * @throws IOException
	 *             Thrown if a stream error occurs
	 */
	public static byte[] inflate(byte[] compressedData) throws DataFormatException, IOException
	{
		Inflater decompressor = new Inflater();
		decompressor.setInput(compressedData);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
		byte[] buf = new byte[20000];
		while (!decompressor.finished()) {
			int count = decompressor.inflate(buf);
			bos.write(buf, 0, count);
		}
		bos.close();
		byte[] decompressedData = bos.toByteArray();
		return decompressedData;
	}

	/**
	 * Deflates (compresses) the specified text.
	 * 
	 * @param text
	 *            Text to encode
	 * @param encode
	 *            Encode type. If null, "UTF-8".
	 * @return Byte array of compressed data
	 * @throws IOException
	 *             Thrown if a stream error occurs
	 */
	public static byte[] deflate(String text, String encode) throws IOException
	{
		if (encode == null) {
			encode = "utf-8";
		}
		byte[] input = text.getBytes(encode);
		Deflater deflater = new Deflater();
		deflater.setInput(input);
		deflater.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		byte[] buf = new byte[20000];
		while (!deflater.finished()) {
			int bytesCompressed = deflater.deflate(buf);
			bos.write(buf, 0, bytesCompressed);
		}
		bos.close();
		byte[] compressedData = bos.toByteArray();
		return compressedData;
	}

	/**
	 * Returns the class type that matches the specified type name. It returns
	 * null if the matching type is not found or supported. The supported types
	 * are
	 * <ul>
	 * <li>primitives and primitive wrappers</li>
	 * <li>java.lang.String</li>
	 * <li>java.util.Date</li>
	 * <li>jorg.joda.time.DateTime</li>
	 * </ul>
	 * 
	 * @param typeName
	 *            Type name.
	 */
	public static Class getType(String typeName)
	{
		if (typeName.equals("Boolean")) {
			return Boolean.class;
		} else if (typeName.equalsIgnoreCase("boolean")) {
			return boolean.class;
		} else if (typeName.equals("Byte")) {
			return Byte.class;
		} else if (typeName.equalsIgnoreCase("byte")) {
			return byte.class;
		} else if (typeName.equals("Character")) {
			return Character.class;
		} else if (typeName.equalsIgnoreCase("char")) {
			return char.class;
		} else if (typeName.equals("Integer")) {
			return Integer.class;
		} else if (typeName.equalsIgnoreCase("int")) {
			return int.class;
		} else if (typeName.equals("Short")) {
			return Short.class;
		} else if (typeName.equalsIgnoreCase("short")) {
			return short.class;
		} else if (typeName.equals("Long")) {
			return Long.class;
		} else if (typeName.equalsIgnoreCase("long")) {
			return long.class;
		} else if (typeName.equals("Float")) {
			return Float.class;
		} else if (typeName.equalsIgnoreCase("float")) {
			return float.class;
		} else if (typeName.equals("Double")) {
			return Double.class;
		} else if (typeName.equalsIgnoreCase("double")) {
			return double.class;
		} else if (typeName.equalsIgnoreCase("String")) {
			return String.class;
		} else if (typeName.equalsIgnoreCase("Date")) {
			return Date.class;
		} else if (typeName.equalsIgnoreCase("DateTime")) {
			return DateTime.class;
		} else {
			return null;
		}
	}

	public static byte[] encryptObject(Object object) throws EncryptionException
	{
		try {
			byte[] blob = ObjectSerializer.serialize(object);
			return AESCipher.encryptBinaryToBinary(blob);
		} catch (Exception ex) {
			String className;
			if (object != null) {
				className = object.getClass().getName();
			} else {
				className = "null";
			}
			throw new EncryptionException("Failed to encrypt " + className, ex);
		}
	}

	public static Object decryptObject(byte[] encryptedBlob) throws EncryptionException
	{
		try {
			Object obj = null;
			byte[] blob = AESCipher.decryptBinaryToBinary(encryptedBlob);
			if (blob != null) {
				obj = ObjectSerializer.deserialize(blob);
			}
			return obj;
		} catch (Exception ex) {
			throw new EncryptionException("Failed to decrypt a binary", ex);
		}
	}

}
