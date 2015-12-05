/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Netcrest
 * Technologies, LLC. Your use of this software is strictly bounded to the terms
 * of the license agreement you established with Netcrest Technologies, LLC.
 * Redistribution and use in source and binary forms, with or without
 * modification, are strictly enforced by such a written license agreement,
 * which you must obtain from Netcrest Technologies, LLC prior to your action.
 */
package com.gemstone.gemfire.addon.dq.internal.port.v8;

import com.gemstone.gemfire.addon.dq.internal.DQueueInternalPort;
import com.gemstone.gemfire.internal.cache.LocalRegion;
import com.gemstone.gemfire.internal.cache.RegionEntry;

public class DQueueInternalPort_v8 extends DQueueInternalPort
{
	public DQueueInternalPort_v8()
	{
		super();
	}
	
	@Override
	public Object getValueInVM(RegionEntry entry, LocalRegion lr)
	{
		return entry.getValueInVM(lr);
	}
}
