package com.netcrest.pado.index.gemfire.internal.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.netcrest.pado.index.internal.db.RecordFile;
import com.netcrest.pado.index.internal.db.LocalResultsDb;
import com.netcrest.pado.index.internal.db.RecordHeader;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.internal.util.PadoUtil;

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
	
	public List<RecordHeader> writeResults(GridQuery criteria, List<?> resultsList) throws IOException
	{
		RecordFile rf = new GemfireRecordFile();
		File file = createFile(criteria.getId());
		return rf.writeRecords(file, resultsList);
	}
	
	public List<byte[]> getSerializedResults(List<RecordHeader> recordHeaderList, GridQuery criteria) throws IOException
	{
		if (recordHeaderList == null) {
			return null;
		}
		RecordFile rf = new GemfireRecordFile();
		File file = createFile(criteria.getId());
		List<byte[]> serializedRecordList = rf.readSerializedRecords(file, recordHeaderList, criteria.getStartIndex(), criteria.getStartIndex() + criteria.getAggregationPageSize() - 1);
		
		return serializedRecordList;
	}
}