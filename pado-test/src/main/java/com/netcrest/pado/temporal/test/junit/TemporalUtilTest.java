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
package com.netcrest.pado.temporal.test.junit;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.netcrest.pado.temporal.TemporalUtil;

public class TemporalUtilTest
{

	@Test
	public void testTemporalTime()
	{
		SimpleDateFormat dayFormat = new SimpleDateFormat("MM/dd/yyyy");
		dayFormat.setTimeZone(TemporalUtil.TIME_ZONE);
		SimpleDateFormat msecFormat = new SimpleDateFormat("MM/dd/yyyy HHmmss.SSS");
		dayFormat.setTimeZone(TemporalUtil.TIME_ZONE);
		Date today = new Date();
		today = TemporalUtil.round(today, TemporalUtil.Resolution.DAY);
		System.out.println(dayFormat.format(today));
		System.out.println(msecFormat.format(today));
	}

}
