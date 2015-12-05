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
package com.netcrest.pado.tools.pado.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.gemstone.gemfire.cache.query.Struct;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.IIndexMatrixBiz;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.exception.IncompatibleTypeException;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.link.IIndexMatrixBizLink.QueryType;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.tools.pado.ICommand;
import com.netcrest.pado.tools.pado.PadoShell;
import com.netcrest.pado.tools.pado.SharedCache;

public class cp implements ICommand
{
	private PadoShell padoShell;
	private static Options options = new Options();
	static {
		options.addOption("?", false, "");
//		options.addOption("r", false, "");
//		options.addOption("R", false, "");
	}

	@Override
	public void initialize(PadoShell padoShell)
	{
		this.padoShell = padoShell;
	}

	@Override
	public void help()
	{
		PadoShell.println("cp <source-path> <target-path> | [-?]");
		PadoShell.println("   Copy the contents of <source-path> to <target-path>.");
		
//		PadoShell.println("cp [-R] [-r] <source-path> <target-path> | [-?]");
//		PadoShell.println("   Copy the contents of <source-path> to <target-path>.");
//		PadoShell.println("      -R  Recursively copy <source-path> and its sub-paths to <target-path>.");
//		PadoShell.println("          If <source-path> ends in a /, the contents of the directory is");
//		PadoShell.println("          copied, rather than the directory itself.");
//		PadoShell.println("      -r  Same as -R.");
	}

	@Override 
	public String getShortDescription()
	{
		return "Copy source path by creating the target path if it does not exist and overwriting its contents.";
	}
	
	@Override
	public boolean isLoginRequired()
	{
		return true;
	}

