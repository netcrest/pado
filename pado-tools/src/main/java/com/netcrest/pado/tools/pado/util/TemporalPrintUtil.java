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
package com.netcrest.pado.tools.pado.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalHelper;

/**
 * TemporalPrintUtil is provides static methods for printing temporal data.
 */
public class TemporalPrintUtil
{
	private static final String INDEX_HEADER = "Row";
	private static final String NAME_HEADER = "Name";
	private static final String REGION_PATH_HEADER = "RegionPath";
	private static final String ATTACHMENT_VALUE_HEADER = "AttachmentValue";
	private static final String HOST_VALUE_HEADER = "HostValue";
	private static final String VALUE_HEADER = "Value";
	private static final String IDENTITY_KEY_HEADER = "IdentityKey";
	private static final String START_VALID_TIME_HEADER = "StartValidTime";
	private static final String END_VALID_TIME_HEADER = "EndValidTime";
	private static final String WRITTEN_TIME_HEADER = "WrittenTime";


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void dump(TemporalDataList tdl, boolean rawFormat) {

		List<String> headerList = new ArrayList<String>();
		List<List<Object>> resultList = new ArrayList<List<Object>>();

		SimpleDateFormat formatter = null;
		if (rawFormat == false) {
			formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss.SSS");
		}

		int curSize = tdl.getTemporalList().size();

		String svt, evt, wt;

		headerList.add(INDEX_HEADER);
		headerList.add(IDENTITY_KEY_HEADER);
		headerList.add(START_VALID_TIME_HEADER);
		headerList.add(END_VALID_TIME_HEADER);
		headerList.add(WRITTEN_TIME_HEADER);
		headerList.add(VALUE_HEADER);

		List<Object> valueArr = null;
		for (int i = 0; i < curSize; i++) {
			valueArr = new ArrayList<Object>();
			ArrayList<TemporalEntry> list = tdl.getTemporalList();
			TemporalEntry entry = list.get(i);
			if (entry.getTemporalKey().getStartValidTime() == Long.MAX_VALUE) {
				svt = "&";
			} else if (rawFormat) {
				svt = entry.getTemporalKey().getStartValidTime() + "";
			} else {
				svt = formatter.format(new Date(entry.getTemporalKey()
						.getStartValidTime()));
			}
			if (entry.getTemporalKey().getEndValidTime() == Long.MAX_VALUE) {
				evt = "&";
			} else if (rawFormat) {
				evt = entry.getTemporalKey().getEndValidTime() + "";
			} else {
				evt = formatter.format(new Date(entry.getTemporalKey()
						.getEndValidTime()));
			}

			if (rawFormat) {
				wt = entry.getTemporalKey().getWrittenTime() + "";
			} else {
				wt = formatter.format(new Date(entry.getTemporalKey()
						.getWrittenTime()));
			}
			valueArr.add(Integer.valueOf(i + 1));
			valueArr.add(entry.getTemporalKey().getIdentityKey());
			valueArr.add(svt);
			valueArr.add(evt);
			valueArr.add(wt);

			Object value = null;
			if (entry.getTemporalData().__getTemporalValue() != null) {
				if (entry.getTemporalData().__getTemporalValue().isDelta()) {
					value = "delta";
				} else {

					TemporalHelper.deserializeAttachments(entry
							.getTemporalData().__getTemporalValue());
					TemporalHelper.deserializeData(entry.getTemporalData()
							.__getTemporalValue());
					value = entry.getTemporalData();
				}
			}

			if (value != null) {
				valueArr.add(value);
			} else {
				valueArr.add(null);
			}
			resultList.add(valueArr);
		}
		PrintUtil.printList(resultList, headerList, headerList.size() - 2);
	}

	public static void printTemporalObject(Object obj) {

		List<String> headerList = new ArrayList<String>();
		headerList.add(VALUE_HEADER);

		List<List<Object>> resultList = new ArrayList<List<Object>>();

		ITemporalData tempData = (ITemporalData) obj;
		ITemporalValue tempValue = tempData.__getTemporalValue();
		ITemporalKey tempKey = tempValue.getTemporalKey();

		List<Object> valueArr = new ArrayList<Object>();

		Object value = null;

		if (tempValue.isDelta()) {
			value = "delta";
		} else {
			TemporalHelper.deserializeAttachments(((ITemporalData) obj)
					.__getTemporalValue());
			TemporalHelper.deserializeData(((ITemporalData) obj)
					.__getTemporalValue());
			value = obj;
		}
		valueArr.add(value);
		resultList.add(valueArr);
		PrintUtil.printList(resultList, headerList, headerList.size() + 1);
	}
	
