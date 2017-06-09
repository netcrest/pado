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
package com.netcrest.pado.biz.mon.impl.gemfire;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.biz.gemfire.proxy.GemfireBizManager;
import com.netcrest.pado.biz.mon.DeploymentFailedException;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.gemfire.GemfirePadoServerInitializer;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.HotDeploymentBizClasses;
import com.netcrest.pado.internal.util.InvertedClassLoader;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.internal.util.SystemClassPathManager;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

public class Deployment
{
	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmm");
	// unit in msec
	private final static int FILE_LOCK_WAIT_TIME_IN_MSEC = Integer.getInteger("pado.delploy.fileLockWaitTimeInMsec", 10000); 
	// unit in msec
	private final static int FILE_LOCK_READ_WAKEUP_INTERVAL_IN_MSEC = 100; 

	/**
	 * Deploys the jar files in the server. The file locking logic implemented
	 * in this method has a couple of holes but should work for most cases.
	 * <ul>
	 * <li>It does not handle deadlock situations well. It simply overwrites the
	 * files if a deadlock occurs.</li>
	 * <li>The lock file is removed during startup by
	 * {@link GemfirePadoServerInitializer}. This means
	 * it could be removed while another server still has the lock. If this
	 * occurs, the above bullet takes place.</li>
	 * </ul>
	 */
	public HotDeploymentBizClasses save(String[] jarNames, byte[][] jarContents, Date timestamp) throws DeploymentFailedException
	{
		String codeMessage = null;

		byte byteBuffers[][] = jarContents;
		Date dateSuffix = timestamp;

		String home = PadoUtil.getProperty(Constants.PROP_HOME_DIR);
		String tmpClassDir = home + "/plugins/tmp";
		File tmpClassDirFile = new File(tmpClassDir);
		File lock = new File(tmpClassDir + "/.lock");
		String hotClassDir = home + "/plugins";
		File hotClassDirFile = new File(hotClassDir);
		boolean deadlockOccurred = false;
		
		HotDeploymentBizClasses hotDeployment = null;
		try {
			tmpClassDirFile.mkdirs();
			hotClassDirFile.mkdirs();
			boolean lockCreated = lock.createNewFile();
			boolean writeOK = lockCreated;
			// if dateSuffix is not given then we must use the system time
			// which could be different from server to server. Having different
			// suffix means different file names and therefore, they must be
			// written.
			if (dateSuffix == null) {
				writeOK = true;
			}

			// Because multiple servers could potentially write the
			// files in the same file system, we must wait till the lock is
			// released by the first server that obtained it. Note that it
			// is possible that the lock may not be released by the first
			// server due to a system failure (termination, etc.).
			// We take care of the dead lock situation by timing out
			// after FILE_LOCK_WAIT_TIME msec.
			if (writeOK == false) {
				boolean lockExists = lock.exists();
				int sleptTime = 0;
				while (lockExists && sleptTime < FILE_LOCK_WAIT_TIME_IN_MSEC) {
					Thread.sleep(FILE_LOCK_READ_WAKEUP_INTERVAL_IN_MSEC);
					sleptTime += FILE_LOCK_READ_WAKEUP_INTERVAL_IN_MSEC;
					lockExists = lock.exists();
				}
				if (lock.exists()) {
					// something is not right. either another server is
					// taking too long to write the jar files or
					// it terminated without removing the lock file.
					// Log it and let's just write the files.
					Logger.warning("Potential jar write deadlock occurred. "
							+ "The file lock has not been released by another server after "
							+ (int) (FILE_LOCK_WAIT_TIME_IN_MSEC / 1000)
							+ " sec. Releasing lock. Deployed jar files will be overwritten.");
					// writeOK = true;
					deadlockOccurred = true;
				}
				writeOK = true;
			}

			// Get dated file paths
			File datedFiles[] = new File[byteBuffers.length];
			URL datedUrls[] = new URL[datedFiles.length];
			for (int i = 0; i < byteBuffers.length; i++) {
				String filePath = tmpClassDir + "/" + getDatedJarName(jarNames[i], dateSuffix);
				datedFiles[i] = new File(filePath);
				datedUrls[i] = datedFiles[i].toURI().toURL();
			}

			// Write all jar files to the file system (tmp dir)
			if (writeOK) {
				for (int i = 0; i < byteBuffers.length; i++) {
					FileOutputStream fos = new FileOutputStream(datedFiles[i]);
					fos.write(byteBuffers[i]);
					fos.close();
				}
			}
			hotDeployment = new HotDeploymentBizClasses(datedUrls, "com io net org mypado");

//			if (hotDeployment.isKeyTypeSuffixFound() && hotDeployment.isBizFound()) {
//
//				// Error - KeyType suffixed classes and IBiz classes cannot be
//				// co-loaded by a new class loader. KeyType suffixed classes 
//				// must always be loaded by the system class loader.
//				codeMessage = "Specified jars contain both KeyType suffixed classes and IBiz classes. "
//						+ "Both types are not allowed in the same class loader. "
//						+ "To deploy IBiz classes, jars must only include biz classes and their dependent classes, "
//						+ "which must not include KeyType classes with the suffix '_v#'. "
//						+ "Jars must always include all of the main KeyType classes, however.";
//				throw new DeploymentFailedException(codeMessage);
//
//			} else 
				if (hotDeployment.isKeyTypesFound() == false && hotDeployment.isBizFound() == false) {

				codeMessage = "Deployment cancelled. The jar files do not contain KeyType or biz classes.";
				throw new DeploymentFailedException(codeMessage);

			} else {

				// Move the files to the hot plug-in dir.
				// Write to the hot dir then delete from the tmp dir
				File tmpDatedFiles[] = datedFiles;
				datedFiles = new File[tmpDatedFiles.length];
				for (int i = 0; i < tmpDatedFiles.length; i++) {
					datedFiles[i] = new File(hotClassDirFile, tmpDatedFiles[i].getName());
					datedUrls[i] = datedFiles[i].toURI().toURL();
				}
				// Write all jar files to the file system (hot dir)
				if (writeOK) {
					for (int i = 0; i < byteBuffers.length; i++) {
						FileOutputStream fos = new FileOutputStream(datedFiles[i]);
						fos.write(byteBuffers[i]);
						fos.close();
					}
				}
				// Delete the tmp files
				for (int i = 0; i < tmpDatedFiles.length; i++) {
					boolean deleted = tmpDatedFiles[i].delete();
				}

//				// All KeyType classes must be loaded by the system class
//				// loader;
//				List<File> datedFileList = Arrays.asList(datedFiles);
//				SystemClassPathManager.registerKeyType(datedFileList);
//
//				// All IBiz classes must be loaded by InvertedClassLoader
//				// Include all IBiz jars
//				SystemClassPathManager.registerBizClasses(datedFileList);
				
				hotDeployment = SystemClassPathManager.addJarsInDir(hotClassDir, true);

				codeMessage = "Deployed to " + tmpClassDirFile.getCanonicalPath();
				if (deadlockOccurred) {
					codeMessage = codeMessage
							+ " -- Deadlock occurred. Files may have been overwritten in the same file system. Please see server log files for details.";
					throw new DeploymentFailedException(codeMessage);
				}
				
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < datedFiles.length; i++) {
					if (i > 0) {
						buffer.append(", ");
					}
					buffer.append(datedFiles[i].getName());
				}
				Logger.info("Hot delployed the following files: " + buffer.toString());
			}

		} catch (Exception ex) {
			
			StringBuffer buffer = new StringBuffer(100);
			buffer.append("[");
			for (int i= 0; i < jarNames.length; i++) {
				if (i == jarNames.length - 1) {
					buffer.append(jarNames[i] + "]");
				} else {
					buffer.append(jarNames[i] + ", ");
				}
			}
			
			throw new DeploymentFailedException("Deployment failed for one or more files: " + buffer.toString(),  ex);
			
		} finally {
			
			lock.delete();
			
		}
		