	@Override
	public Options getOptions()
	{
		return options;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void run(CommandLine commandLine, String command) throws Exception
	{
		boolean isRecursive = commandLine.hasOption("-r") || commandLine.hasOption("-R");

		List<String> argList = (List<String>) commandLine.getArgList();
		if (argList.size() < 3) {
			PadoShell.printlnError(this, "Invalid number of arguments.");
			return;
		}
		String sourcePath = argList.get(1);
		String targetPath = argList.get(2);

		String sourceGridId = padoShell.getGridId(sourcePath);
		String targetGridId = padoShell.getGridId(targetPath);
		String sourceGridPath = padoShell.getGridPath(sourcePath);
		String targetGridPath = padoShell.getGridPath(targetPath);
		String sourceFullPath = padoShell.getFullPath(sourcePath);
		String targetFullPath = padoShell.getFullPath(targetPath);

		// If source and target are in the same grid then do copy in the grid,
		// otherwise, do it from here.
		if (sourceGridId.equals(targetGridId)) {

			// Do copy in the grid.
			IPathBiz pathBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IPathBiz.class);
			pathBiz.copy(sourceGridId, sourceGridPath, targetGridPath);

		} else {

			IPathBiz pathBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IPathBiz.class);
			if (pathBiz.exists(sourceGridId, sourceGridPath) == false) {
				PadoShell.printlnError(this, sourcePath + ": Source path does not exist.");
				return;
			}
			IGridMapBiz gridMapBiz = SharedCache.getSharedCache().getPado().getCatalog().newInstance(IGridMapBiz.class);
			gridMapBiz.setGridPath(sourceGridPath);
			gridMapBiz.getBizContext().getGridContextClient().setGridIds(sourceGridId);
			Map.Entry sourceEntry = gridMapBiz.getRandomEntry();
			if (sourceEntry == null) {
				return;
			}

			// Get target entry if the target grid path exists
			if (pathBiz.exists(targetGridId, targetGridPath) == false) {
				// See if we can create the path
				IPathBiz.PathType pathType = pathBiz.getPathType(sourceGridId, sourceGridPath);
				if (pathType == IPathBiz.PathType.NOT_SUPPORTED) {
					PadoShell.printlnError(this, "Unable to copy. Path type not supported.");
					return;
				}
				boolean created = pathBiz.createPath(targetGridId, targetGridPath, pathType, false);
				if (created == false) {
					PadoShell.printlnError(this, targetGridPath + ": Target path creation failed.");
					return;
				}
				// refresh required to get the newly created path info
				SharedCache.getSharedCache().refresh();
			} else {
			
				// Get a target entry to determine the key and value types.
				gridMapBiz.setGridPath(targetGridPath);
				gridMapBiz.getBizContext().getGridContextClient().setGridIds(targetGridId);
				Map.Entry targetEntry = gridMapBiz.getRandomEntry();
				if (targetEntry != null) {
					checkIncompatibleTypes(sourceEntry.getKey(), sourceEntry.getValue(), targetEntry.getKey(),
							targetEntry.getValue());
				}
			}

			gridMapBiz.setGridPath(targetGridPath);
			gridMapBiz.getBizContext().getGridContextClient().setGridIds(targetGridId);
			IIndexMatrixBiz imBiz = SharedCache.getSharedCache().getPado().getCatalog()
					.newInstance(IIndexMatrixBiz.class, true);
			imBiz.setQueryType(QueryType.OQL);
			imBiz.setGridIds(sourceGridId);
			// imBiz.setFetchSize(10000);
			String queryString = String.format(less.QUERY_KEYS_VALUES, sourceFullPath);
			IScrollableResultSet rs = imBiz.execute(queryString);
			HashMap map = new HashMap(imBiz.getFetchSize());
			do {
				List<Struct> list = rs.toList();
				for (Struct struct : list) {
					map.put(struct.getFieldValues()[0], struct.getFieldValues()[1]);
				}
				gridMapBiz.putAll(map);
				map.clear();
			} while (rs.nextSet());
			SharedCache.getSharedCache().refresh();
		}
	}

	@SuppressWarnings("rawtypes")
	private void checkIncompatibleTypes(Object sourceKey, Object sourceValue, Object targetKey, Object targetValue)
			throws IncompatibleTypeException
	{
		// If the target region is not empty then make sure the source
		// and target paths have the same entry types.
		if (targetKey != null) {

			// Temporal identity keys must have the same type.
			if (sourceKey instanceof ITemporalKey && targetKey instanceof ITemporalKey == false
					|| targetKey instanceof ITemporalKey == false && targetKey instanceof ITemporalKey) {
				throw new IncompatibleTypeException("Incompatible temporal key types");
			}
			if (sourceKey instanceof ITemporalKey && targetKey instanceof ITemporalKey) {
				sourceKey = ((ITemporalKey) sourceKey).getIdentityKey();
				targetKey = ((ITemporalKey) targetKey).getIdentityKey();
			}

			// Keys (or identity keys) must have the same type.
			if (targetKey.getClass() != sourceKey.getClass()) {
				throw new IncompatibleTypeException("Incompatible key types");
			}

			// Temporal values must have the same type.
			if (sourceValue instanceof ITemporalData && targetValue instanceof ITemporalData == false
					|| sourceValue instanceof ITemporalData == false && targetValue instanceof ITemporalData) {
				throw new IncompatibleTypeException("Incompatible temporal data types");
			}
			if (targetValue instanceof ITemporalData && sourceValue instanceof ITemporalData) {
				targetValue = ((ITemporalData) targetValue).getValue();
				sourceValue = ((ITemporalData) sourceValue).getValue();
			}

			// Values (temporal values) must have the same type.
			if (sourceValue instanceof KeyMap && targetValue instanceof KeyMap == false
					|| sourceValue instanceof KeyMap == false && targetValue instanceof KeyMap) {
				throw new IncompatibleTypeException("Incompatible temporal data (KeyMap) types");
			}
			if (sourceValue instanceof KeyMap && targetValue instanceof KeyMap) {
				KeyType sourceKeyType = ((KeyMap) sourceValue).getKeyType();
				KeyType targetKeyType = ((KeyMap) targetValue).getKeyType();
				if (sourceKeyType.getId().equals(targetKeyType.getId()) == false) {
					throw new IncompatibleTypeException("Incompatible KeyMap KeyTypes");
				}
			} else {
				// Values (or temporal values) must have the same type.
				if (sourceValue.getClass() != targetValue.getClass()) {
					throw new IncompatibleTypeException("Incompatible temporal data types");
				}
			}
		}
	}
}