	public static void printObject(Object obj) {

		List<String> headerList = new ArrayList<String>();
		headerList.add(VALUE_HEADER);

		List<List<Object>> resultList = new ArrayList<List<Object>>();

		List<Object> valueArr = new ArrayList<Object>();

		Object value = obj;
		valueArr.add(value);
		resultList.add(valueArr);
		PrintUtil.printList(resultList, headerList, headerList.size() + 1);
	}

	public static void printAttachments(Map<String, List<Object>> map,
			String fullPath, Object hostvalue) {
		List<String> headerList = new ArrayList<String>();

		headerList.add(INDEX_HEADER);
		headerList.add(NAME_HEADER);
		headerList.add(REGION_PATH_HEADER);
		headerList.add(HOST_VALUE_HEADER);
		headerList.add(ATTACHMENT_VALUE_HEADER);

		List<List<Object>> resultList = new ArrayList<List<Object>>();

		int i = 0;

		List<Object> attachments = null;
		List<Object> valueArr = new ArrayList<Object>();

		valueArr.add(++i);
		valueArr.add("");
		valueArr.add(fullPath);
		if (hostvalue != null) {
			TemporalHelper.deserializeAttachments(((ITemporalData) hostvalue)
					.__getTemporalValue());
			TemporalHelper.deserializeData(((ITemporalData) hostvalue)
					.__getTemporalValue());
		}
		valueArr.add(hostvalue);
		valueArr.add("");
		resultList.add(valueArr);
		if (map != null) {
			for (String key : map.keySet()) {
				attachments = map.get(key);

				for (Object obj : attachments) {
					valueArr = new ArrayList<Object>();
					valueArr.add(++i);
					valueArr.add(key);
					valueArr.add(fullPath);
					valueArr.add("");

					ITemporalData tempData = (ITemporalData) obj;
					ITemporalValue tempValue = tempData.__getTemporalValue();
					if (tempValue.isDelta()) {
						obj = "delta";
					} else {
						TemporalHelper
								.deserializeAttachments(((ITemporalData) obj)
										.__getTemporalValue());
						TemporalHelper.deserializeData(((ITemporalData) obj)
								.__getTemporalValue());
					}
					valueArr.add(obj);
					resultList.add(valueArr);
				}
			}
		}
		PrintUtil.printList(resultList, headerList, headerList.size() - 3);
	}

	public static void printAttachments(List<Object> objects, String path) {
		List<String> headerList = new ArrayList<String>();

		headerList.add(INDEX_HEADER);
		headerList.add(NAME_HEADER);
		headerList.add(REGION_PATH_HEADER);
		headerList.add(ATTACHMENT_VALUE_HEADER);

		List<List<Object>> resultList = new ArrayList<List<Object>>();
		int i = 0;
		AttachmentSet<Object> attachments = null;
		List<Object> valueArr = null;
		for (Object obj : objects) {

			valueArr = new ArrayList<Object>();
			valueArr.add(++i);
			valueArr.add("");
			valueArr.add(path);
			ITemporalData tempData = (ITemporalData) obj;
			ITemporalValue tempValue = tempData.__getTemporalValue();
			if (tempValue.isDelta()) {
				obj = "delta";
			} else {
				TemporalHelper.deserializeAttachments(((ITemporalData) obj)
						.__getTemporalValue());
				TemporalHelper.deserializeData(((ITemporalData) obj)
						.__getTemporalValue());
			}
			valueArr.add(obj);
			resultList.add(valueArr);
		}
		PrintUtil.printList(resultList, headerList, headerList.size() - 2);
	}

	public static void printAttachments(Set<Object>[] sets, String path) {
		List<String> headerList = new ArrayList<String>();

		headerList.add(INDEX_HEADER);
		headerList.add(NAME_HEADER);
		headerList.add(REGION_PATH_HEADER);
		headerList.add(ATTACHMENT_VALUE_HEADER);

		List<List<Object>> resultList = new ArrayList<List<Object>>();
		int i = 0;
		AttachmentSet<Object> attachments = null;
		List<Object> valueArr = null;
		for (Set set : sets) {
			for (Object obj : set) {

				valueArr = new ArrayList<Object>();
				valueArr.add(++i);
				valueArr.add("");
				valueArr.add(path);
				ITemporalData tempData = (ITemporalData) obj;
				ITemporalValue tempValue = tempData.__getTemporalValue();
				if (tempValue.isDelta()) {
					obj = "delta";
				} else {
					TemporalHelper.deserializeAttachments(((ITemporalData) obj)
							.__getTemporalValue());
					TemporalHelper.deserializeData(((ITemporalData) obj)
							.__getTemporalValue());
				}
				valueArr.add(obj);
				resultList.add(valueArr);
			}
		}
		PrintUtil.printList(resultList, headerList, headerList.size() - 2);
	}
}
