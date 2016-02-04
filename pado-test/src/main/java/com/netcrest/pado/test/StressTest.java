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
package com.netcrest.pado.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.test.biz.IStressTestBiz;

@SuppressWarnings({ "rawtypes" })
public class StressTest
{
	private IPado pado;

	public StressTest()
	{
		Pado.connect();
		pado = Pado.login();
	}
	
	@SuppressWarnings("unchecked")
	public void start() throws FileNotFoundException, IOException
	{
		JsonLite config = new JsonLite(new File("etc/client/StressTest.json"));
		writeLine(config.toString(4, false, false));
		IStressTestBiz stressTestBiz = pado.getCatalog().newInstance(IStressTestBiz.class);
		Object[] paths = (Object[])config.get("Paths");
		for (Object obj : paths) {
			JsonLite jl = (JsonLite)obj;
			String pathType = (String)jl.get("PathType");
			if (pathType != null) {
				IPathBiz.PathType pt = IPathBiz.PathType.valueOf(pathType.toUpperCase());
				jl.put("PathType", pt);
			}
			stressTestBiz.addPath(jl);
		}
		int threadCountPerServer = 5;
		int loopCount = 1;
		boolean isIncludeObjectCreationTime = false;
		if (config.get("ThreadCountPerServer") != null) {
			threadCountPerServer = (Integer)config.get("ThreadCountPerServer");
		}
		if (config.get("LoopCount") != null) {
			loopCount = (Integer)config.get("LoopCount");
		}
		if (config.get("IsIncludeObjectCreationTime") != null) {
			isIncludeObjectCreationTime = (Boolean)config.get("IsIncludeObjectCreationTime");
		}
		stressTestBiz.setThreadCountPerServer(threadCountPerServer);
		stressTestBiz.setLoopCount(loopCount);
		stressTestBiz.setIncludeObjectCreationTime(isIncludeObjectCreationTime);
		
		List<String> statusList = stressTestBiz.start();
		for (String status : statusList) {
			writeLine(status);
		}
	}

	public void close()
	{
		Pado.close();
	}

	private static void writeLine()
	{
		System.out.println();
	}

	private static void writeLine(String line)
	{
		System.out.println(line);
	}

	private static void write(String str)
	{
		System.out.print(str);
	}

	private static void usage()
	{
		writeLine();
		writeLine("Usage:");
		writeLine("   StressTest [-?]");
		writeLine();
		writeLine("   Default: StressTest");
		writeLine("   StressTest writes mock data into the grid by all servers.");
		writeLine();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception
	{
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			}
		}
		
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		StressTest client = new StressTest();
		client.start();
		client.close();
	}
}
