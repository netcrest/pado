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
package com.netcrest.pado.index.helper;

import java.io.Serializable;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;

/**
 * Facilitates Function execution and allows local unit test by specifing
 * <code> realm = Realm.LOCAL </code>
 * 
 */
public class FunctionExecutor {

	public static enum Realm {
		REGION, SERVER, ALL_SERVERS, MEMBER, MEMBER_RANDOM, ALL_MEMBERS, LOCAL, UNDEFINED
	}

	public static ResultCollector execute(Realm realm, Region region, Pool pool, 
			RegionService regionService, Set filters,
			Serializable argument, ResultCollector resultCollector,
			String functionId, Class functionClass) {
		switch (realm) {
		case REGION:
			return executeRegion(region, filters, argument, resultCollector,
					functionId, functionClass);
		case SERVER:
			return executeServer(pool, regionService, filters, argument, resultCollector,
					functionId, functionClass);
		case MEMBER:
			return executeMember(filters, argument, resultCollector,
					functionId, functionClass);	
		case MEMBER_RANDOM:
			return executeMemberRandom(filters, argument, resultCollector,
					functionId, functionClass);
		case ALL_SERVERS:
			return executeServers(pool, regionService, filters, argument, resultCollector,
					functionId, functionClass);
		case ALL_MEMBERS:
			return executeMembers(filters, argument, resultCollector,
					functionId, functionClass);
		case LOCAL:
			return executeLocal(filters, argument, resultCollector, functionId,
					functionClass);

		}
		return null;
	}

	private static ResultCollector executeLocal(Set filters, Serializable argument,
			ResultCollector resultCollector, String functionId,
			Class functionClass) {
		Function function = null;;
		try {
			function = (Function) functionClass.newInstance();
			Properties props = new Properties();
			((Declarable) function).init(props);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		LocalFunctionExecution localFunction = new LocalFunctionExecution();
		return localFunction.withArgs(argument).withCollector(resultCollector).withFilter(filters).execute(function);
	}

	private static ResultCollector executeServer(Pool pool, RegionService regionService, Set filters, Serializable argument,
			ResultCollector resultCollector, String functionId,
			Class functionClass) {
		Execution exec;
		if (regionService != null) {
			exec = FunctionService.onServer(regionService).withCollector(
					resultCollector);
		} else if (pool != null) {
			exec = FunctionService.onServer(pool).withCollector(
					resultCollector);
		} else {
			return null;
		}
		if (argument != null) {
			exec = exec.withArgs(argument);
		}
		return exec.execute(functionId);
	}
	
	private static ResultCollector executeServers(Pool pool, RegionService regionService, Set filters, Serializable argument,
			ResultCollector resultCollector, String functionId,
			Class functionClass) {
		Execution exec;
		if (regionService != null) {
			exec = FunctionService.onServers(regionService).withCollector(
					resultCollector);
		} else if (pool != null) {
			exec = FunctionService.onServers(pool).withCollector(
					resultCollector);
		} else {
			return null;
		}
		if (argument != null) {
			exec = exec.withArgs(argument);
		}
		return exec.execute(functionId);
	}
	
	private static ResultCollector executeMembers(Set filters, Serializable argument,
			ResultCollector resultCollector, String functionId,
			Class functionClass) {
		Execution exec = FunctionService.onMembers(CacheFactory.getAnyInstance().getDistributedSystem()).withCollector(
					resultCollector);
		
		if (argument != null) {
			exec = exec.withArgs(argument);
		}
		return exec.execute(functionId);
	}
	
	/**
	 * Executes function in itself
	 * @param filters
	 * @param argument
	 * @param resultCollector
	 * @param functionId
	 * @param functionClass
	 * @return
	 */
	private static ResultCollector executeMember(Set filters, Serializable argument,
			ResultCollector resultCollector, String functionId,
			Class functionClass) {
		Execution exec = FunctionService.onMember(CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember()).withCollector(
					resultCollector);
		
		if (argument != null) {
			exec = exec.withArgs(argument);
		}
		return exec.execute(functionId);
	}
	
	private static ResultCollector executeMemberRandom(Set filters, Serializable argument,
			ResultCollector resultCollector, String functionId,
			Class functionClass) {
		Set<DistributedMember> set = CacheFactory.getAnyInstance().getDistributedSystem().getAllOtherMembers();
		int index = new Random().nextInt(set.size() + 1);
		DistributedMember member;
		if (index == set.size()) {
			member = CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember();
		} else {
			member = set.toArray(new DistributedMember[set.size()])[index];
		}
		Execution exec = FunctionService.onMember(member).withCollector(resultCollector);
		
		if (argument != null) {
			exec = exec.withArgs(argument);
		}
		return exec.execute(functionId);
	}

	private static ResultCollector executeRegion(Region region, Set filters,
			Serializable argument, ResultCollector resultCollector,
			String functionId, Class functionClass) {
		Execution exec = FunctionService.onRegion(region).withCollector(
				resultCollector);
		if (argument != null) {
			exec = exec.withArgs(argument);
		}
		if (filters != null && filters.size() > 0) {
			exec = exec.withFilter(filters);
		}
		return exec.execute(functionId);
	}
}
