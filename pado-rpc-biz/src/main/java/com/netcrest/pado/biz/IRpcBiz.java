package com.netcrest.pado.biz;

import java.util.List;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * IRpcBiz provides methods to make RPC calls in the specified grid(s). All
 * requests and replies are in the form of JSON objects and must conform to the
 * JSON-RPC 2.0 spec with extensions described below.
 * <p>
 * <b>Request:</b>
 * <table border="1" cellpadding="5" cellspacing="0" style=
 * "border-collapse:collapse;">
 * <tr>
 * <tr>
 * <th>Key</th>
 * <th>Description</th>
 * <th>JSON-RPC 2.0</th>
 * </tr>
 * <td>"jsonrpc"</td>
 * <td>JSON RPC version. Always "2.0".</td>
 * <td>yes</td>
 * </tr>
 * <td>"id"</td>
 * <td>Unique id</td>
 * <td>yes</td>
 * </tr>
 * <td>"lang"</td>
 * <td>Language. Supported: "java", "python".</td>
 * <td>no</td>
 * </tr>
 * <td>"classname"</td>
 * <td>Fully-qualified class name</td>
 * <td>no</td>
 * </tr>
 * <td>"method"</td>
 * <td>Method name</td>
 * <td>yes</td>
 * </tr>
 * <td>"params"</td>
 * <td>Parameter JSON object that contains method parameters.</td>
 * <td>yes</td>
 * </tr>
 * <td>"timeout"</td>
 * <td>Request timeout in msec. Default: 10000 msec</td>
 * <td>no</td>
 * </tr>
 * <td>"daemon"</td>
 * <td>false to start the method call in a non-daemon thread. By default, a
 * method call is made in a daemon thread causing the target process to exit
 * upon method completion. Default: true</td>
 * <td>false</td>
 * </tr>
 * </table>
 * <p>
 * <b>Reply:</b>
 * <table border="1" cellpadding="5" cellspacing="0" style=
 * "border-collapse:collapse;">
 * <tr>
 * <tr>
 * <th>Key</th>
 * <th>Description</th>
 * <th>JSON-RPC 2.0</th>
 * </tr>
 * <td>"jsonrpc"</td>
 * <td>JSON RPC version. Always "2.0".</td>
 * <td>yes</td>
 * </tr>
 * <td>"id"</td>
 * <td>Unique ID</td>
 * <td>yes</td>
 * </tr>
 * <td>"gid"</td>
 * <td>Grid ID</td>
 * <td>no</td>
 * </tr>
 * <td>"sid"</td>
 * <td>Server ID</td>
 * <td>no</td>
 * </tr>
 * <td>"result"</td>
 * <td>Method call results in JSON object</td>
 * <td>yes</td>
 * </tr>
 * <td>"error"</td>
 * <td>Error in JSON object. If the reply contains this parameter then the
 * "result" parameter may not exist in the reply.</td>
 * <td>yes</td>
 * </tr>
 * </table>
 * <p>
 * <b>Error:</b>
 * <p>
 * An "error" object has the following parameters:
 * <table border="1" cellpadding="5" cellspacing="0" style=
 * "border-collapse:collapse;">
 * <tr>
 * <tr>
 * <th>Key</th>
 * <th>Description</th>
 * <th>JSON-RPC 2.0</th>
 * </tr>
 * <td>"code"</td>
 * <td>An integer value that indicates the error type that occurred.</td>
 * <td>yes</td>
 * </tr>
 * <td>"message"</td>
 * <td>A String providing a short description of the error.</td>
 * <td>yes</td>
 * </tr>
 * <td>"data"</td>
 * <td>A Primitive or Structured value that contains additional information
 * about the error.</td>
 * <td>yes</td>
 * </tr>
 * </table>
 * 
 * @author dpark
 *
 */
@SuppressWarnings("rawtypes")
@BizClass(name = "IRpcBiz")
public interface IRpcBiz extends IBiz
{
	/**
	 * Broadcasts the specified request to all servers in the grid resulting all
	 * servers to invoke the RPC method specified in the request.
	 * 
	 * @param request
	 *            Request JSON object
	 * @return Aggregated list of replies from all servers mapped by grid IDs.
	 */
	@BizMethod
	@OnServer(broadcast = true)
	@WithGridCollector(gridCollectorClass="com.netcrest.pado.biz.collector.JsonLiteGridCollector")
	JsonLite broadcast(JsonLite request);

	/**
	 * Executes the specified request on one of the servers in the grid.
	 * 
	 * @param request
	 *            Request JSON object
	 * @return RPC method call reply mapped by grid IDs.
	 */
	@BizMethod
	@OnServer
	@WithGridCollector(gridCollectorClass="com.netcrest.pado.biz.collector.JsonLiteGridCollector")
	JsonLite executeOnServer(JsonLite request);
	
	/**
	 * Executes the specified request on path.
	 * 
	 * @param request
	 *            Request JSON object
	 * @return Aggregated list of replies from one or more servers mapped by grid IDs.
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass="com.netcrest.pado.biz.collector.JsonLiteGridCollector")
	JsonLite executeOnPath(JsonLite request);
}
