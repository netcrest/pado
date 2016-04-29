package com.netcrest.pado.index.internal.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * LocalResultsDb off-loads query results to the server file system to reduce
 * the memory footprint.
 * 
 * @author dpark
 *
 */
public class LocalResultsDb
{
	private static LocalResultsDb db;

	private static String LOCAL_RESULTS_DB_DIR = PadoUtil.getProperty("index.local.results.dir", "db/index");

	static {
		try {
			Class<?> clazz = PadoUtil.getClass(Constants.PROP_CLASS_LOCAL_RESULTS_DB,
					Constants.DEFAULT_CLASS_LOCAL_RESULTS_DB);
			db = (LocalResultsDb) clazz.newInstance();
		} catch (Exception ex) {
			Logger.severe(ex);
		}
		File dir = new File(LOCAL_RESULTS_DB_DIR);
		if (dir.exists() == false) {
			dir.mkdirs();
		}
	}

	protected LocalResultsDb()
	{
	}

	public static LocalResultsDb getLocalResultsDb()
	{
		return db;
	}

	protected File createFile(String resultId)
	{
		String fileName;
		fileName = resultId.replaceAll("\\/", "_");
		File file = new File(LOCAL_RESULTS_DB_DIR, fileName);
		return file;
	}

	/**
	 * Writes the specified results to the file system.
	 * 
	 * @param criteria
	 *            Grid criteria
	 * @param resultsList
	 *            Results
	 * @return List of RecordHeader objects containing file offset information.
	 * @throws IOException
	 *             Thrown if IO error occurs
	 */
	public List<RecordHeader> writeResults(GridQuery criteria, List<?> resultsList) throws IOException
	{
		RecordFile rf = new RecordFile();
		File file = createFile(criteria.getId());
		return rf.writeRecords(file, resultsList);
	}

	/**
	 * Returns serialized results read from the files system.
	 * 
	 * @param recordHeaderList
	 *            Record header list that contains all of record headers.
	 * @param id
	 *            Unique Id identifying the results.
	 * @param startIndex
	 *            Start index of the results
	 * @param endIndex
	 *            End index of the results
	 * @throws IOException
	 *             Thrown if IO error occurs
	 */
	public List<byte[]> getSerializedResults(List<RecordHeader> recordHeaderList, String id, int startIndex,
			int endIndex) throws IOException
	{
		if (recordHeaderList == null) {
			return null;
		}
		RecordFile rf = new RecordFile();
		File file = createFile(id);
		List<byte[]> serializedRecordList = rf.readSerializedRecords(file, recordHeaderList, startIndex, endIndex);

		return serializedRecordList;
	}

	/**
	 * Returns serialized results read from the file system.
	 * 
	 * @param recordHeaderList
	 *            Record header list that contains all of record headers.
	 * @param criteria
	 *            Grid criteria containing index information
	 * @throws IOException
	 *             Thrown if IO error occurs
	 */
	public List<byte[]> getSerializedResults(List<RecordHeader> recordHeaderList, GridQuery criteria) throws IOException
	{
		return getSerializedResults(recordHeaderList, criteria.getId(), criteria.getStartIndex(),
				criteria.getStartIndex() + criteria.getAggregationPageSize() - 1);
	}
}