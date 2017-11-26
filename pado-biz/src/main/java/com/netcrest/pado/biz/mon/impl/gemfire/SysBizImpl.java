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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;

import com.gemstone.gemfire.admin.RegionNotFoundException;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheTransactionManager;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.distributed.PoolCancelledException;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.BizType;
import com.netcrest.pado.biz.mon.DeploymentFailedException;
import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.gemfire.info.GemfireCacheInfo;
import com.netcrest.pado.gemfire.info.GemfireKeyTypeInfo;
import com.netcrest.pado.info.BizInfo;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.CacheServerInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.GridPathInfo;
import com.netcrest.pado.info.KeyTypeInfo;
import com.netcrest.pado.info.LoginInfo;
import com.netcrest.pado.info.PadoInfo;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.internal.impl.GridRoutingTable;
import com.netcrest.pado.internal.util.HotDeploymentBizClasses;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SysBizImpl implements ISysBiz
{

	// unit in msec
	private final static int FILE_LOCK_WAIT_TIME_IN_MSEC = Integer.getInteger("pado.delploy.fileLockWaitTimeInMsec",
			10000);
	// unit in msec
	private final static int FILE_LOCK_READ_WAKEUP_INTERVAL_IN_MSEC = 100;

	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmm");

	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;

	@Resource
	private IBizContextServer bizContext;

	/**
	 * Returns a sorted map of all key types in each region. This method call
	 * can be expensive as it searches all regions by iterating all entries. The
	 * returned results contain (full region path, Set &lt;KeyTypeInfo&gt;).
	 */
	private Map<String, Set<KeyTypeInfo>> getAllRegionKeyTypes()
	{
		Map<String, Set<KeyTypeInfo>> map = new TreeMap();
		Cache cache = CacheFactory.getAnyInstance();
		if (cache == null) {
			return map;
		}
		Set<Region<?, ?>> regionSet = cache.rootRegions();
		for (Region<?, ?> region : regionSet) {
			if (region.isDestroyed()) {
				continue;
			}
			map = getKeyTypes(region, map);
		}
		return map;
	}

	private Map<String, Set<KeyTypeInfo>> getKeyTypes(Region region, Map<String, Set<KeyTypeInfo>> map)
	{
		if (region == null || region.isDestroyed()) {
			return map;
		}
		Region localRegion;
		if (region instanceof PartitionedRegion) {
			localRegion = PartitionRegionHelper.getLocalPrimaryData(region);
		} else {
			localRegion = region;
		}

		Set<Region.Entry<?, ?>> entrySet = localRegion.entrySet(false);
		KeyMap keyMap = null;
		TreeSet<KeyTypeInfo> keyTypeInfoSet = new TreeSet();
		HashMap<String, KeyTypeInfo> nameMap = new HashMap();
		for (Region.Entry<?, ?> entry : entrySet) {
			Object obj = entry.getValue();
			keyMap = null;
			if (obj instanceof KeyMap) {
				keyMap = (KeyMap) obj;
			} else if (obj instanceof TemporalData) {
				obj = ((TemporalData) obj).getValue();
				if (obj instanceof KeyMap) {
					keyMap = (KeyMap) obj;
				}
			}
			if (keyMap == null || keyMap.getKeyType() == null) {
				break;
			}
			KeyTypeInfo info = nameMap.get(keyMap.getKeyTypeName());
			if (info == null) {
				info = new GemfireKeyTypeInfo();
				info.setKeyTypeClassName(keyMap.getKeyTypeName());
				info.setMergePoint(keyMap.getKeyType().getMergePoint());
				info.setVersion(keyMap.getKeyTypeVersion());
				info.setInstanceCount(1);
				nameMap.put(info.getKeyTypeClassName(), info);
				keyTypeInfoSet.add(info);
			} else {
				info.setInstanceCount(info.getInstanceCount() + 1);
			}
		}
		if (keyTypeInfoSet.size() > 0) {
			map.put(region.getFullPath(), keyTypeInfoSet);
		}

		Set<Region> subRegionSet = region.subregions(false);
		for (Region region2 : subRegionSet) {
			map = getKeyTypes(region2, map);
		}

		return map;
	}

	/**
	 * Ignored. This method is never used in the server side. It always returns
	 * null.
	 */
	@BizMethod
	@Override
	public IBizContextClient getBizContext()
	{
		return null;
	}

	@BizMethod
	@Override
	public PadoInfo getPadoInfo(String appId)
	{
		return InfoFactory.getInfoFactory().createPadoInfo(appId);
	}

	@BizMethod
	@Override
	public CacheInfo getCacheInfo()
	{
		return new GemfireCacheInfo(PadoServerManager.getPadoServerManager().getGridId(),
				CacheFactory.getAnyInstance());
	}

	@BizMethod
	@Override
	public List<CacheInfo> getCacheInfoList()
	{
		ArrayList<CacheInfo> list = new ArrayList(2);
		list.add(getCacheInfo());
		return list;
	}

	@BizMethod
	@Override
	public Map<String, List<CacheInfo>> getMapOfCacheInfoList()
	{
		HashMap<String, List<CacheInfo>> map = new HashMap(1, 1f);
		map.put(PadoServerManager.getPadoServerManager().getGridId(), getCacheInfoList());
		return map;
	}

	@BizMethod
	@Override
	public List<ServerInfo> getServerInfoList(String fullPath)
	{
		GridInfo gridInfo = PadoServerManager.getPadoServerManager().getGridInfo();
		CacheInfo cacheInfo = gridInfo.getCacheInfo();
		List<CacheServerInfo> cacheServerInfoList = cacheInfo.getCacheServerInfoList();
		List<ServerInfo> serverInfoList = new ArrayList(cacheServerInfoList.size() + 1);
		for (CacheServerInfo cacheServerInfo : cacheServerInfoList) {
			serverInfoList
					.add(InfoFactory.getInfoFactory().createServerInfo(gridInfo, cacheInfo, cacheServerInfo, fullPath));
		}
		return serverInfoList;
	}

	@BizMethod
	@Override
	public Map<String, List<ServerInfo>> getServerInfoListMap(String fullPath)
	{
		HashMap<String, List<ServerInfo>> map = new HashMap(1, 1f);
		map.put(PadoServerManager.getPadoServerManager().getGridId(), getServerInfoList(fullPath));
		return map;
	}

	@BizMethod
	@Override
	public Map<String, Set<KeyTypeInfo>>[] getKeyTypeInfoMaps()
	{
		Map[] maps = new Map[2];
		Map<String, Set<KeyTypeInfo>> map = KeyTypeManager.getAllRegisteredKeyTypeInfos();
		maps[0] = map;
		map = getAllRegionKeyTypes();
		maps[1] = map;
		return maps;
	}

	@BizMethod
	@Override
	public Map<String, Set<KeyTypeInfo>> getRegisteredKeyTypeInfoMap()
	{
		return KeyTypeManager.getAllRegisteredKeyTypeInfos();
	}

	/**
	 * Returns a map that contains KeyTypeInfo for each region in the server.
	 * 
	 * @return &lt;full path of region, Set&lt;KeyTypeInfo&gt;&gt
	 */
	@BizMethod
	@Override
	public Map<String, Set<KeyTypeInfo>> getRegionKeyTypeInfoMap()
	{
		return getAllRegionKeyTypes();
	}

	@BizMethod
	@Override
	public String[] getRegisteredMainKeyTypeNames()
	{
		return KeyTypeManager.getAllRegisteredMainKeyTypeNames();
	}

	@BizMethod
	@Override
	public void mergeKeyTypeVersions(String gridPath, String keyTypeClassName, int targetVersion, int[] versions)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new PadoException(new RegionNotFoundException(gridPath + " is not found in the grid."));
		}

		int toVersion = targetVersion;
		int fromVersions[] = versions;
		KeyType toKeyType = null;
		try {
			Class clazz = Class.forName(keyTypeClassName);
			KeyType[] keyTypes = KeyTypeManager.getAllRegisteredVersions(clazz);
			if (keyTypes == null || keyTypes.length == 0) {
				throw new PadoException("Specified KeyType class not registered: " + keyTypeClassName);
			}
			toKeyType = KeyTypeManager.getKeyType((UUID) keyTypes[0].getId(), toVersion);
		} catch (ClassNotFoundException ex) {
			throw new PadoException("Specified KeyType class not found: " + keyTypeClassName);
		} catch (Exception ex) {
			throw new PadoException("Exception raised in the grid: " + ex.getMessage(), ex);
		}

		if (toKeyType == null) {
			throw new PadoException("The targer version not registred in the grid: [" + keyTypeClassName + ", version="
					+ toVersion + "]");
		}

		Region localRegion;
		if (region instanceof PartitionedRegion) {
			localRegion = PartitionRegionHelper.getLocalPrimaryData(region);
		} else {
			localRegion = region;
		}

		// Transact - very expensive operation
		// TODO: try breaking it down to multiple transactions
		CacheTransactionManager txManager = CacheFactory.getAnyInstance().getCacheTransactionManager();
		HashMap map = new HashMap(1001, 1f);
		try {
			txManager.begin();
			Set<Map.Entry> set = localRegion.entrySet();
			for (Map.Entry entry : set) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				Object data = value;
				if (data instanceof TemporalData) {
					data = ((TemporalData) data).getValue();
				}
				if (data instanceof KeyMap) {
					KeyMap keyMap = (KeyMap) data;
					for (int i = 0; i < fromVersions.length; i++) {
						if (keyMap.getKeyType().getVersion() == fromVersions[i]) {
							keyMap.merge(toKeyType);
							map.put(key, value);
						}
					}
				}
				if (map.size() > 0 && map.size() % 1000 == 0) {
					region.putAll(map);
					map.clear();
				}
			}
			if (map.size() > 0) {
				region.putAll(map);
				map.clear();
			}
			txManager.commit();

		} catch (Exception ex) {

			throw new PadoException("Transaction failed. It is rolled back." + ex.getMessage(), ex);

		}
	}

	@BizMethod
	@Override
	public Set<BizInfo> getAllSysBizInfos()
	{
		return getSysBizInfos(null);
	}

	@BizMethod
	@Override
	public Set<BizInfo> getSysBizInfos(String regex)
	{
		LoginInfo loginInfo = PadoServerManager.getPadoServerManager()
				.getLoginInfo(bizContext.getUserContext().getToken());
		if (loginInfo == null) {
			return null;
		}
		String appId = loginInfo.getAppId();
		if (appId == null || appId.equals("sys") == false) {
			return null;
		}
		return PadoServerManager.getPadoServerManager().getSysBizInfos("sys", regex);
	}

	@BizMethod
	@Override
	public Set<BizInfo> getAllAppBizInfos()
	{
		return getAppBizInfos(null);
	}

	@BizMethod
	@Override
	public Set<BizInfo> getAppBizInfos(String regex)
	{
		LoginInfo loginInfo = PadoServerManager.getPadoServerManager()
				.getLoginInfo(bizContext.getUserContext().getToken());
		if (loginInfo == null) {
			return null;
		}
		String appId = loginInfo.getAppId();
		if (appId == null) {
			return null;
		}
		return PadoServerManager.getPadoServerManager().getAllAppBizInfos(appId, regex);
	}

	@BizMethod
	@Override
	public Set<BizInfo> getAllBizInfos()
	{
		return getBizInfos(null);
	}

	@BizMethod
	@Override
	public Set<BizInfo> getBizInfos(String regex)
	{
		LoginInfo loginInfo = PadoServerManager.getPadoServerManager()
				.getLoginInfo(bizContext.getUserContext().getToken());
		if (loginInfo == null) {
			return null;
		}
		String appId = loginInfo.getAppId();
		if (appId == null) {
			return null;
		}
		return PadoServerManager.getPadoServerManager().getBizInfos(appId, regex);
	}

	@BizMethod
	@Override
	public Map<String, GridPathInfo> getGridPathInfoMap()
	{
		return PadoServerManager.getPadoServerManager().getGridPathInfoMap();
	}

	@BizMethod
	@Override
	public long getCurrentTimeMillis()
	{
		return System.currentTimeMillis();
	}

	@BizMethod
	@Override
	public void deployJars(String[] jarNames, byte[][] jarContents, Date timestamp) throws DeploymentFailedException
	{
		HotDeploymentBizClasses hotDeployment = new Deployment().save(jarNames, jarContents, timestamp);
		PadoServerManager.getPadoServerManager().fireDeploymentEvent(hotDeployment);
	}

	@BizMethod
	@Override
	public void deployDnas(String[] dnaDistNames, byte[][] dnaDistContents, Date timestamp)
			throws DeploymentFailedException
	{
		saveDnaFiles(dnaDistNames, dnaDistContents, timestamp);
	}

	/**
	 * Saves the specified DNA distribution contents in the language specific
	 * directories.
	 * 
	 * @param dnaDistNames
	 *            DNA distribution names
	 * @param dnaDistContents
	 *            DNA distribution binary contents
	 * @param timestamp
	 *            Time stamp for versioning the file names
	 * @throws DeploymentFailedException
	 *             Thrown if encounters an IO error.
	 */
	private void saveDnaFiles(String[] dnaDistNames, byte[][] dnaDistContents, Date timestamp)
			throws DeploymentFailedException
	{
		String codeMessage = null;

		byte byteBuffers[][] = dnaDistContents;
		Date dateSuffix = timestamp;

		String langDir = PadoUtil.getProperty(com.netcrest.pado.internal.Constants.PROP_LANG_DIR,
				com.netcrest.pado.internal.Constants.DEFAULT_LANG_DIR);
		String tmpDir = langDir + "/tmp";
		File tmpDirFile = new File(tmpDir);
		File lock = new File(tmpDir + "/.lock");
		boolean deadlockOccurred = false;

		try {
			tmpDirFile.mkdirs();
			boolean lockCreated = lock.createNewFile();
			boolean writeOK = lockCreated;
			if (dateSuffix == null) {
				writeOK = true;
			}

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
			}

			// Get dated file paths
			File datedFiles[] = new File[byteBuffers.length];
			URL datedUrls[] = new URL[datedFiles.length];
			for (int i = 0; i < byteBuffers.length; i++) {
				String filePath = tmpDir + "/" + getDatedZipName(dnaDistNames[i], dateSuffix);
				datedFiles[i] = new File(filePath);
				datedUrls[i] = datedFiles[i].toURI().toURL();
			}

			// Write all zip files to the file system (tmp dir) and unzip them.
			if (writeOK) {
				for (int i = 0; i < byteBuffers.length; i++) {
					FileOutputStream fos = new FileOutputStream(datedFiles[i]);
					fos.write(byteBuffers[i]);
					fos.close();
				}

				// Unzip the dated files in the language specific directories
				for (int i = 0; i < datedFiles.length; i++) {
					String lang = getLang(datedFiles[i].getAbsolutePath());
					String dir;
					if (lang.equals("python")) {
						dir = langDir + "/" + lang + "/local-packages";
					} else if (lang.equals("java")) {
						dir = langDir + "/" + lang + "/lib";
					} else {
						dir = langDir + "/" + lang;
					}
					unzip(datedFiles[i].getAbsolutePath(), dir);
				}

				// Delete old versioned files from the tmp dir
				deleteOldVersionedFiles(tmpDirFile, ".zip");
			}
			codeMessage = "Deployed DNA file(s) to " + tmpDirFile.getCanonicalPath();
			if (deadlockOccurred) {
				codeMessage = codeMessage
						+ " -- Deadlock occurred. DNA file(s) may have been overwritten in the same file system. Please see server log files for details.";
				throw new DeploymentFailedException(codeMessage);
			}

			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < datedFiles.length; i++) {
				if (i > 0) {
					buffer.append(", ");
				}
				buffer.append(datedFiles[i].getName());
			}
			Logger.info("Delployed the following DNA file(s): " + buffer.toString());

		} catch (Exception ex) {
			StringBuffer buffer = new StringBuffer(100);
			buffer.append("[");
			for (int i = 0; i < dnaDistNames.length; i++) {
				if (i == dnaDistNames.length - 1) {
					buffer.append(dnaDistNames[i] + "]");
				} else {
					buffer.append(dnaDistNames[i] + ", ");
				}
			}

			throw new DeploymentFailedException("DNA deployment failed for one or more files: " + buffer.toString(),
					ex);
		} finally {

			lock.delete();

		}
	}

	/**
	 * Deletes all of the old versions of files. A versioned file has the file
	 * name format "&lt;prefix&gt;.v&lt;yyyymmddhhmm&gt;.&lt;fileExtension&gt;".
	 * After this call, only the latest versioned files remain in the specified
	 * directory.
	 * 
	 * @param dir
	 *            Directory to search for all files
	 * @param fileExtension
	 *            File extension
	 */
	private void deleteOldVersionedFiles(File dir, final String fileExtension)
	{
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname)
			{
				boolean keep = pathname.getName().endsWith(fileExtension);
				return keep;
			}

		});

		Arrays.sort(files, Collections.reverseOrder());
		String filePrefix = "";
		String prevFilePrefix = "";
		for (File file : files) {
			String split[] = file.getName().split("\\.");
			if (split.length > 2) {
				String version = split[split.length - 2];
				if (version.startsWith("v2")) {
					for (int i = 0; i < split.length - 2; i++) {
						if (i == 0) {
							filePrefix = split[i];
						} else {
							filePrefix += "." + split[i];
						}
					}
					if (filePrefix.equals(prevFilePrefix)) {
						// Delete
						try {
							file.delete();
						} catch (Exception ex) {
							// ignore
						}
					} else {
						prevFilePrefix = filePrefix;
					}
				} else {
					prevFilePrefix = "";
				}
			} else {
				prevFilePrefix = "";
			}
		}
	}

	/**
	 * Returns a new name with the version number extension.
	 * 
	 * @param dnaDistName
	 *            DNA distribution name
	 * @param date
	 *            Date
	 */
	private static String getDatedZipName(String dnaDistName, Date date)
	{
		String nameNoExtension = dnaDistName.substring(0, dnaDistName.lastIndexOf(".zip"));
		if (date != null) {
			return nameNoExtension + ".v" + dateFormatter.format(date) + ".zip";
		}
		return nameNoExtension + ".v" + dateFormatter.format(new Date()) + ".zip";
	}

	/**
	 * Returns the language of the specified zip file. The language is
	 * determined by searching the zip file for the language specific file
	 * extension as follows:
	 * <ul>
	 * <li>python - .py</li>
	 * <li>java - .jar</li>
	 * </ul>
	 * 
	 * @param zipFilePath
	 * @return "python" or "java"
	 * @throws IOException
	 *             Thrown if unable to read the zip file
	 */
	private String getLang(String zipFilePath) throws IOException
	{
		ZipFile zipFile = null;
		String lang = "java";
		try {
			zipFile = new ZipFile(zipFilePath);
			Enumeration zipEntries = zipFile.entries();
			String fname;

			while (zipEntries.hasMoreElements()) {
				fname = ((ZipEntry) zipEntries.nextElement()).getName();
				if (fname.endsWith(".py")) {
					lang = "python";
					break;
				} else if (fname.endsWith(".jar")) {
					lang = "java";
					break;
				}
			}
		} finally {
			if (zipFile != null) {
				zipFile.close();
			}
		}
		return lang;
	}

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified
	 * by destDirectory (will be created if does not exists)
	 * 
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	private void unzip(String zipFilePath, String destDirectory) throws IOException
	{
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException
	{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	@BizMethod
	@Override
	public void updateGridRoutingTable(GridRoutingTable routingTable)
	{
		if (routingTable == null) {
			return;
		}
		PadoServerManager.getPadoServerManager().updateGridRoutingTable(routingTable);
	}

	@BizMethod
	@Override
	public void updateGridPaths(Set<GridPathInfo> gridPathInfoSet)
	{
		if (gridPathInfoSet == null) {
			return;
		}
		PadoServerManager.getPadoServerManager().updateGridPaths(gridPathInfoSet);
	}

	@Override
	public void setGridEnabled(String appId, String gridId, boolean enabled)
	{
		PadoServerManager.getPadoServerManager().setGridEnabled(appId, gridId, enabled);
	}

	@Override
	public boolean isGridEnabled(String appId, String gridId)
	{
		return PadoServerManager.getPadoServerManager().isGridEnabled(appId, gridId);
	}

	@Override
	@BizMethod
	public void resetKeyTypeQueryRerences()
	{
		KeyTypeManager.resetDb(PadoUtil.getProperty(Constants.PROP_DB_DIR));
	}

	@Override
	@BizMethod
	public void registerKeyTypeQueryReferences(JsonLite jl, boolean isPersist)
			throws IOException, ClassNotFoundException, PadoException, ClassCastException
	{
		KeyTypeManager.registerQueryReferences(PadoUtil.getProperty(Constants.PROP_DB_DIR), jl, isPersist);
	}

	@Override
	@BizMethod
	public void registerKeyTypeQueryReferences(JsonLite[] list, boolean isPersist)
			throws IOException, ClassNotFoundException, PadoException, ClassCastException
	{
		if (list == null) {
			return;
		}
		for (JsonLite jl : list) {
			KeyTypeManager.registerQueryReferences(PadoUtil.getProperty(Constants.PROP_DB_DIR), jl, isPersist);
		}
	}

	@Override
	@BizMethod
	public void attachToParentGrid(String parentGridId)
	{
		Logger.info("Parent grid attachement command received and processing... [gridId="
				+ PadoServerManager.getPadoServerManager().getGridId() + ", parentGridId=" + parentGridId + "]");
		if (PadoServerManager.getPadoServerManager().getGridId().equals(parentGridId)) {
			Logger.info("Detaching grid failed. The specified grid " + parentGridId + " cannot be same as this grid.");
			return;
		}

		// First, attach itself to the parent grid by connecting to the parent
		PadoServerManager.getPadoServerManager().attachToParentGrid(parentGridId);

		// Next, provide this grid's GridInfo to the attached parent grid to
		// update the child grid metadata.
		GridInfo gridInfo = PadoServerManager.getPadoServerManager().getGridInfo();
		ISysBiz sysBiz = PadoServerManager.getPadoServerManager().getCatalog().newInstance(ISysBiz.class);
		sysBiz.attachToParentGridWithGridInfo(gridInfo);
		Logger.info("The grid " + PadoServerManager.getPadoServerManager().getGridId()
				+ " has successfully been attached to the specified parent grid " + parentGridId + ".");
	}

	@Override
	@BizMethod
	public void detachFromParentGrid(String parentGridId)
	{
		Logger.info("Parent grid detachment command received and processing... [gridId="
				+ PadoServerManager.getPadoServerManager().getGridId() + ", parentGridId=" + parentGridId + ", user="
				+ bizContext.getUserContext().getUsername() + "]");
		if (PadoServerManager.getPadoServerManager().getGridId().equals(parentGridId)) {
			Logger.info("Detaching grid failed. The specified grid " + parentGridId + " cannot be same as this grid.");
			return;
		}

		// First, notify the parent grid to remove this grid
		GridInfo gridInfo = PadoServerManager.getPadoServerManager().getGridInfo();
		ISysBiz sysBiz = PadoServerManager.getPadoServerManager().getCatalog().newInstance(ISysBiz.class);
		try {
			sysBiz.detachFromParentGridWithGridInfo(gridInfo);
		} catch (PoolCancelledException ex) {
			// Ignore. This is raised if the connectio pool has been destroyed
			// previously when detaching the grid. GemFire does not provide
			// an API to check whether the pool is connected. This is a
			// workaround.
		}

		// Next, remove itself from its parent notification service, i.e.,
		// heart beats sent to the parent
		PadoServerManager.getPadoServerManager().removeGrid(parentGridId, true);
		Logger.info("This grid has successfully been detached from the specified parent grid " + parentGridId + ") - "
				+ "[user=" + bizContext.getUserContext().getUsername() + "]");
	}

	@Override
	@BizMethod
	public void attachToParentGridWithGridInfo(GridInfo childGridInfo)
	{
		Logger.info("Child grid attachement command received and processing... [user="
				+ bizContext.getUserContext().getUsername() + "]");
		if (childGridInfo == null
				|| PadoServerManager.getPadoServerManager().getGridId().equals(childGridInfo.getGridId())) {
			Logger.info("Attaching child grid failed. The specified grid " + childGridInfo.getGridId()
					+ " cannot be same as this grid ID.");
			return;
		}

		PadoServerManager.getPadoServerManager().updateGrid(childGridInfo, false);
		Logger.info("The specified child grid " + childGridInfo.getGridId()
				+ " has successfully been attached to this parent grid ("
				+ PadoServerManager.getPadoServerManager().getGridId() + ") - " + "[user="
				+ bizContext.getUserContext().getUsername() + "]");
	}

	@Override
	@BizMethod
	public void detachFromParentGridWithGridInfo(GridInfo parentGridInfo)
	{
		Logger.info("Parent grid detachment command received and processing... [user="
				+ bizContext.getUserContext().getUsername() + "]");
		if (parentGridInfo == null
				|| PadoServerManager.getPadoServerManager().getGridId().equals(parentGridInfo.getGridId())) {
			Logger.info("Detaching grid failed. The specified grid " + parentGridInfo.getGridId()
					+ " cannot be same as this grid ID.");
			return;
		}

		PadoServerManager.getPadoServerManager().removeGrid(parentGridInfo, false);
		Logger.info("This grid has successfully been detached from the specified parent grid "
				+ parentGridInfo.getGridId() + ") - " + "[user=" + bizContext.getUserContext().getUsername() + "]");
	}

	@Override
	@BizMethod(bizType = BizType.PADO)
	public void attachChildToParentGrid(String childGridId)
	{
		// Not supported
	}

	@Override
	@BizMethod
	public void detachChildFromParentGrid(String childGridId)
	{
		Logger.info("Child grid detachment command received and processing... [gridId="
				+ PadoServerManager.getPadoServerManager().getGridId() + ", childGridId=" + childGridId + ", user="
				+ bizContext.getUserContext().getUsername() + "]");
		if (PadoServerManager.getPadoServerManager().getGridId().equals(childGridId)) {
			Logger.info("Detaching grid failed. The specified grid " + childGridId + " cannot be same as this grid.");
			return;
		}

		PadoServerManager.getPadoServerManager().removeGrid(childGridId, false);
		Logger.info("The specified child grid " + childGridId
				+ " has successfully been detached from the this parent grid ("
				+ PadoServerManager.getPadoServerManager().getGridId() + ") - " + "[user="
				+ bizContext.getUserContext().getUsername() + "]");
	}
}
