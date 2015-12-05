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
package com.netcrest.pado.gemfire.port;

import com.gemstone.gemfire.internal.GemFireVersion;

/**
 * GemfireVersionDetail breaks down the GemFire version number to individual
 * version components, i.e., major, minor, update, and patch.
 * 
 * @author dpark
 * 
 */
public class GemfireVersionDetail
{
	String version;
	int major = 0;
	int minor = 0;
	int update = 0;
	int patch = 0;

	public GemfireVersionDetail()
	{
		String gemfireVersion = GemFireVersion.getGemFireVersion();
		String split[] = gemfireVersion.split("\\.");

		for (int i = 0; i < split.length; i++) {
			switch (i) {
			case 0:
				major = Integer.parseInt(split[i]);
				break;
			case 1:
				try {
					minor = Integer.parseInt(split[i]);
				} catch (NumberFormatException ex) {
					minor = Integer.parseInt(split[i].substring(0, 1));
				}
				break;
			case 2:
				try {
					update = Integer.parseInt(split[i]);
				} catch (NumberFormatException ex) {
					// non-number. ignore.
				}
				break;
			case 3:
				try {
					patch = Integer.parseInt(split[i]);
				} catch (NumberFormatException ex) {
					// non-number. ignore.
				}
				break;
			}
		}
	}

	public String getVersion()
	{
		return version;
	}

	public int getMajor()
	{
		return major;
	}

	public int getMinor()
	{
		return minor;
	}

	public int getUpdate()
	{
		return update;
	}

	public int getPatch()
	{
		return patch;
	}
}