		return hotDeployment;
	}
	
	public void save_old(String[] jarNames, byte[][] jarContents, Date timestamp) throws DeploymentFailedException
	{
		
		String codeMessage = null;

		Cache cache = CacheFactory.getAnyInstance();

		byte byteBuffers[][] = jarContents;
		Date dateSuffix = timestamp;

		String home = PadoUtil.getProperty(Constants.PROP_HOME_DIR);
		String tmpClassDir = home + "/plugins/tmp";
		File tmpClassDirFile = new File(tmpClassDir);
		File lock = new File(tmpClassDir + "/.lock");
		String hotClassDir = home + "/plugins/hot";
		File hotClassDirFile = new File(tmpClassDir);
		boolean deadlockOccurred = false;
		try {
			tmpClassDirFile.mkdirs();
			hotClassDirFile.mkdirs();
			boolean lockCreated = lock.createNewFile();
			boolean writeOK = lockCreated;
			// if dateSuffix is not given then we must use the system time
			// which could be different from server to server. Having different
			// suffix means different file names and therefore, they must be
			// written.
			if (dateSuffix == null) {
				writeOK = true;
			}

			// Because multiple servers could potentially write the
			// files in the same file system, we must wait till the lock is
			// released by the first server that obtained it. Note that it
			// is possible that the lock may not be released by the first
			// server due to a system failure (termination, etc.).
			// We take care of the dead lock situation by timing out
			// after FILE_LOCK_WAIT_TIME msec.
			if (writeOK == false) {
				boolean lockExists = lock.exists();
				int sleptTime = 0;
				while (lockExists && sleptTime < FILE_LOCK_WAIT_TIME_IN_MSEC) {
					Thread.sleep(FILE_LOCK_READ_WAKEUP_INTERVAL_IN_MSEC);
					sleptTime += FILE_LOCK_READ_WAKEUP_INTERVAL_IN_MSEC;
					lockExists = lock.exists();
				}
				if (lock.exists()) {
					// something is not right. either another server is
					// taking too long to write the jar files or
					// it terminated without removing the lock file.
					// Log it and let's just write the files.
					Logger.warning("Potential jar write deadlock occurred. "
							+ "The file lock has not been released by another server after "
							+ (int) (FILE_LOCK_WAIT_TIME_IN_MSEC / 1000)
							+ " sec. Releasing lock. Deployed jar files will be overwritten.");
					// writeOK = true;
					deadlockOccurred = true;
				}
				writeOK = true;
			}

			// Get dated file paths
			File datedFiles[] = new File[byteBuffers.length];
			URL datedUrls[] = new URL[datedFiles.length];
			for (int i = 0; i < byteBuffers.length; i++) {
				String filePath = tmpClassDir + "/" + getDatedJarName(jarNames[i], dateSuffix);
				datedFiles[i] = new File(filePath);
				datedUrls[i] = datedFiles[i].toURI().toURL();
			}

			// Write all jar files to the file system (tmp dir)
			if (writeOK) {
				for (int i = 0; i < byteBuffers.length; i++) {
					FileOutputStream fos = new FileOutputStream(datedFiles[i]);
					fos.write(byteBuffers[i]);
					fos.close();
				}
			}
			HotDeploymentBizClasses hotDeployment = new HotDeploymentBizClasses(datedUrls, "com io net org mypado");

			if (hotDeployment.isKeyTypeSuffixFound() && hotDeployment.isBizFound()) {

				// Error - KeyType suffixed classes and IBiz classes cannot be
				// co-loaded by a new class loader. KeyType suffixed classes 
				// must always be loaded by the system class loader.
				codeMessage = "Specified jars contain both KeyType suffixed classes and IBiz classes. "
						+ "Both types are not allowed in the same class loader. "
						+ "To deploy IBiz classes, jars must only include biz classes and their dependent classes, "
						+ "which must not include KeyType classes with the suffix '_v#'. "
						+ "Jars must always include all of the main KeyType classes, however.";
				throw new DeploymentFailedException(codeMessage);

			} else if (hotDeployment.isKeyTypesFound() == false && hotDeployment.isBizFound() == false) {

				codeMessage = "Deployment cancelled. The jar files do not contain KeyType or biz classes.";
				throw new DeploymentFailedException(codeMessage);

			} else {

				// Move the files to the hot plug-in dir.
				// Write to the hot dir then delete from the tmp dir
				File tmpDatedFiles[] = datedFiles;
				for (int i = 0; i < tmpDatedFiles.length; i++) {
//					File plugInFile = new File(datedFiles[i].getParentFile().getParentFile(), datedFiles[i].getName());
					File hotFile = new File(hotClassDirFile, tmpDatedFiles[i].getName());
//					boolean deleted = tmpDatedFiles[i].delete();
					datedFiles[i] = hotFile;
					datedUrls[i] = datedFiles[i].toURI().toURL();
				}
				// Write all jar files to the file system (hot dir)
				if (writeOK) {
					for (int i = 0; i < byteBuffers.length; i++) {
						FileOutputStream fos = new FileOutputStream(datedFiles[i]);
						fos.write(byteBuffers[i]);
						fos.close();
					}
				}
				// Delete the tmp files
				for (int i = 0; i < tmpDatedFiles.length; i++) {
					boolean deleted = tmpDatedFiles[i].delete();
				}

				// All KeyType classes must be loaded by the system class
				// loader;
				StringBuffer buffer = new StringBuffer();
				if (hotDeployment.isKeyTypesFound()) {

					// Add the jars to the class path
					for (int i = 0; i < datedFiles.length; i++) {
						File file = datedFiles[i];
						SystemClassPathManager.addFile(file);
						buffer.append(file.getCanonicalPath() + ", ");
					}
					if (buffer.length() > 0) {
						buffer.replace(buffer.length() - 2, buffer.length(), "");
						Logger.config("Added the following jar file(s) in the class path: "
								+ buffer.toString());
					}

					// Register KeyTypes
					buffer.delete(0, buffer.length());
					String keyTypeNames[] = hotDeployment.getKeyTypes();
					ClassLoader classLoader = ClassLoader.getSystemClassLoader();
					for (String keyTypeName : keyTypeNames) {
						Class cls = classLoader.loadClass(keyTypeName);
						if (KeyType.class.isAssignableFrom(cls) && cls.getSimpleName().matches(".*_v\\d++$")) {
							Method method = cls.getMethod("getKeyType", null);
							KeyType fieldType = (KeyType) method.invoke(cls, null);
							KeyTypeManager.registerSingleKeyType(fieldType);
							buffer.append(cls.getName() + ", ");
						}
					}
					if (buffer.length() > 0) {
						buffer.replace(buffer.length() - 2, buffer.length(), "");
						Logger.config("Registered the following KeyMap's KeyType classes: "
								+ buffer.toString());
					}
				}

				// All IBiz classes must be loaded by InvertedClassLoader
				if (hotDeployment.isBizFound()) {

					// Bizs - All biz classes must be loaded by a new class
					// loader. It is important that the jars must contain all
					// biz dependent classes except KeyType classes. Jars must
					// contain only main KeyType classes. They must NOT include
					// any of the version suffixed KeyType classes, i.e.,
					// "_v#.class" is not allowed.
					buffer.delete(0, buffer.length());
					String bizNames[] = hotDeployment.getBizNames();
					InvertedClassLoader classLoader = new InvertedClassLoader(datedUrls);
					hotDeployment.setClassLoader(classLoader);
					PadoServerManager sm = PadoServerManager.getPadoServerManager();

					for (String bizName : bizNames) {
						Class cls = classLoader.loadClass(bizName);
						GemfireBizManager<BizClass> mgr = new GemfireBizManager<BizClass>(cls,
								false);
						mgr.init();
						sm.addAppBizManager(mgr);
						buffer.append(cls.getName() + ", ");
					}
					if (buffer.length() > 0) {
						buffer.replace(buffer.length() - 2, buffer.length(), "");
						Logger.config("Registered the following biz classes: "
								+ buffer.toString());
					}
				}

				codeMessage = "deployed to " + tmpClassDirFile.getCanonicalPath();
				if (deadlockOccurred) {
					codeMessage = codeMessage
							+ " -- Deadlock occurred. Files may have been overwritten in the same file system. Please see server log files for details.";
					throw new DeploymentFailedException(codeMessage);
				}
			}

			hotDeployment = null;

		} catch (Exception ex) {
			
			StringBuffer buffer = new StringBuffer(100);
			buffer.append("[");
			for (int i= 0; i < jarNames.length; i++) {
				if (i == jarNames.length - 1) {
					buffer.append(jarNames[i] + "]");
				} else {
					buffer.append(jarNames[i] + ", ");
				}
			}
			
			throw new DeploymentFailedException("Deployment failed for one or more files: " + buffer.toString(),  ex);
			
		} finally {
			
			lock.delete();
			
		}
		
	}

	// public static Class[] getAllClasses(String jarPath, ClassLoader
	// classLoader) throws ClassNotFoundException, IOException
	// {
	// if (jarPath == null || classLoader == null) {
	// return null;
	// }
	// String[] classNames = getAllClassNames(jarPath);
	// Class classes[] = new Class[classNames.length];
	// for (int i = 0; i < classNames.length; i++) {
	// String className = (String)classNames[i];
	// classes[i] = classLoader.loadClass(className);
	// }
	// return classes;
	// }

	private static String getDatedJarName(String jarName, Date date)
	{
		String nameNoExtension = jarName.substring(0, jarName.lastIndexOf(".jar"));
		if (date != null) {
			return nameNoExtension + ".v" + dateFormatter.format(date) + ".jar";
		}
		return nameNoExtension + ".v" + dateFormatter.format(new Date()) + ".jar";
	}

}
