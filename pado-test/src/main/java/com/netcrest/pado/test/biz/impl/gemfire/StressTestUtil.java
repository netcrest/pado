package com.netcrest.pado.test.biz.impl.gemfire;

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
}
