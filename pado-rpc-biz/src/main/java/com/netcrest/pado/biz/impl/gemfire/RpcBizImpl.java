package com.netcrest.pado.biz.impl.gemfire;

import javax.annotation.Resource;

import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.mqtt.MqttJsonRpcOsClient;

@SuppressWarnings({ "rawtypes" })
public class RpcBizImpl
{
	@Resource
	IBizContextServer bizContext;

	@BizMethod
	public JsonLite broadcast(JsonLite request)
	{
		if (request == null) {
			return null;
		}

		return MqttJsonRpcOsClient.getRpcClient().execute(bizContext.getUserContext().getToken(), request);
	}
	
	@BizMethod
	public JsonLite executeOnServer(JsonLite request)
	{
		if (request == null) {
			return null;
		}

		return MqttJsonRpcOsClient.getRpcClient().execute(bizContext.getUserContext().getToken(), request);
	}
	
	@BizMethod
	public JsonLite executeOnPath(JsonLite request)
	{
		if (request == null) {
			return null;
		}

		return MqttJsonRpcOsClient.getRpcClient().execute(bizContext.getUserContext().getToken(), request);
	}
}