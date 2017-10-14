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
package com.netcrest.pado.temporal;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TemporalDataList contains a snapshot of a temporal list. It provides
 * information on location and the complete change history of an identity key in
 * terms of deltas.
 * 
 * @author dpark
 * 
 * @param <K>
 * @param <V>
 */
public abstract class TemporalDataList<K, V>
{
	/**
	 * Identity key
	 */
	protected Object identityKey;

	/**
	 * Last temporal entry
	 */
	protected TemporalEntry<K, V> lastEntry;

	/**
	 * Temporal list
	 */
	protected ArrayList<TemporalEntry<K, V>> temporalList;

	/**
	 * ID of the bucket that is responsible for the temporal list
	 */
	protected int bucketId;

	/**
	 * ID of the member that hosts the temporal list
	 */
	protected String memberId;

	/**
	 * Name of the member that hosts the temporal list
	 */
	protected String memberName;

	/**
	 * Host (or IP address) in which the member is running
	 */
	protected String host;

	/**
	 * Full path of the temporal list
	 */
	protected String fullPath;

	/**
	 * Constructs an empty TemporalDataList object.
	 */
	public TemporalDataList()
	{
	}

	/**
	 * Constructs a TemporalDataList object with the specified parameters.
	 * 
	 * @param identityKey
	 *            Identity key
	 * @param lastEntry
	 *            Last temporal entry
	 * @param temporalList
	 *            Temporal list
	 * @param bucketId
	 *            ID of the bucket that is responsible for the temporal list
	 * @param memberId
	 *            ID of the member that hosts the temporal list
	 * @param memberName
	 *            Name of the member that hosts the temporal list
	 * @param host
	 *            Host (or IP address) in which the member is running
	 * @param fullPath
	 *            Full path of the temporal list
	 */
	public TemporalDataList(Object identityKey, TemporalEntry<K, V> lastEntry,
			ArrayList<TemporalEntry<K, V>> temporalList, int bucketId, String memberId, String memberName, String host,
			String fullPath)
	{
		this.identityKey = identityKey;
		this.lastEntry = lastEntry;
		this.temporalList = temporalList;
		this.bucketId = bucketId;
		this.memberId = memberId;
		this.memberName = memberName;
		this.host = host;
		this.fullPath = fullPath;
	}

	/**
	 * Prints the temporal list to the console in raw time (msec) format.
	 */
	public void dump()
	{
		dump(System.out, null);
	}

	/**
	 * Prints the temporal list to the console in the specified date format.
	 * 
	 * @param formatter
	 *            Date formatter.
	 */
	public void dump(SimpleDateFormat formatter)
	{
		dump(System.out, formatter);
	}

