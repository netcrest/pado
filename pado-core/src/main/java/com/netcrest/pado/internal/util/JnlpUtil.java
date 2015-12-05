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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

/**
 * JnlpUtil provides JNLP specific services such as reading and writing local
 * files via JNLP's persistence service. In order enable JnlpUtil, JNLP client
 * applications must set the code-base URL during startup by invoking
 * {@link #setCodeBaseURL(URL)}.
 * 
 * @author dpark
 * 
 */
public class JnlpUtil
{
	/**
	 * Code-base URL. Maybe null if not set by the application.
	 */
	private static URL codeBaseURL;

	/**
	 * Returns true if the code-base URL is set.
	 */
	public final static boolean isJnlpClient()
	{
		return codeBaseURL != null;
	}

	/**
	 * Returns the code-base URL set by the JNLP client during startup. It
	 * returns null if the code base is not set by the client. It is the client
	 * application's responsibility to set the code base during its startup by
	 * invoking {@link #setCodeBaseURL(URL)}.
	 */
	public final static URL getCodeBaseURL()
	{
		return codeBaseURL;
	}

	/**
	 * Sets the code-base URL. This method must be invoked by the JNLP client
	 * during startup.
	 * 
	 * @param codeBaseURL
	 *            Code-base URL
	 */
	public final static void setCodeBaseURL(URL codeBaseURL)
	{
		JnlpUtil.codeBaseURL = codeBaseURL;
	}

	/**
	 * Returns the JNLP persistence service.
	 * 
	 * @throws UnavailableServiceException
	 *             Thrown if the JNLP persistence service is not available.
	 */
	public final static PersistenceService getPersistenceService() throws UnavailableServiceException
	{
		return (PersistenceService) ServiceManager.lookup("javax.jnlp.PersistenceService");
	}

	/**
	 * Returns a FileContents object that provides JNLP file operations for the
	 * specified URL.
	 * 
	 * @param url
	 *            URL to the local file managed by the JNLP persistence service.
	 * @return null if the code base is not specified. See
	 *         {@link #getCodeBaseURL()}.
	 * @throws FileNotFoundException
	 *             Thrown if the specified file path does not exist.
	 * @throws IOException
	 *             Thrown if the file cannot be opened.
	 * @throws UnavailableServiceException
	 *             Thrown if the JNLP persistence service is not available.
	 */
	public final static FileContents getFileContents(URL url) throws FileNotFoundException, IOException,
			UnavailableServiceException
	{
		PersistenceService ps = (PersistenceService) ServiceManager.lookup("javax.jnlp.PersistenceService");
		return ps.get(url);
	}

	/**
	 * Returns a FileContents object that provides JNLP file operations for the
	 * specified file path.
	 * 
	 * @param filePath
	 *            File path relative to the code base.
	 * @return null if the code base is not specified. See
	 *         {@link #getCodeBaseURL()}.
	 * @throws FileNotFoundException
	 *             Thrown if the specified file path does not exist.
	 * @throws IOException
	 *             Thrown if the file cannot be opened.
	 * @throws UnavailableServiceException
	 *             Thrown if the JNLP persistence service is not available.
	 */
	public final static FileContents getFileContents(String filePath) throws FileNotFoundException, IOException,
			UnavailableServiceException
	{
		URL codeBaseURL = getCodeBaseURL();
		if (codeBaseURL == null) {
			return null;
		} else {
			URL url = new URL(codeBaseURL, filePath);
			return getFileContents(url);
		}
	}

	/**
	 * Returns an InputStream to the specified file path.
	 * 
	 * @param filePath
	 *            File path relative to the code base.
	 * @return null if the code base is not specified. See
	 *         {@link #getCodeBaseURL()}.
	 * @throws FileNotFoundException
	 *             Thrown if the specified file path does not exist.
	 * @throws IOException
	 *             Thrown if the file cannot be opened.
	 * @throws UnavailableServiceException
	 *             Thrown if the JNLP persistence service is not available.
	 */
	public final static InputStream getFileInputStream(String filePath) throws FileNotFoundException, IOException,
			UnavailableServiceException
	{
		FileContents fc = getFileContents(filePath);
		if (fc == null) {
			return null;
		} else {
			return fc.getInputStream();
		}
	}

	/**
	 * Returns an OutputStream to the specified file path.
	 * 
	 * @param filePath
	 *            File path relative to the code base.
	 * @param overwrite
	 *            true to overwrite the file.
	 * @return null if the code base is not specified. See
	 *         {@link #getCodeBaseURL()}.
	 * @throws FileNotFoundException
	 *             Thrown if the specified file path does not exist.
	 * @throws IOException
	 *             Thrown if the file cannot be opened.
	 * @throws UnavailableServiceException
	 *             Thrown if the JNLP persistence service is not available.
	 */
	public final static OutputStream getFileOutputStream(String filePath, boolean overwrite)
			throws FileNotFoundException, IOException, UnavailableServiceException
	{
		FileContents fc = getFileContents(filePath);
		if (fc == null) {
			return null;
		} else {
			return fc.getOutputStream(overwrite);
		}
	}

	/**
	 * Reads the entire data from the specified input stream.
	 * 
	 * @return null if the specified input stream is null.
	 * @param is
	 *            Input stream
	 * @throws IOException
	 *             Thrown if a read error occurs.
	 */
	public static byte[] read(InputStream is) throws IOException
	{
		if (is == null) {
			return null;
		}
		try {
			// Read 10000 bytes at a time
			int bufferSize = 10000; // 10 KB
			byte[] buffer = new byte[bufferSize];
			byte[] data = new byte[0];
			int size;
			int pos = 0;
			while ((size = is.read(buffer, 0, bufferSize)) != -1) {
				byte newData[] = new byte[data.length + size];
				System.arraycopy(data, 0, newData, 0, data.length);
				pos = data.length;
				System.arraycopy(buffer, 0, newData, pos, size);
				data = newData;
			}
			return data;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * Reads the specified file.
	 * 
	 * @return null if the specified input stream is null.
	 * @param filePath
	 *            File path relative to the code base.
	 * @return null if the code base is not specified. See
	 *         {@link #getCodeBaseURL()}.
	 * @throws FileNotFoundException
	 *             Thrown if the specified file path does not exist.
	 * @throws IOException
	 *             Thrown if the file cannot be opened or a read error occurs.
	 * @throws UnavailableServiceException
	 *             Thrown if the JNLP persistence service is not available.
	 */
	public final static byte[] read(String filePath) throws FileNotFoundException, IOException,
			UnavailableServiceException
	{
		InputStream is = getFileInputStream(filePath);
		if (is == null) {
			return null;
		} else {
			return read(is);
		}
	}

	/**
	 * Reads the specified file in String form. The file should contain text
	 * data.
	 * 
	 * @return null if the specified input stream is null.
	 * @param filePath
	 *            File path relative to the code base.
	 * @return null if the code base is not specified. See
	 *         {@link #getCodeBaseURL()}.
	 * @throws FileNotFoundException
	 *             Thrown if the specified file path does not exist.
	 * @throws IOException
	 *             Thrown if the file cannot be opened or a read error occurs.
	 * @throws UnavailableServiceException
	 *             Thrown if the JNLP persistence service is not available.
	 */
	public final static String readString(String filePath) throws FileNotFoundException, IOException,
			UnavailableServiceException
	{
		byte[] data = read(filePath);
		if (data == null) {
			return null;
		}
		return new String(data);
	}
}
