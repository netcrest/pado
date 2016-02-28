package com.netcrest.pado.test.biz.impl.gemfire;

import java.util.ArrayList;
import java.util.List;

import com.netcrest.pado.data.jsonlite.JsonLite;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class StressTestUtil
{
	public static JsonLite createObject(int fieldCount, int fieldSize)
	{
		JsonLite jl = new JsonLite();
		for (int i = 0; i < fieldCount; i++) {
			jl.put("f" + i, createField(fieldSize));
		}
		return jl;
	}

	public static String createField(int fieldSize)
	{
		StringBuffer buffer = new StringBuffer(fieldSize);
		for (int i = 0; i < fieldSize; i++) {
			buffer.append('a');
		}
		return buffer.toString();
	}

	/**
	 * Creates and returns a batch of JsonLite objects.
	 * 
	 * @param batchSize
	 *            Bash size
	 * @param fieldCount
	 *            Field count in each JsontLite object
	 * @param fieldSize
	 *            Size of each field in JsonLite object
	 * @return
	 */
	public static List<JsonLite> createBatchOfObjects(int batchSize, int fieldCount, int fieldSize)
	{
		ArrayList<JsonLite> list = new ArrayList<JsonLite>(batchSize);
		for (int i = 0; i < batchSize; i++) {
			JsonLite jl = createObject(fieldCount, fieldSize);
			list.add(jl);
		}
		return list;
	}
}
