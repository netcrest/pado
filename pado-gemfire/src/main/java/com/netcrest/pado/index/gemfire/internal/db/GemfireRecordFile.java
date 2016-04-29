package com.netcrest.pado.index.gemfire.internal.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.gemstone.gemfire.internal.util.BlobHelper;
import com.netcrest.pado.index.internal.db.RecordFile;
import com.netcrest.pado.index.internal.db.RecordHeader;

public class GemfireRecordFile extends RecordFile
{
	public GemfireRecordFile()
	{
	}

	@Override
	public List<RecordHeader> writeRecords(File file, List<?> resultsList) throws IOException
	{
		ArrayList<RecordHeader> recordHeaderList = null;
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		try {
			writeHeader(raf, resultsList);
			int index = 0;
			long filePosition = getRecordStartPosition();
			raf.seek(filePosition);
			recordHeaderList = new ArrayList<RecordHeader>(resultsList.size());
			for (Object object : resultsList) {
				byte[] serializedRecord = BlobHelper.serializeToBlob(object);
				filePosition = raf.getFilePointer();
				RecordHeader recordHeader = new RecordHeader(index, filePosition, serializedRecord.length);
				// If writeRcordHeader is invoked then file position must be
				// added in both
				// writeRecordHeader() and writeRecord()
				// writeRecordHeader(raf, recordHeader);
				writeRecord(raf, serializedRecord);
				recordHeaderList.add(recordHeader);
				index++;
			}
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
		return recordHeaderList;
	}
}
