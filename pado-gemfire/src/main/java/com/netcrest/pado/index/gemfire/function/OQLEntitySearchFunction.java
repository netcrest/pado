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
package com.netcrest.pado.index.gemfire.function;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.result.IMemberResults;
import com.netcrest.pado.index.result.MemberResults;
import com.netcrest.pado.index.result.ValueInfo;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

/**
 * The OQL EntitySearchFunction called by GridSearch server to build indexMatrix
 * for an OQL style query
 * 
 */
public class OQLEntitySearchFunction extends AbstractEntitySearchFunction implements Function, Declarable,
		IEntitySearchFunction
{
	private static final long serialVersionUID = 1L;

	public final static String Id = "OQLEntitySearchFunction";
	
	private final static Pattern limitPattern = Pattern.compile("(?i) limit ");

	@Override
	public String getId()
	{
		return Id;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List queryLocal(GridQuery criteria, FunctionContext context) throws GridQueryException
	{
		try {
			// RegionFunctionContext rfc = (RegionFunctionContext) context;
			// Region region = rfc.getDataSet();

			String queryString = criteria.getQueryString();
			Region region = IndexMatrixOperationUtility.getRegionFromQuery(queryString, null);
			if (region == null) {
				return null;
			}

			// Apply limit if defined based on the number of servers
			int limit = criteria.getLimit();
			if (limit > 0) {
				limit = (int)Math.ceil(criteria.getLimit() / PadoServerManager.getPadoServerManager().getServerCount());
			}
			if (limit >= 0) {
				Matcher matcher = limitPattern.matcher(queryString);
				if (matcher.find() == false) {
					queryString = queryString + " limit " + limit;
				}
			}
			
			QueryService qs = CacheFactory.getAnyInstance().getQueryService();
			DefaultQuery query = (DefaultQuery) qs.newQuery(queryString);
			LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
			SelectResults sr = (SelectResults) localDS.executeQuery(query, null, null);

			// CollectionType type = sr.getCollectionType();
			// ObjectType elementType = type.getElementType();
			// List resultList = sr.asList();
			// if (elementType.isStructType()) {
			// for (Object object : sr) {
			// Struct struct = (Struct)object;
			// StructType st = struct.getStructType();
			// System.out.println(st);
			// }
			// } else {
			// resultList = sr.asList();
			// }
			// return resultList;
			return sr.asList();
		} catch (Exception ex) {
			Logger.warning(ex);
			throw new GridQueryException(ex);
		}

	}

	@SuppressWarnings("rawtypes")
	@Override()
	protected IMemberResults makeResults(GridQuery criteria, List resultsList)
	{
		MemberResults memberResults = new MemberResults();

		// Determine fromIndex and toIndex
		if (resultsList == null || resultsList.size() == 0) {
			memberResults.setTotalSizeOnServer(0);
			memberResults.setCurrentBatchIndexOnServer(Constants.END_OF_LIST);
			memberResults.setNextBatchIndexOnServer(Constants.END_OF_LIST);
			memberResults.setResults(new ArrayList<ValueInfo>());
			return memberResults;
		}
		int fromIndex = criteria.getStartIndex();
		if (fromIndex < 0) {
			fromIndex = 0;
		}
//		int toIndex = fromIndex + (criteria.getEndPageIndex() - criteria.getStartPageIndex() + 1) * criteria.getAggregationPageSize() - 1;
//		if (toIndex > resultsList.size()) {
//			toIndex = resultsList.size() - 1;
//		}
		int toIndex = fromIndex + criteria.getAggregationPageSize() - 1;
		if (toIndex > resultsList.size()) {
			toIndex = resultsList.size() - 1;
		}

		// Set the total size of the results
		memberResults.setTotalSizeOnServer(resultsList.size());
		memberResults.setCurrentBatchIndexOnServer(fromIndex);

		int arraySize = toIndex - fromIndex + 1;
		if (arraySize > 0) {
			try {
				ArrayList<ValueInfo> list = new ArrayList<ValueInfo>(toIndex - fromIndex + 1);
				for (int i = fromIndex; i <= toIndex; i++) {
					Object v = resultsList.get(i);
					if (criteria.isReturnKey()) {
						list.add(new ValueInfo(makeKey(v, criteria), i));
					} else {
						// if (v instanceof Serializable){
						// list.add(new ValueInfo(transformEntity((Serializable)
						// v), i));
						// } else if (v instanceof StructImpl) {
						// list.add(new ValueInfo(v, i));
						// }
						list.add(new ValueInfo(transformEntity(v), i));
					}
				}
				memberResults.setResults(list);
				if (toIndex == resultsList.size() - 1) {
					memberResults.setNextBatchIndexOnServer(Constants.END_OF_LIST);
				} else {
					memberResults.setNextBatchIndexOnServer(toIndex + 1);
				}
			} catch (IndexOutOfBoundsException ex) {
				// ignore
			}
		} else {
			memberResults.setNextBatchIndexOnServer(Constants.END_OF_LIST);
		}
		return memberResults;
	}
}
