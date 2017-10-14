package com.netcrest.pado.index.gemfire.internal.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.netcrest.pado.index.internal.db.LocalResultsDb;
import com.netcrest.pado.index.internal.db.RecordFile;
import com.netcrest.pado.index.internal.db.RecordHeader;
import com.netcrest.pado.index.service.GridQuery;

public class GemfireLocalResultsDb extends LocalResultsDb
{
	private final static GemfireLocalResultsDb db = new GemfireLocalResultsDb();
	
	public GemfireLocalResultsDb()
	{
	}
	
	public static LocalResultsDb getLocalResultsDb()
	{
		return db;
	}
	
	private static String getValidFileName(String id)
	{
		// TODO: This is not unique. Need more than the id.
		// TODO: Use the id as hash to generate the file name and have the db to expire the
		// hash upon idle.
		return id.replaceAll("[\\{\\}\\+\\/\\,\\*:\"\\?'^&%\\$#@!\\\\(\\)\\[\\]]", "_");
	}
	
	public List<RecordHeader> writeResults(GridQuery criteria, List<?> resultsList) throws IOException
	{
		RecordFile rf = new GemfireRecordFile();
		File file = createFile(getValidFileName(criteria.getId()));
		return rf.writeRecords(file, resultsList);
	}
	
	public List<byte[]> getSerializedResults(List<RecordHeader> recordHeaderList, GridQuery criteria) throws IOException
	{
		if (recordHeaderList == null) {
			return null;
		}
		RecordFile rf = new GemfireRecordFile();
		File file = createFile(getValidFileName(criteria.getId()));
		List<byte[]> serializedRecordList = rf.readSerializedRecords(file, recordHeaderList, criteria.getStartIndex(), criteria.getStartIndex() + criteria.getAggregationPageSize() - 1);
		
		return serializedRecordList;
	}
}