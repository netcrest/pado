package com.netcrest.pado.test.rpc.client;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.rpc.client.biz.PathRpcBiz;
import com.netcrest.pado.rpc.mqtt.ReplyKey;
import com.netcrest.pado.rpc.mqtt.client.IRpcListener;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MlDemo
{
	public JsonLite execute(JsonLite params)
	{
		String gridPath = params.getString("gridPath", "test1");
		PathRpcBiz pathBiz = new PathRpcBiz(gridPath);
		String queryPredicate = (String) params.get("queryPredicate");
		JsonLite queryResult = pathBiz.query(queryPredicate);
		Object[] list = (Object[]) queryResult.get("result");

		// Let's also dump the grid path
		JsonLite dumpResult = pathBiz.dump();
		String filePath = (String) dumpResult.get(ReplyKey.result.name());
		
		// Get the path size
		JsonLite sizeResult = pathBiz.size();
		int size = sizeResult.getInt(ReplyKey.result.name(), 0);
		
		JsonLite result = new JsonLite();
		result.put("dataList", list);
		result.put("filePath", filePath);
		result.put("size", size);
		return result;
	}

	public void testRpcListener(JsonLite params)
	{
		String gridPath = params.getString("gridPath", "test1");
		final PathRpcBiz pathBiz = new PathRpcBiz(gridPath);
		params.getString("name", pathBiz.getGridPath());
		final IRpcListener listener = new RpcListenerImpl();
		pathBiz.addListener(pathBiz.getGridPath(), listener);
		System.out.println("MlDemo.testRpcListener() listener added: name=" + pathBiz.getGridPath());

		int count = 0;
		while (count < 20) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
			count++;
		}
		Logger.info("MlDemo.testRpcListener() finished.");
		pathBiz.removeListener(pathBiz.getGridPath(), listener);
		Logger.info("MlDemo.testRpcListener() listener removed: " + pathBiz.getGridPath());

	}

	public class RpcListenerImpl implements IRpcListener
	{

		@Override
		public void messageReceived(Object message)
		{
			Logger.info(message.toString());
		}

	}
}
