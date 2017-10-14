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
package com.netcrest.pado.biz.impl.gemfire;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.management.ObjectName;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.management.ManagementService;
import com.gemstone.gemfire.management.MemberMXBean;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.data.ServerLoad;
import com.netcrest.pado.biz.file.CompositeKeyInfo;
import com.netcrest.pado.biz.file.CsvFileLoader;
import com.netcrest.pado.biz.file.SchemaInfo;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.gemfire.factory.GemfireInfoFactory;
import com.netcrest.pado.gemfire.info.GemfireBucketInfo;
import com.netcrest.pado.gemfire.info.GemfireRegionInfo;
import com.netcrest.pado.gemfire.info.GemfireWhichInfo;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.gemfire.util.RegionUtil;
import com.netcrest.pado.index.provider.gemfire.OqlSearch;
import com.netcrest.pado.info.BucketInfo;
import com.netcrest.pado.info.CacheDumpInfo;
import com.netcrest.pado.info.DumpInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.info.WhichInfo;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.internal.util.OutputUtil;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.util.GridUtil;

public class UtilBizImpl
{
	@Resource
	IBizContextServer bizContext;

	@BizMethod
	public byte[] ping(byte[] payload)
	{
		return payload;
	}

	@BizMethod
	public String echo(String message)
	{
		Logger.info(message);
		return message;
	}

	@BizMethod
	public ServerLoad getServerLoad()
	{
		ManagementService ms = ManagementService.getExistingManagementService(CacheFactory.getAnyInstance());
		try {
			Cache cache = CacheFactory.getAnyInstance();
			String memberName = cache.getDistributedSystem().getDistributedMember().getName();
			// "GemFire:type=Member,member=<name-or-dist-member-id>"
			ObjectName objName = new ObjectName("GemFire:type=Member,member=" + memberName);
			MemberMXBean memberBean = (MemberMXBean) ms.getMBeanInstance(objName, MemberMXBean.class);
			// JVMMetrics metrics = memberBean.showJVMMetrics();
			// long freeMemory = metrics.getMaxMemory() -
			// metrics.getUsedMemory();

			PadoServerManager psm = PadoServerManager.getPadoServerManager();
			String gridId = psm.getGridId();
			String siteId = psm.getSiteId();
			String serverId = psm.getServerId();
			long freeMemory = memberBean.getFreeHeapSize();
			long maxMemory = memberBean.getMaximumHeapSize();
			byte memoryUsage = 0;
			if (maxMemory > 0) {
				memoryUsage = (byte) (freeMemory / maxMemory);
			}
			byte cpuUsage = (byte) (memberBean.getCpuUsage());
			double averageLoad = memberBean.getLoadAverage();
			long gcPauses = memberBean.getJVMPauses();
			long gcCount = memberBean.getGarbageCollectionCount();
			long gcTime = memberBean.getGarbageCollectionTime();
			long gcAverageTime = 0;
			if (gcCount > 0) {
				gcAverageTime = gcTime / gcCount;
			}
			ServerLoad load = new ServerLoad(gridId, siteId, serverId, memoryUsage, cpuUsage, averageLoad,
					gcAverageTime, gcPauses);

			return load;

		} catch (Exception e) {
			throw new PadoServerException("ServerLoad creation error", e);
		}
	}

	@BizMethod
	public Date getServerTime()
	{
		return new Date();
	}

	@BizMethod
	public String dumpAll()
	{
		String dumpDir = PadoUtil.getProperty(Constants.PROP_DUMP_DIR, Constants.DEFAULT_DUMP_DIR);
		String fileNamePostfix = getFileNamePostfix();

		GemfirePadoServerManager sm = GemfirePadoServerManager.getPadoServerManager();
		GemfireRegionInfo regionInfo = new GemfireRegionInfo(sm.getRootRegion(), true);

		File topDir = new File(dumpDir, "all/" + fileNamePostfix + regionInfo.getFullPath());
		topDir.mkdirs();
		dumpPaths(regionInfo, topDir, fileNamePostfix);
		return topDir.getAbsolutePath();
	}

