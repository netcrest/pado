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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.internal.factory.BizManagerFactory;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

/**
 * SystemClassPathManager assigns the class path to the system class loader in
 * runtime.
 * 
 * @author dpark
 * 
 */
public class SystemClassPathManager
{
	private static final Class[] parameters = new Class[] { URL.class };

	public static void addFile(String s) throws IOException
	{
		File f = new File(s);
		addFile(f);
	}

	public static void addFile(File f) throws IOException
	{
		addURL(f.toURI().toURL());
	}

	public static void addURL(URL u) throws IOException
	{

		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}

	}

	/**
	 * Includes all of the jar files in the specified directory in the class
	 * path. It first includes the latest dated jar files that end with the
	 * extension '.vyyyyMMddHHmm.jar'. For example, if there are
	 * 'foo.v201010231217' and 'foo.v201010221011' then only the formal is added
	 * in the class path since it has the latest version date.
	 * <p>
	 * Once all of the date-versioned jar files are added, it then proceed to
	 * add the rest of the jar files in sorted order.
	 * <p>
	 * It also auto registers versioned classes such as KeyMap's KeyType.
	 * 
	 * @param dirPath
	 *            The absolute or relative directory path.
	 * @param isInitBiz
	 *            true to initialize IBiz classes, false to defer
	 *            initialization.
	 */
	public static void addJarsInDir(String dirPath, boolean isInitBiz)
	{
		if (dirPath == null) {
			return;
		}

		File classDirFile = new File(dirPath);
		classDirFile.mkdirs();

		ArrayList<String> jarList = new ArrayList();
		File[] files = classDirFile.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				String fileName = file.getName();
				jarList.add(file.getAbsolutePath());
			}
		}

		// Register the latest files only
		Collections.sort(jarList);
		String prevFileNameNoDate = "";
		ArrayList<File> datedFiles = new ArrayList();
		ArrayList<File> undatedFiles = new ArrayList();
		for (int i = jarList.size() - 1; i >= 0; i--) {
			String filePath = jarList.get(i);
			if (filePath.endsWith(".jar") == false) {
				continue;
			}
			File file = new File(filePath);
			String fileName = file.getName();
			String nameNoExtension = fileName.substring(0, fileName.lastIndexOf(".jar"));
			int index = nameNoExtension.lastIndexOf(".v");
			if (index == -1) {
				// not dated
				undatedFiles.add(file);
				continue;
			}
			String fileNameNoDate = nameNoExtension.substring(0, index);
			if (fileNameNoDate.equals(prevFileNameNoDate) == false) {
				try {
					SystemClassPathManager.addFile(file);
					datedFiles.add(file);
				} catch (IOException e) {
					Logger.error(e);
				}
				prevFileNameNoDate = fileNameNoDate;
			}
		}

		// Add the un-dated files - dated files take precedence
		Collections.sort(undatedFiles);
		for (File file : undatedFiles) {
			try {
				SystemClassPathManager.addFile(file);
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		// Log
		StringBuffer buffer = new StringBuffer(1024);
		for (File file : undatedFiles) {
			buffer.append(file.getName());
			buffer.append(", ");
		}
		for (File file : datedFiles) {
			buffer.append(file.getName());
			buffer.append(", ");
		}
		String fileInfo = null;
		if (buffer.length() > 0) {
			// remove trailing ", "
			fileInfo = buffer.substring(0, buffer.length() - 2);
		}

		Logger.config("Jars loaded by the system classs loader (" + classDirFile.getAbsolutePath() + "): " + fileInfo);

		List<File> allFiles = new ArrayList<File>(datedFiles);
		allFiles.addAll(undatedFiles);
		
		// Register KeyTypes
		registerKeyType(allFiles);

		// Register IBiz classes
		registerBizClasses(allFiles, isInitBiz);

		// for gc
		datedFiles.clear();
		datedFiles = null;
		undatedFiles.clear();
		undatedFiles = null;
		jarList.clear();
		jarList = null;
	}

	public static void registerKeyType(List<File> files)
	{
		if (files == null || files.size() == 0) {
			return;
		}
		StringBuffer buffer = new StringBuffer(200);
		for (File file : files) {
			try {
				buffer.delete(0, buffer.length());
				Class[] classes = ClassFinder.getAllClasses(file.getAbsolutePath());
				for (int j = 0; j < classes.length; j++) {
					Class cls = classes[j];
					if (KeyType.class.isAssignableFrom(cls) && cls.getSimpleName().matches(".*_v\\d++$")) {
						try {
							Method method = cls.getMethod("getKeyType", null);
							KeyType keyType = (KeyType) method.invoke(cls, null);
							KeyTypeManager.registerSingleKeyType(keyType);
							buffer.append(cls.getName() + ", ");
						} catch (Exception ex) {
							// ignore
						}
					}
				}
				if (buffer.length() > 0) {
					buffer.replace(buffer.length() - 2, buffer.length(), "");
					Logger.config("Registered the following KeyType classes: "
							+ buffer.toString());

				}
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void registerBizClasses(List<File> files, boolean isInitBiz)
	{
		if (files == null || files.size() == 0) {
			return;
		}
		URL urls[] = new URL[files.size()];
		try {
			for (int i = 0; i < urls.length; i++) {
				urls[i] = files.get(i).toURI().toURL();
			}
			HotDeploymentBizClasses hotDeployment = new HotDeploymentBizClasses(urls, "com io net org mypado");

			if (hotDeployment.isBizFound()) {
				StringBuffer buffer1 = new StringBuffer(2000);
				StringBuffer buffer2 = new StringBuffer(2000);
				String bizNames[] = hotDeployment.getBizNames();
				InvertedClassLoader classLoader = new InvertedClassLoader(urls);
				int i = 0;
				for (File file : files) {
					if (i++ > 0) {
						buffer1.append(", ");
					}
					buffer1.append(file.getName());
				}
				i = 0;
				for (String bizName : bizNames) {
					if (i++ > 0) {
						buffer2.append(", ");
					}
					Class cls = classLoader.loadClass(bizName);
					BizManager<BizClass> mgr = BizManagerFactory.getBizManagerFactory().createBizManager(cls, false);
					if (isInitBiz) {
						mgr.init();
					}
					PadoServerManager.getPadoServerManager().addAppBizManager(mgr);
					buffer2.append(cls.getName());
				}
				if (buffer2.length() > 0) {
					String header;
					if (isInitBiz) {
						header = "Registered (and initialized)";
					} else {
						header = "Registered (but not initialized - deferred)";
					}
					try {
						Logger.config("IBiz plugins read: "
								+ buffer1.toString() + ". " + header + " the following IBiz classes: " + buffer2.toString());
					} catch (Exception ex) {
						System.out
								.println("IBiz plugins read: "
										+ buffer1.toString() + ". " + header + " the following IBiz classes: "
										+ buffer2.toString());
					}
				}
			}

		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}
}
