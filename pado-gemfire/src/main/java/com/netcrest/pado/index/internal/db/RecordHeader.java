package com.netcrest.pado.index.internal.db;

/**
 * RecordHeader contains record information, i.e., index, file offset, and
 * number of bytes.
 * 
 * @author dpark
 *
 */
public class RecordHeader
{
	public final static long RECORD_HEADER_SIZE = 16; // int + long + int

	private int index;
	private long filePosition;
	private int numBytes;

	public RecordHeader(int index, long filePosition, int numBytes)
	{
		super();
		this.index = index;
		this.filePosition = filePosition;
		this.numBytes = numBytes;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public long getFilePosition()
	{
		return filePosition;
	}

	public void setFilePosition(long filePosition)
	{
		this.filePosition = filePosition;
	}

	public int getNumBytes()
	{
		return numBytes;
	}

	public void setNumBytes(int numBytes)
	{
		this.numBytes = numBytes;
	}
}