	private void dumpPaths(GemfireRegionInfo regionInfo, File topDir, String fileNamePostfix)
	{
		if (regionInfo.isHidden(false) || regionInfo.isDataPolicyEmptyRegion(false)) {
			return;
		}

		if (regionInfo.isRoot() == false) {
			String gridPath = regionInfo.getGridRelativePath();
			if (gridPath != null && gridPath.length() > 0) {
				dumpServer(topDir, fileNamePostfix, gridPath);
			}
		}

		List<PathInfo> list = regionInfo.getChildList();
		for (PathInfo pathInfo : list) {
			GemfireRegionInfo ri = (GemfireRegionInfo) pathInfo;
			if (ri.isHidden(false) || ri.isDataPolicyEmptyRegion(false)) {
				continue;
			}
			dumpPaths(ri, topDir, fileNamePostfix);
		}
	}

	@BizMethod
	public String dumpServers(String... gridPaths)
	{
		String dumpDir = PadoUtil.getProperty(Constants.PROP_DUMP_DIR, Constants.DEFAULT_DUMP_DIR);
		String fileNamePostfix = getFileNamePostfix();
		GemfirePadoServerManager sm = GemfirePadoServerManager.getPadoServerManager();
		File dir = new File(dumpDir, "path/" + fileNamePostfix + sm.getRootRegion().getFullPath());
		for (String gridPath : gridPaths) {
			dumpServer(dir, fileNamePostfix, gridPath);
		}
		return dir.getAbsolutePath();
	}

