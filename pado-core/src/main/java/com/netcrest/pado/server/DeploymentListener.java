package com.netcrest.pado.server;

import com.netcrest.pado.internal.util.HotDeploymentBizClasses;

/**
 * Hot deployment notification listener. This callback is invoked when new jars
 * are hot-deployed to the grid.
 * 
 * @author dpark
 *
 */
public interface DeploymentListener {
	/**
	 * Invoked when a hot-deployment of jar files occurs.
	 */
	void jarDeployed(HotDeploymentBizClasses deployment);
}
