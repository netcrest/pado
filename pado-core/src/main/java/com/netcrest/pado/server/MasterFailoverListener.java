package com.netcrest.pado.server;

/**
 * Master failover notification listener. This callback is invoked when a
 * failover occurs due to a server joining or leaving the grid.
 * 
 * @author dpark
 *
 */
public interface MasterFailoverListener
{
	/**
	 * Invoked when a failover occurs due to a server joining or leaving the
	 * grid.
	 * 
	 * @param isMasterBefore
	 *            The state of the server before the failover.
	 * @param isMasterAfter
	 *            The state of the server after the failover.
	 */
	void failoverOccurred(boolean isMasterBefore, boolean isMasterAfter);
}