	/**
	 * Prints the temporal list to the specified output stream in the specified
	 * date format.
	 * 
	 * @param printStream
	 *            Output stream. If null, then it prints to the console.
	 * @param formatter
	 *            Date formatter. If null, it prints in raw time (msec).
	 */
	public void dump(PrintStream printStream, SimpleDateFormat formatter)
	{
		if (printStream == null) {
			printStream = System.out;
		}
		lastEntry.getTemporalData().__getTemporalValue().deserializeAll();
		printStream.println();
		printStream.println("=====================================================");
		printStream.println("Member Name: " + memberName);
		printStream.println("  Member Id: " + memberId);
		printStream.println("       Host: " + host);
		printStream.println("  Full path: " + fullPath);
		printStream.println("  Bucket Id: " + bucketId);
		printStream.println("IdentityKey: " + identityKey);
		printStream.println(" Last value: " + lastEntry);
		printStream.println("    Removed: " + isRemoved());

		if (temporalList == null) {
			printStream.println("TemporalList empty");
		} else {
			// never modify in place
			int curSize = temporalList.size();
			String sv, ev, wt;
			printStream.println("Index IdentityKey  StartValid  EndValidTime  WrittenTime  Value AttachmentKeys");
			printStream.println("----- -----------  ----------  ------------  -----------  ----- ---------");
			for (int i = 0; i < curSize; i++) {
				TemporalEntry entry = temporalList.get(i);
				if (entry.getTemporalKey().getStartValidTime() == Long.MAX_VALUE) {
					sv = "&";
				} else if (formatter == null) {
					sv = entry.getTemporalKey().getStartValidTime() + "";
				} else {
					sv = formatter.format(new Date(entry.getTemporalKey().getStartValidTime()));
				}
				if (entry.getTemporalKey().getEndValidTime() == Long.MAX_VALUE) {
					ev = "&";
				} else if (formatter == null) {
					ev = entry.getTemporalKey().getEndValidTime() + "";
				} else {
					ev = formatter.format(new Date(entry.getTemporalKey().getEndValidTime()));
				}

				if (formatter == null) {
					wt = entry.getTemporalKey().getWrittenTime() + "";
				} else {
					wt = formatter.format(new Date(entry.getTemporalKey().getWrittenTime()));
				}
				if (entry.getTemporalData() == null) {

					printStream.println(entry.getTemporalKey().getIdentityKey() + "  " + sv + "  " + ev + "  " + wt
							+ "  null" + "  null");
				} else {
					Object value;
					if (entry.getTemporalData().__getTemporalValue().isDelta()) {
						value = "delta";
					} else {
						value = entry.getTemporalData();
						if (value instanceof TemporalData) {
							value = ((TemporalData) value).getValue();
						} else if (value instanceof ITemporalDataNull) {
							value = "null";
						}
					}
					if (entry.getTemporalData().__getTemporalValue().getAttachmentMap() != null) {
						printStream.println(i + "  " + entry.getTemporalKey().getIdentityKey() + "  " + sv + "  " + ev
								+ "  " + wt + "  " + value);
					} else {
						printStream.println(i + "  " + entry.getTemporalKey().getIdentityKey() + "  " + sv + "  " + ev
								+ "  " + wt + "  " + value + "  "
								+ entry.getTemporalData().__getTemporalValue().getAttachmentMap());
					}
				}
			}
		}
		printStream.println("=====================================================");
	}

	/**
	 * Returns the identity key
	 */
	public Object getIdentityKey()
	{
		return identityKey;
	}

	/**
	 * Returns the first entry in the temporal list.
	 */
	public TemporalEntry getFirstEntry()
	{
		if (temporalList == null || temporalList.size() == 0) {
			return null;
		} else {
			return temporalList.get(0);
		}
	}

	/**
	 * Returns the last entry in the temporal list.
	 */
	public TemporalEntry getLastEntry()
	{
		return lastEntry;
	}
	
	/**
	 * Returns true if this temporal list has been marked as "removed".
	 */
	public boolean isRemoved()
	{
		if (temporalList == null || temporalList.size() == 0) {
			return false;
		}
		TemporalEntry entry = temporalList.get(temporalList.size() - 1);
		return entry.getTemporalKey().getStartValidTime() == -1 && entry.getTemporalKey().getEndValidTime() == -1;
	}

	/**
	 * Returns the temporal list.
	 */
	public ArrayList<TemporalEntry<K, V>> getTemporalList()
	{
		return temporalList;
	}
	
	public List<TemporalEntry<K, V>> getDeltaAppliedTemporalList()
	{
		return null;
	}

	/**
	 * Returns the ID of the bucket that is responsible for the temporal list.
	 */
	public int getBucketId()
	{
		return bucketId;
	}

	/**
	 * Returns the ID of the member that hosts the temporal list.
	 */
	public String getMemberId()
	{
		return memberId;
	}

	/**
	 * Returns the name of the member that hosts the temporal list.
	 */
	public String getMemberName()
	{
		return memberName;
	}

	/**
	 * Returns the host (or IP address) in which the member is running.
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Returns the full path of the temporal list.
	 */
	public String getFullPath()
	{
		return fullPath;
	}
	
}
