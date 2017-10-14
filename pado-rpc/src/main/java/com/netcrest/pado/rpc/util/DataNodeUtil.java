package com.netcrest.pado.rpc.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.biz.file.CompositeKeyInfo;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.gemfire.util.RegionUtil;
import com.netcrest.pado.internal.util.OutputUtil;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.util.GridUtil;

public class DataNodeUtil
{
	/**
	 * Dumps the contents of the temporal list of the specified identity key in
	 * the specified grid path of data node.
	 * 
	 * @param topDir
	 *            Top-level directory in which the grid path contents are dumped
	 *            in CSV file.
	 * @param fileNamePostfix
	 *            File name postfix.
	 * @param gridPath
	 *            Grid path. If nested, then CSV file is created in the parent
	 *            directory.
	 * @param identityKey
	 *            Identity key.
	 * @return Dumped file path. Null if the local data set is empty or the grid
	 *         path is not defined.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static File dumpTemporalList(File topDir, String fileNamePostfix, String gridPath,
			String identityKey, boolean isIncludeColumnHeader)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
		if (tm == null) {
			return null;
		}
		ITemporalList list = tm.getTemporalList(identityKey);
		if (list == null) {
			return null;
		}
		TemporalDataList tdl = list.getTemporalDataList();
		List<TemporalEntry<ITemporalKey, ITemporalData>> tl = tdl.getTemporalList();

		String serverName = PadoServerManager.getPadoServerManager().getServerName();
		String fileRelativePath = fullPath + "/" + identityKey + "." + serverName + "." + fileNamePostfix;
		String schemaRelativePath = fileRelativePath + ".schema";
		String csvRelativePath = fileRelativePath + ".csv";
		File fileDir = new File(topDir, fullPath).getParentFile();
		if (fileDir.exists() == false) {
			fileDir.mkdirs();
		}
		File schemaFile = new File(topDir, schemaRelativePath);
		File csvFile = new File(topDir, csvRelativePath);
		PrintWriter schemaWriter = null;
		PrintWriter csvWriter = null;
		try {
			Object key = null;
			Object data = null;

			// Get the first entry in the region.
			Iterator<TemporalEntry<ITemporalKey, ITemporalData>> iterator = tl.iterator();
			if (iterator.hasNext()) {
				TemporalEntry<ITemporalKey, ITemporalData> te = iterator.next();
				key = te.getTemporalKey();
				data = te.getTemporalData();
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
			CompositeKeyInfo compositeKeyInfo = RegionUtil.getCompositeKeyInfoForIdentityKeyPartionResolver(fullPath);
			OutputUtil.printSchema(schemaWriter, gridPath, key, data, keyList, OutputUtil.TYPE_KEYS_VALUES, ",",
					iso8601DateFormat, true, true, compositeKeyInfo);
			schemaWriter.flush();

			OutputUtil.printList(csvWriter, tl, ",", OutputUtil.TYPE_KEYS_VALUES, iso8601DateFormat, isIncludeColumnHeader);
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

		return csvFile;
	}
}
