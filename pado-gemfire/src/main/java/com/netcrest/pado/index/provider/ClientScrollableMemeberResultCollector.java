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
package com.netcrest.pado.index.provider;

import com.gemstone.gemfire.DataSerializable;
import com.netcrest.pado.index.result.BaseMemberResultCollector;
import com.netcrest.pado.index.result.ClientResults;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.IScrollableResultSet;

public class ClientScrollableMemeberResultCollector extends
		BaseMemberResultCollector  {

	private ClientResults clientResults = null;	
	
	public IScrollableResultSet<DataSerializable> getClientResults() {
		return clientResults;
	}

	public ClientScrollableMemeberResultCollector(GridQuery query, IIndexMatrixProvider provider, ClientResults clientResults) {
		super(query, provider);
		this.clientResults = clientResults;
	}

	@Override
	public void endResults() {
		super.endResults();
		this.clientResults.setValueResultList(getResult().getAggregatedSortedResults());
	}

}
