package com.netcrest.pado.index.internal.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.netcrest.pado.io.ObjectSerializer;

/**
 * RecordFile writes to or reads from the specified query results file.
 * 
 * @author dpark
 *
 */
public class RecordFile
{
	private long recordStartPosition;

	public RecordFile()
	{
	}

	/**
	 * Writes the header information to the file.
	 * 
	 * @param raf
	 *            RandomAccessFile object
	 * @param resultsList
	 *            Results
	 * @throws IOException
	 *             Thrown if IO error occurs
	 */
	protected void writeHeader(RandomAccessFile raf, List<?> resultsList) throws IOException
	{
		raf.seek(0);
		int numRecords = resultsList.size();
		long dataStartPointer = 4 + numRecords * RecordHeader.RECORD_HEADER_SIZE;
		raf.writeInt(numRecords);
		raf.writeLong(dataStartPointer);
		recordStartPosition = raf.getFilePointer();
	}

	/**
	 * Writes the specified record header to the file.
	 * 
	 * @param raf
	 *            RandomAccessFile object
	 * @param recordHeader
	 *            RecordHeader object to be written
	 * @throws IOException
	 *             Thrown if IO error occurs
	 */
	protected void writeRecordHeader(RandomAccessFile raf, RecordHeader recordHeader) throws IOException
	{
		raf.writeInt(recordHeader.getIndex());
		raf.writeLong(recordHeader.getFilePosition());
		raf.writeInt(recordHeader.getNumBytes());
		recordStartPosition = raf.getFilePointer();
	}

	protected void writeRecord(RandomAccessFile raf, byte[] serializedRecord) throws IOException
	{
		raf.write(serializedRecord);
	}

	/**
	 * Returns the record data start position.
	 */
	protected long getRecordStartPosition()
	{
		return recordStartPosition;
	}

	/**
	 * Writes the specified results to the specifies file.
	 * 
	 * @param file
	 *            File to write to
	 * @param resultsList
	 *            Query results
	 * @return List of RecordHeader objects containing file offset information.
	 * @throws IOException
	 *             Thrown if IO error occurs
	 */
	public List<RecordHeader> writeRecords(File file, List<?> resultsList) throws IOException
	{
		ArrayList<RecordHeader> recordHeaderList = null;
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		try {
			writeHeader(raf, resultsList);
			int index = 0;
			long filePosition = raf.length();
			recordHeaderList = new ArrayList<RecordHeader>(resultsList.size());
			for (Object object : resultsList) {
				byte[] serializedRecord = ObjectSerializer.serialize(object);
				RecordHeader recordHeader = new RecordHeader(index, filePosition, serializedRecord.length);
				// If writeRcordHeader is invoked then file position must be
				// added in both
				// writeRecordHeader() and writeRecord()
				// writeRecordHeader(raf, recordHeader);
				writeRecord(raf, serializedRecord);
				recordHeaderList.add(recordHeader);
				index++;
				filePosition += serializedRecord.length;
			}
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
		return recordHeaderList;
	}

	/**
	 * Reads the serialized results from the specified file.
	 * 
	 * @param file
	 *            File to read
	 * @param recordHeaderList
	 *            ist of RecordHeader objects containing file offset information
	 * @param startIndex
	 *            Start index
	 * @param endIndex
	 *            End index
	 * @return List of serialized objects read from the file.
	 * @throws IOException
	 *             Thrown if IO error occurs
	 */
	public List<byte[]> readSerializedRecords(File file, List<RecordHeader> recordHeaderList, int startIndex,
			int endIndex) throws IOException
	{
		ArrayList<byte[]> recordList = null;
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		try {
			raf.seek(0);
			int numRecords = raf.readInt();
			// long dataStartPointer = raf.readLong();

			if (startIndex < 0) {
				startIndex = 0;
			}
			if (endIndex >= numRecords) {
				endIndex = numRecords - 1;
			}

			recordList = new ArrayList<byte[]>(endIndex - startIndex + 1);
			for (int i = startIndex; i <= endIndex; i++) {
				RecordHeader recordHeader = recordHeaderList.get(i);
				raf.seek(recordHeader.getFilePosition());
				byte[] serializedRecord = new byte[recordHeader.getNumBytes()];
				raf.read(serializedRecord);
				recordList.add(serializedRecord);
			}
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
		return recordList;
	}
}
