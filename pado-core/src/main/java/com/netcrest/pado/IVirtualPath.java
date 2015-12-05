/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado;

import java.util.List;

/**
 * IVirtualPath is the virtual path executor that can be obtained from
 * IVirtualPathBiz or the corresponding implementation class.
 * 
 * @author dpark
 * 
 * @param <T>
 *            The result list element type. This is typically KeyMap or JsonLite
 *            but can be any type supported by the underlying virtual path
 *            engine.
 */
public interface IVirtualPath<T>
{
	/*
	 * Executes the specified input for the now-relative temporal data. This
	 * call is analogous to execute(input, -1, -1).
	 * 
	 * @param input Input values to the virtual path definition, i.e., ${Input}.
	 */
	public List<T> execute(Object input);

	/*
	 * Executes the specified input for the now-relative as-of temporal data.
	 * This call is analogous to execute(input, validAtTime, -1).
	 * 
	 * @param input Input values to the virtual path definition, i.e., ${Input}.
	 * 
	 * @param validAtTime Valid-at time. If -1, then current time.
	 */
	public List<T> execute(Object input, long validAtTime);

	/**
	 * Executes the specified input for the specified temporal time constraints.
	 * 
	 * @param input
	 *            Input values to the virtual path definition, i.e., ${Input}.
	 * @param validAtTime
	 *            Valid-at time. If -1, then current time.
	 * @param asOfTime
	 *            As-of time. If -1, then current time.
	 * @return Virtual path anchored data set
	 */
	public List<T> execute(Object input, long validAtTime, long asOfTime);

	/**
	 * Closes this instance of VirtualPath. It is recommended that this method
	 * should always be invoked when this VirtualPath instance is no longer
	 * needed in order to free system resources. Note that IVirtualPath is still
	 * usable even after it is closed. In that case, the thread pool no longer
	 * exists and all subsequent executions will be done in the current thread.
	 */
	public void close();
}
