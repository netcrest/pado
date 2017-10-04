package com.netcrest.pado.rpc.client.biz;

import java.util.List;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.rpc.mqtt.ReplyKey;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;

/**
 * TemporalBiz provides methods to access temporal data.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalBiz
{
	private String gridPath;

	/**
	 * Constructs a new instance of TemporalBiz.
	 * 
	 * @param gridPath
	 *            Grid path (not full path)
	 */
	public TemporalBiz(String gridPath)
	{
		this.gridPath = gridPath;
	}

	/**
	 * Returns the grid path.
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
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(this, "get", params, 0);
		if (retJl == null) {
			return null;
		}
		return (JsonLite)retJl.get(ReplyKey.result.name());
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
	 *            The as-of time compared against the written times. If -1, then current
	 *            time.
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
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(this, "getEntry", params, 0);
		if (retJl == null) {
			return null;
		}
		return (JsonLite)retJl.get(ReplyKey.result.name());
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
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(this, "getAllEntries", params, 0);
		if (retJl == null) {
			return null;
		}
		return retJl.getList(ReplyKey.result.name());
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
		JsonLite retJl =  MqttJsonRpcClient.getRpcClient().execute(this, "getEntryHistoryWrittenTimeRangeList", params, 0);
		if (retJl == null) {
			return null;
		}
		return retJl.getList(ReplyKey.result.name());
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
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(this, "size", params, 0);
		if (retJl == null) {
			return 0;
		}
		return retJl.getInt(ReplyKey.result.name(), 0);
	}

	/**
	 * Returns the total count of temporal lists. The returned number represents
	 * the total number of unique identity keys.
	 */
	public int getTemporalListCount()
	{
		JsonLite params = new JsonLite();
		params.put("gridPath", gridPath);
		JsonLite retJl = MqttJsonRpcClient.getRpcClient().execute(this, "getTemporalListCount", params, 0);
		if (retJl == null) {
			return 0;
		}
		return retJl.getInt(ReplyKey.result.name(), 0);
	}
}
