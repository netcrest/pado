package com.netcrest.pado.test.junit.jsonlite;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;

public class JsonLiteTest
{
	@SuppressWarnings("rawtypes")
	@Test
	public void testJsonString()
	{
		String str = "{\"result\":{\"filePath\":\"/home/dpark/Work/git/pado/deploy/pado_0.4.1-B4/run/server-mygrid-us01/dump/path/201709151728/mygrid\",\"dataList\":[{\"Zip\":\"06611\",\"City\":\"Trumbull\",\"Date\":\"2017-09-15T21:27:58.416Z\",\"Name\":\"Park\",\"State\":\"CT\",\"Street\":\"6 Old Tree Farm Lane\"}]},\"id\":\"794425785219470\",\"jsonrpc\":\"2.0\"}";
		JsonLite jl = new JsonLite(str);
		System.out.println(jl.toString(4, false, false));
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testJsonString2()
	{
		String str ="{\"result\":{\"dataList\":[{\"value\":{\"SalesHistoryId\":\"sjde_53422523_3001_1\",\"CertaintyId\":\"Base\",\"LocationId\":\"USM7\",\"ConversionFactor01\":0.451975,\"ValidValue01\":1,\"Quantity\":296,\"ConversionFactor02\":34.9014,\"ValidValue02\":\"1\",\"SalesUnit\":\"EA\",\"ProductId\":\"351.709S\",\"CustomerId\":\"sjde_CA_99999\",\"DemandStreamId\":\"Revenue\",\"OrderStatus\":999,\"OrderType\":\"SO\",\"ForecastItemId\":null,\"OrderSubType\":\"S\",\"Currency\":\"USD\",\"OrderReason\":null,\"DueDate\":\"2016-01-04T05:00:01.000Z\",\"FromDueDate\":\"2016-01-04T05:00:00.000Z\"},\"key\":{\"IdentityKey\":\"351.709S.sjde_CA_99999\",\"EndWrittenTime\":4102376400000,\"WrittenTime\":18000000,\"Name\":\"TemporalKey\",\"EndValidTime\":4102376400000,\"Username\":\"dpark\",\"StartValidTime\":18000000}}]},\"id\":\"1387293484815599\",\"jsonrpc\":\"2.0\"}";
		JsonLite jl = new JsonLite(str);
		System.out.println(jl.toString(4, false, false));
	}
}