package com.netcrest.pado.rpc.client.biz;

import java.util.List;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.rpc.client.IRpcContext;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;
import com.netcrest.pado.rpc.util.RpcUtil;

/**
 * TemporalBiz provides methods to access temporal data.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalBiz
{
	private IRpcContext rpcContext;
	private String gridPath;

	/**
	 * Constructs a new instance of TemporalBiz.
	 * 
	 * @param rpcContext
	 *            IRpcContext object
	 * @param gridPath
	 *            Grid path (not full path)
	 */
	public TemporalBiz(IRpcContext rpcContext, String gridPath)
	{
		this.rpcContext = rpcContext;
		this.gridPath = gridPath;
	}

	/**
	 * Returns the grid path.
	 * 
	 * @return
	 */
	public String getGridPath()
	{
		return this.gridPath;
	}

	/**
	 * Returns the latest value as of now mapped by the specified identity key.
	 * It returns null if the value is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * 
	 */
	JsonLite get(Object identityKey)
	{
		return get(identityKey, -1, -1);
	}

	/**
	 * Returns the value that satisfy the specified valid-at time. It returns
	 * null if the value is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid. If -1, then current
	 *            time.
	 * 
	 */
	public JsonLite get(Object identityKey, long validAtTime)
	{
		return get(identityKey, validAtTime, -1);
	}

	/**
	 * Returns the value that satisfy the specified valid-at and as-of times. It
	 * returns null if the value is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid. If -1, then current
	 *            time.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            current time.
	 */
	public JsonLite get(Object identityKey, long validAtTime, long asOfTime)
	{
		if (identityKey == null) {
			return null;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("identityKey", identityKey);
		params.put("validAtTime", validAtTime);
		params.put("asOfTime", asOfTime);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "get", params, 0);
		return (JsonLite) RpcUtil.getResult(retJl);
	}

	/**
	 * Returns the latest temporal entry as of now mapped by the specified
	 * identity key. It returns null if the value is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * 
	 */
	public JsonLite getEntry(Object identityKey)
	{
		return getEntry(identityKey, -1, -1);
	}

	/**
	 * Returns the temporal entry that satisfies the specified valid-at. It
	 * returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid. If -1, then current
	 *            time.
	 * 
	 * 
	 */
	public JsonLite getEntry(Object identityKey, long validAtTime)
	{
		return getEntry(identityKey, validAtTime, -1);
	}

	/**
	 * Returns the temporal entry that satisfies the specified valid-at and
	 * as-of times. It returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid. If -1, then current
	 *            time.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            current time.
	 * 
	 */
	public JsonLite getEntry(Object identityKey, long validAtTime, long asOfTime)
	{
		if (identityKey == null) {
			return null;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("identityKey", identityKey);
		params.put("validAtTime", validAtTime);
		params.put("asOfTime", asOfTime);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "getEntry", params, 0);
		return (JsonLite) RpcUtil.getResult(retJl);
	}

	/**
	 * Returns all temporal entries of the specified identity key as of now. It
	 * returns a chronologically ordered set providing a history of changes that
	 * fall in the valid-at time. The entries are ordered by start-valid and
	 * written times.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * 
	 */
	public List<JsonLite> getAllEntries(Object identityKey)
	{
		return getAllEntries(identityKey, -1, -1);
	}

	/**
	 * Returns all temporal entries of the specified identity key that satisfy
	 * the specified valid-at time. It returns a chronologically ordered set
	 * providing a history of changes that fall in the valid-at time. The
	 * entries are ordered by start-valid and written times.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid. If -1, then current
	 *            time.
	 * 
	 */
	public List<JsonLite> getAllEntries(Object identityKey, long validAtTime)
	{
		return getAllEntries(identityKey, validAtTime, -1);
	}

	/**
	 * Returns all temporal entries of the specified identity key that satisfy
	 * the specified valid-at and as-of times. It returns a chronologically
	 * ordered set providing a history of changes that fall in the valid-at and
	 * as-of times. The entries are ordered by start-valid and written times.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid. If -1, then current
	 *            time.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            current time.
	 * 
	 */
	public List<JsonLite> getAllEntries(Object identityKey, long validAtTime, long asOfTime)
	{
		if (identityKey == null) {
			return null;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("identityKey", identityKey);
		params.put("validAtTime", validAtTime);
		params.put("asOfTime", asOfTime);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "getAllEntries", params, 0);
		return (List<JsonLite>) RpcUtil.getResult(retJl);
	}

	/**
	 * Returns the temporal entries that satisfy the specified valid-at and end
	 * written time range for the given PQL query string. Note that this method
	 * may retrieve one or more valid objects per temporal list. It searches
	 * temporal values that fall in the specified written time range.
	 * 
	 * @param queryStatement
	 *            PQL and OQL query string. If OQL, i.e., begins with "select"
	 *            then the select projection must be identity key. For example,
	 *            "select distinct e.key.IdentityKey from
	 *            /mygrid/portfolio.entrySet e where
	 *            e.value.value['PortfolioId']='port_a'"
	 * @param validAtTime
	 *            The time at which the value is valid. -1 for current time.
	 * @param fromWrittenTime
	 *            start of the written time range. -1 for current time
	 * @param toWrittenTime
	 *            end of the written time range. -1 for current time.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server.
	 */
	List<JsonLite> getEntryHistoryWrittenTimeRangeList(String queryStatement, long validAtTime, long fromWrittenTime,
			long toWrittenTime)
	{
		JsonLite params = new JsonLite();
		params.put("queryStatement", validAtTime);
		params.put("validAtTime", validAtTime);
		params.put("fromWrittenTime", fromWrittenTime);
		params.put("toWrittenTime", toWrittenTime);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "getEntryHistoryWrittenTimeRangeList", params,
				0);
		return (List<JsonLite>) RpcUtil.getResult(retJl);
	}

	/**
	 * Returns the total count of now-relative entries.
	 */
	public int size()
	{
		return size(-1, -1);
	}

	/**
	 * Returns the total count of entries that satisfy the specified valid-at
	 * and as-of times.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 */
	public int size(long validAtTime, long asOfTime)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("validAtTime", validAtTime);
		params.put("asOfTime", asOfTime);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "size", params, 0);
		return (Integer) RpcUtil.getResult(retJl);
	}

	/**
	 * Returns the total count of temporal lists. The returned number represents
	 * the total number of unique identity keys.
	 */
	public int getTemporalListCount()
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "getTemporalListCount", params, 0);
		return (Integer) RpcUtil.getResult(retJl);
	}

	/**
	 * Returns the temporal list of the specified identity key. A temporal list
	 * reflects a history of a given identity key.
	 * 
	 * @param identityKey
	 *            Identity key
	 * @return null if the specified identity key is not found.
	 */
	public List<JsonLite> getTemporalList(String identityKey)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("identityKey", identityKey);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "getTemporalList", params, 0);
		return (List<JsonLite>) RpcUtil.getResult(retJl);
	}

	/**
	 * Dumps the temporal lists of the the specified identity keys in the
	 * specified directory.
	 * 
	 * @param dumpDir
	 *            Directory in which the temporal lists are dumped. If null,
	 *            then the server specified default directory is used. If
	 *            directory does not exist, it is created.
	 * @param identityKeys
	 *            Identity keys
	 * @return null if the identity keys are not specified or JSON object
	 *         containing "dumpDir" and "fileMap". "fileMap" contains
	 *         ("identityKey", "fileName") entries.
	 */
	public JsonLite dumpTemporalLists(String dumpDir, String... identityKeys)
	{
		if (identityKeys == null) {
			return null;
		}
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("dumpDir", dumpDir);
		params.put("identityKeys", identityKeys);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "dumpTemporalLists", params, 0);
		return (JsonLite) RpcUtil.getResult(retJl);
	}

	/**
	 * Dumps all of the temporal lists in the data node.
	 * <p>
	 * <b>IMPORTANT</b>: This method may take a significant amount of time to
	 * complete if there are many identity keys. It is recommended to inovoke
	 * dump_temporal_lists() instead for faster execution and put less load on
	 * the grid.
	 * 
	 * @param dumpDir
	 *            Directory in which the temporal lists are dumped. If null,
	 *            then the server specified default directory is used. If
	 *            directory does not exist, it is created.
	 * @return null if the identity keys are not specified or JSON object
	 *         containing "dumpDir" and "fileMap". "fileMap" contains
	 *         ("identityKey", "fileName") entries.
	 */
	public JsonLite dumpAllTemporalLists(String dumpDir)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("dumpDir", dumpDir);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "dumpAllTemporalLists", params, 0);
		return (JsonLite) RpcUtil.getResult(retJl);
	}
	
	/**
	 * Puts the specified (key, value) pair in the grid path.
	 * @param key Key
	 * @param value Value
	 */
	public JsonLite put(Object identityKey, JsonLite value, long startValidTime, long endValidTime, long writtenTime)
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		params.put("identityKey", identityKey);
		params.put("value", value);
		params.put("startValidTime", startValidTime);
		params.put("endValidTime", endValidTime);
		params.put("writtenTime", writtenTime);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "put", params, 0);
		return (JsonLite) RpcUtil.getResult(retJl);
	}
}