	private String getFileNamePostfix()
	{
		Date date = new Date();
		Object[] args = bizContext.getGridContextServer().getAdditionalArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Date) {
				date = (Date) args[0];
			}
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
		String fileNamePostfix = formatter.format(date);
		return fileNamePostfix;
	}

	/**
	 * Dumps the contents of the specified grid path of this server.
	 * 
	 * @param topDir
	 *            Top-level directory in which the grid path contents are dumped
	 *            in CSV file.
	 * @param fileNamePostfix
	 *            File name postfix.
	 * @param gridPath
	 *            Grid path. If nested, then CSV file is created in the parent
	 *            directory.
	 * @return Dumped file path. Null if the local data set is empty or the grid
	 *         path is not defined.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized String dumpServer(File topDir, String fileNamePostfix, String gridPath)
	{
		Region rootRegion = GemfirePadoServerManager.getPadoServerManager().getRootRegion();
		Region region = rootRegion.getSubregion(gridPath);
		if (region == null) {
			return null;
		}

		String serverName = PadoServerManager.getPadoServerManager().getServerName();
		String fileRelativePath = gridPath + "." + serverName + "." + fileNamePostfix;
		String schemaRelativePath = fileRelativePath + ".schema";
		String csvRelativePath = fileRelativePath + ".csv";
		File fileDir = new File(topDir, gridPath).getParentFile();
		if (fileDir.exists() == false) {
			fileDir.mkdirs();
		}
		File schemaFile = new File(topDir, schemaRelativePath);
		File csvFile = new File(topDir, csvRelativePath);
		PrintWriter schemaWriter = null;
		PrintWriter csvWriter = null;
		try {
			Map map = region;
			Object key = null;
			Object data = null;

			// Get the first entry in the region.
			// TODO: Scan the entire region to get the full set of keys if
			// the object is non-KeyMap Map. This is required since
			// the key may not exist in non-KeyMap map.
			Iterator iterator = map.entrySet().iterator();
			if (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				key = entry.getKey();
				data = entry.getValue();
			}
			if (key == null) {
				return null;
			}

			schemaFile.getParentFile().mkdirs();
			schemaWriter = new PrintWriter(schemaFile);
			csvWriter = new PrintWriter(csvFile);
			SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

			Object value = data;
			if (value instanceof TemporalData) {
				TemporalData td = (TemporalData) value;
				value = td.getValue();
			}
			List keyList = null;
			if (value instanceof Map) {
				// Must iterate the entire map to get all unique keys
				Map valueMap = (Map) value;
				Set keySet = valueMap.keySet();
				HashSet set = new HashSet(keySet.size(), 1f);
				set.addAll(keySet);
				keyList = new ArrayList(set);
				Collections.sort(keyList);
			}
			String fullPath = GridUtil.getFullPath(gridPath);
			CompositeKeyInfo compositeKeyInfo = RegionUtil.getCompositeKeyInfoForIdentityKeyPartionResolver(fullPath);
			OutputUtil.printSchema(schemaWriter, gridPath, key, data, keyList, OutputUtil.TYPE_KEYS_VALUES, ",",
					iso8601DateFormat, true, true, compositeKeyInfo);
			schemaWriter.flush();

			if (region instanceof PartitionedRegion) {
				PartitionedRegion pr = (PartitionedRegion) region;
				Set<BucketRegion> set = pr.getDataStore().getAllLocalPrimaryBucketRegions();
				Iterator<BucketRegion> bucketRegionIterator = set.iterator();
				// Print the column header for the first bucket region that has
				// data.
				while (bucketRegionIterator.hasNext()) {
					BucketRegion bucketRegion = bucketRegionIterator.next();
					if (bucketRegion.size() > 0) {
						OutputUtil.printEntries(csvWriter, bucketRegion, ",", OutputUtil.TYPE_KEYS_VALUES,
								iso8601DateFormat, true);
						break;
					}
				}
				// No column header for the rest of the bucket regions that
				// have data.
				while (bucketRegionIterator.hasNext()) {
					BucketRegion bucketRegion = bucketRegionIterator.next();
					if (bucketRegion.size() > 0) {
						OutputUtil.printEntries(csvWriter, bucketRegion, ",", OutputUtil.TYPE_KEYS_VALUES,
								iso8601DateFormat, false);
					}
				}
			} else {
				if (PadoServerManager.getPadoServerManager().isMaster()) {
					OutputUtil.printEntries(csvWriter, map, ",", OutputUtil.TYPE_KEYS_VALUES, iso8601DateFormat, true);
				}
			}

			csvWriter.flush();
		} catch (IOException e) {
			throw new PadoServerException(e);
		} finally {
			if (schemaWriter != null) {
				schemaWriter.close();
			}
			if (csvWriter != null) {
				csvWriter.close();
			}
		}

		return csvFile.getAbsolutePath();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@BizMethod
	public WhichInfo which(String gridPath, Object key)
	{
		Region region = GemfireGridUtil.getRegion(gridPath);
		if (region == null) {
			return null;
		}

		GemfireWhichInfo whichInfo = null;
		Object value = null;
		Object actualKey = null;
		BucketInfo bucketInfo = null;
		if (region instanceof PartitionedRegion) {

			// Partitioned region
			PartitionedRegion pr = (PartitionedRegion) region;
			Set<BucketRegion> localBucketSet = pr.getDataStore().getAllLocalBucketRegions();
			for (BucketRegion br : localBucketSet) {
				value = br.get(key);
				if (value != null) {
					actualKey = key;
					bucketInfo = new GemfireBucketInfo(br.getId(), br.getBucketAdvisor().isPrimary(), br.size(),
							br.getTotalBytes());
				} else {
					Set<Map.Entry> set = br.entrySet();
					for (Map.Entry entry : set) {
						Object tempKey = entry.getKey();
						if (tempKey.hashCode() == key.hashCode() && tempKey.equals(key)) {
							actualKey = tempKey;
							bucketInfo = new GemfireBucketInfo(br.getId(), br.getBucketAdvisor().isPrimary(), br.size(),
									br.getTotalBytes());
							value = entry.getValue();
							break;
						}
					}
				}
				if (actualKey != null) {
					whichInfo = new GemfireWhichInfo(key, actualKey, value, true, bucketInfo);
					break;
				}
			}

		} else {

			// Non-partitioned region
			value = region.get(key);
			if (value == null) {
				Set<Map.Entry> set = region.entrySet();
				for (Map.Entry entry : set) {
					Object tempKey = entry.getKey();
					if (tempKey.hashCode() == key.hashCode() && tempKey.equals(key)) {
						actualKey = tempKey;
						value = entry.getValue();
						break;
					}
				}
			} else {
				actualKey = key;
			}

			if (actualKey != null) {
				whichInfo = new GemfireWhichInfo(key, actualKey, value, false, null);
			}
		}

		if (whichInfo != null) {
			updateWhichInfo(region, whichInfo);
		}
		return whichInfo;
	}

	@SuppressWarnings("rawtypes")
	private WhichInfo updateWhichInfo(Region region, GemfireWhichInfo whichInfo)
	{
		PadoServerManager sm = PadoServerManager.getPadoServerManager();
		DistributedMember member = CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember();
		String serverName = member.getName();
		String serverId = member.getId();

		if (whichInfo == null) {
			whichInfo = new GemfireWhichInfo();
		}
		whichInfo.setPartitioned(region instanceof PartitionedRegion);
		whichInfo.setGridId(sm.getGridId());
		whichInfo.setServerName(serverName);
		whichInfo.setServerId(serverId);
		whichInfo.setHost(member.getHost());
		whichInfo.setRedundancyZone(
				CacheFactory.getAnyInstance().getDistributedSystem().getProperties().getProperty("redundancy-zone"));

		return whichInfo;
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public WhichInfo whichRoutingKey(String gridPath, Object routingKey)
	{
		Region region = CacheFactory.getAnyInstance().getRegion(GridUtil.getFullPath(gridPath));
		if (region == null) {
			return null;
		}
		return updateWhichInfo(region, null);
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public List executeRoutingQuery(String queryString, Object routingKey)
	{
		return OqlSearch.getOqlSearch().executeQuery(queryString);
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public List executeQuery(String queryString)
	{
		return OqlSearch.getOqlSearch().executeQuery(queryString);
	}

	@BizMethod
	public String importAll()
	{
		return importAll(null);
	}

	/**
	 * Returns the directory with the latest time stamp that is less than or
	 * equal to the specified as-of date.
	 * 
	 * @param asOfDate
	 *            As-of date. If null, the current time is assigned.
	 * @param isAll
	 *            true to search the "all" directory, false to search the "path"
	 *            directory.
	 * @return null if the directory is not found.
	 */
	private File getAsOfDir(Date asOfDate, boolean isAll)
	{
		if (asOfDate == null) {
			asOfDate = new Date();
		}
		// Make sure the dump directory exists
		String dumpDir = PadoUtil.getProperty(Constants.PROP_DUMP_DIR, Constants.DEFAULT_DUMP_DIR);
		File dir;
		if (isAll) {
			dir = new File(dumpDir, "all");
		} else {
			dir = new File(dumpDir, "path");
		}
		if (dir.isDirectory() == false || dir.exists() == false) {
			return null;
		}

		// Determine the latest dump directory by searching the sorted
		// sub-directories under the dump directory.
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
		String[] fileNames = dir.list();
		Arrays.sort(fileNames);
		Date date = null;
		File parentDir = null;
		for (int i = fileNames.length - 1; i >= 0; i--) {
			try {
				parentDir = new File(dir, fileNames[i]);
				if (parentDir.isDirectory()) {
					date = formatter.parse(fileNames[i]);
					if (date.getTime() <= asOfDate.getTime()) {
						break;
					}
				}
			} catch (ParseException ex) {
				// ignore
			}
		}
		if (date == null || parentDir == null) {
			return null;
		}

		// Get root path
		GemfirePadoServerManager sm = GemfirePadoServerManager.getPadoServerManager();
		parentDir = new File(parentDir, sm.getRootRegion().getName());
		return parentDir;
	}

	@BizMethod
	public String importAll(Date asOfDate)
	{
		File dir = getAsOfDir(asOfDate, true);
		if (dir == null) {
			return null;
		}

		// Import data
		CsvFileLoader loader = new CsvFileLoader(PadoServerManager.getPadoServerManager().getPado());
		importFile(loader, dir);
		return dir.getAbsolutePath();
	}

	/**
	 * Recursively import files.
	 * 
	 * @param loader
	 *            File loader
	 * @param file
	 *            file
	 */
	private void importFile(CsvFileLoader loader, File file)
	{
		if (file.isDirectory()) {
			File files[] = file.listFiles();
			for (File file2 : files) {
				importFile(loader, file2);
			}
		} else if (file.getName().endsWith(".schema")) {
			File schemaFile = file;
			int index = schemaFile.getName().lastIndexOf(".schema");
			String dataFileName = schemaFile.getName().substring(0, index) + ".csv";
			File dataFile = new File(schemaFile.getParentFile(), dataFileName);
			if (dataFile.exists()) {
				SchemaInfo schemaInfo = new SchemaInfo("file", schemaFile);

				// Disable temporal indexing temporarily during the import time
				// to speed up.
				boolean temporalWasEnabled = false;
				if (schemaInfo.isTemporal()) {
					String fullPath = GridUtil.getFullPath(schemaInfo.getGridPath());
					TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
					if (tm != null) {
						temporalWasEnabled = tm.isEnabled();
						if (temporalWasEnabled) {
							// block till done
							tm.setEnabled(false, false, false /* spawnThread */);
						}
					}
				}

				// Load data
				loader.load(schemaInfo, dataFile);

				// Enable temporal indexing if disabled at the start of this
				// method.
				if (schemaInfo.isTemporal()) {
					if (temporalWasEnabled) {
						String fullPath = GridUtil.getFullPath(schemaInfo.getGridPath());
						TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
						if (tm != null) {
							if (temporalWasEnabled) {
								// block till done
								tm.setEnabled(true, true,
										false /* spawn Thread */);
							}
						}
					}
				}
			}
		}
	}

	@BizMethod
	public String importServers(boolean isAll, String... gridPaths)
	{
		return importServers(null, isAll, gridPaths);
	}

	@BizMethod
	public String importServers(Date asOfDate, boolean isAll, String... gridPaths)
	{
		File dir = getAsOfDir(asOfDate, isAll);
		if (dir == null) {
			return null;
		}

		// Import data
		String serverName = PadoServerManager.getPadoServerManager().getServerName();
		String fileNamePostfix = dir.getParentFile().getName();
		CsvFileLoader loader = new CsvFileLoader(PadoServerManager.getPadoServerManager().getPado());
		for (String gridPath : gridPaths) {
			String fileRelativePath = gridPath + "." + serverName + "." + fileNamePostfix;
			String schemaRelativePath = fileRelativePath + ".schema";
			String csvRelativePath = fileRelativePath + ".csv";
			File schemaFile = new File(dir, schemaRelativePath);
			File dataFile = new File(dir, csvRelativePath);
			if (schemaFile.exists() && dataFile.exists()) {
				SchemaInfo schemaInfo = new SchemaInfo("file", schemaFile);

				// Disable temporal indexing temporarily during the import time
				// to speed up.
				boolean temporalWasEnabled = false;
				if (schemaInfo.isTemporal()) {
					String fullPath = GridUtil.getFullPath(schemaInfo.getGridPath());
					TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
					if (tm != null) {
						temporalWasEnabled = tm.isEnabled();
						if (temporalWasEnabled) {
							// Block till done
							tm.setEnabled(false, false, false /* spawnThread */);
						}
					}
				}

				// Load data
				loader.load(schemaInfo, dataFile);

				// Enable temporal indexing if disabled at the start of this
				// method
				if (schemaInfo.isTemporal()) {
					if (temporalWasEnabled) {
						String fullPath = GridUtil.getFullPath(schemaInfo.getGridPath());
						TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
						if (tm != null) {
							if (temporalWasEnabled) {
								// Block till done
								tm.setEnabled(true, true, false /* spawnThread */);
							}
						}
					}
				}
			}
		}
		return dir.getAbsolutePath();
	}

	@BizMethod
	public CacheDumpInfo getCacheDumpInfoList(boolean isAll)
	{
		return getCacheDumpInfo(isAll);
	}

	@BizMethod
	public CacheDumpInfo getCacheDumpInfo(boolean isAll)
	{
		List<File> datedDirList = getDatedDirList(isAll);
		if (datedDirList == null) {
			return null;
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
		List<DumpInfo> dumpInfoList = new ArrayList<DumpInfo>(datedDirList.size());
		String rootFullPath = PadoServerManager.getPadoServerManager().getGridInfo().getRootPathInfo().getFullPath();
		String rootName = rootFullPath.substring(1);
		for (File datedDir : datedDirList) {
			String timestampStr = datedDir.getName();
			try {
				Date date = formatter.parse(timestampStr);
				File[] files = datedDir.listFiles();
				for (File file : files) {
					if (file.getName().equals(rootName)) {
						DumpInfo dumpInfo = InfoFactory.getInfoFactory().createDumpInfo("/", file, date, true);
						// Overwrite the date with the file name which is the
						// correct
						// time stamp.
						dumpInfoList.add(dumpInfo);
						break;
					}
				}

			} catch (ParseException e) {
				// ignore (skips bad date format. this should never happen)
			}
		}
		CacheDumpInfo cdi = GemfireInfoFactory.getInfoFactory().createCacheDumpInfo(dumpInfoList);
		return cdi;
	}

	/**
	 * Returns a list of dated directories.
	 * 
	 * @param isAll
	 *            true to include "all" directories, false to include "path"
	 *            directories.
	 * @return null if "all" or "path" sub-directories in the dump director is
	 *         not found.
	 */
	private List<File> getDatedDirList(boolean isAll)
	{
		// Make sure the dump directory exists
		String dumpDir = PadoUtil.getProperty(Constants.PROP_DUMP_DIR, Constants.DEFAULT_DUMP_DIR);
		File dir;
		if (isAll) {
			dir = new File(dumpDir, "all");
		} else {
			dir = new File(dumpDir, "path");
		}
		if (dir.isDirectory() == false || dir.exists() == false) {
			return null;
		}

		// Determine the latest dump directory by searching the sorted
		// sub-directories under the dump directory.
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
		String[] fileNames = dir.list();
		Arrays.sort(fileNames);
		List<File> datedDirList = new ArrayList<File>();
		for (int i = 0; i < fileNames.length; i++) {
			try {
				File datedDir = new File(dir, fileNames[i]);
				if (datedDir.isDirectory()) {
					formatter.parse(fileNames[i]);
					datedDirList.add(datedDir);
				}
			} catch (ParseException ex) {
				// ignore
			}
		}

		return datedDirList;
	}

	// public List<String> getDumpList(boolean isAll)
	// {
	// // Make sure the dump directory exists
	// String dumpDir = PadoUtil.getProperty(Constants.PROP_DUMP_DIR,
	// Constants.DEFAULT_DUMP_DIR);
	// File dir;
	// if (isAll) {
	// dir = new File(dumpDir, "all");
	// } else {
	// dir = new File(dumpDir, "path");
	// }
	// if (dir.isDirectory() == false || dir.exists() == false) {
	// return null;
	// }
	//
	// // Determine the latest dump directory by searching the sorted
	// // sub-directories under the dump directory.
	// SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
	// String[] fileNames = dir.list();
	// Arrays.sort(fileNames);
	// List<String> filePathList = new ArrayList<String>();
	// for (int i = 0; i < fileNames.length; i++) {
	// try {
	// File datedDir = new File(dir, fileNames[i]);
	// if (datedDir.isDirectory()) {
	// formatter.parse(fileNames[i]);
	// filePathList = findAllDumpFiles(datedDir, filePathList);
	// }
	// } catch (ParseException ex) {
	// // ignore
	// }
	// }
	// return filePathList;
	// }
	//
	// private List<String> findAllDumpFiles(File datedDir, List<String>
	// filePathList)
	// {
	// filePathList.add(datedDir.getAbsolutePath());
	// File files[] = datedDir.listFiles();
	// Arrays.sort(files);
	// for (File file : files) {
	// if (file.isDirectory()) {
	// findAllDumpFiles(file, filePathList);
	// } else {
	// filePathList.add(file.getAbsolutePath());
	// }
	// }
	// return filePathList;
	// }

	@BizMethod
	public void setCompositeKeyInfo(String gridPath, CompositeKeyInfo compositeKeyInfo)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		RegionUtil.setCompositeKeyInfoForIdentityKeyPartionResolver(fullPath, compositeKeyInfo);
	}

	@BizMethod
	public CompositeKeyInfo getCompositeKeyInfo(String gridPath)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		return RegionUtil.getCompositeKeyInfoForIdentityKeyPartionResolver(fullPath);
	}
}