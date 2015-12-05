package com.gemstone.gemfire.addon.dq.internal;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.internal.GemFireVersion;
import com.gemstone.gemfire.internal.cache.LocalRegion;
import com.gemstone.gemfire.internal.cache.RegionEntry;

/**
 * Pado addition.
 * @author dpark
 *
 */
public class DQueueInternalPort
{
	private static DQueueInternalPort port;
	
	static {
		String version = GemFireVersion.getGemFireVersion();
		int major = GemFireVersion.getMajorVersion(version);
		if (major <= 7) {
			major = 7;
		} else {
			major = 8;
		}
		String className = "com.gemstone.gemfire.addon.dq.internal.port.v" + major + ".DQueueInternalPort_v" + major;
		try {
			Class<?> clazz = Class.forName(className);
			port = (DQueueInternalPort) clazz.newInstance();
		} catch (Exception ex1) {
			try {
				Cache cache = CacheFactory.getAnyInstance();
				cache.getLogger().severe(ex1);
			} catch (Exception ex2) {
				ex1.printStackTrace();
			}
		}
	}
	
	public static DQueueInternalPort getInternalPort()
	{
		return port;
	}
	
	protected DQueueInternalPort()
	{
	}
	
	public Object getValueInVM(RegionEntry entry, LocalRegion lr)
	{
		return port.getValueInVM(entry, lr);
	}
}
