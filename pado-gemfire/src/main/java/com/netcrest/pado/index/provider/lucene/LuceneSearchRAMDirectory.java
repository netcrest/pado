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
package com.netcrest.pado.index.provider.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.util.BlobHelper;
import com.netcrest.pado.index.exception.IndexMatrixException;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.util.GridUtil;

public class LuceneSearchRAMDirectory
{
	private static LuceneSearchRAMDirectory search = new LuceneSearchRAMDirectory();
	
	public static StandardQueryParser parser = new StandardQueryParser(new StandardAnalyzer(Version.LUCENE_47));

	private LuceneSearchRAMDirectory()
	{
	}

	public static LuceneSearchRAMDirectory getLuceneSearch()
	{
		return search;
	}
	
	/**
	 * Returns a list of temporal identity keys.
	 * @param criteria Index matrix query criteria
	 */
	public List<TemporalEntry> searchTemporal(GridQuery criteria)
	{	
		List<Object> identityKeyList = getIdentityKeyList(criteria.getFullPath(), criteria.getQueryString());
		
		if (identityKeyList == null || identityKeyList.size() == 0) {
			return null;
		}
		
		String childPath = GridUtil.getChildPath(criteria.getFullPath());
		ITemporalBizLink temporalBiz = (ITemporalBizLink) PadoServerManager.getPadoServerManager().getCatalog().newInstanceLocal("com.netcrest.pado.biz.ITemporalBiz", 
				"com.netcrest.pado.biz.impl.gemfire.TemporalBizImplLocal", childPath);
		temporalBiz.getBizContext().getGridContextClient().setGridIds(criteria.getGridIds());
		HashSet set = new HashSet(identityKeyList);
		AttachmentSetFactory factory = new AttachmentSetFactory();
		AttachmentSet as = factory.createAttachmentSet(set);
		as.setGridPath(childPath);
		Map<ITemporalKey, ITemporalData> maps[] = temporalBiz.getAttachmentsEntries(new AttachmentSet[] { as });
		Map<ITemporalKey, ITemporalData> map = maps[0];
		map.entrySet();
		ArrayList<TemporalEntry> list = new ArrayList(map.size() + 1);
		for (Map.Entry<ITemporalKey, ITemporalData> entry : map.entrySet()) {
			TemporalEntry newEntry = TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(entry.getKey(), entry.getValue());
			list.add(newEntry);
		}
		return list;
	}
	
	/**
	 * Returns a list of temporal identity keys.
	 * @param criteria Index matrix query criteria
	 */
	public List getIdentityKeyList(String fullPath, String queryString)
	{	
		Cache cache = CacheFactory.getAnyInstance();
		Region<String, RAMDirectory> region = cache.getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_LUCENE));

		RAMDirectory directory = region.get(fullPath); 
		if (directory == null) {
			throw new IndexMatrixException("Lucene indexes have not been built for this query. [query=" + queryString + ", fullPath=" + fullPath + "]");
		}

		return getIdentityKeyList(queryString, directory);
	}
	
	private List<Object> getIdentityKeyList(String queryString, RAMDirectory dir)
	{
		List<Object> list = new ArrayList<Object>();
		IndexReader reader;
		try {
			reader = IndexReader.open(dir); 
		} catch (CorruptIndexException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1); 
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1); 
		} 
	    IndexSearcher searcher = new IndexSearcher(reader);

	    Query query;
		try {
			query = parser.parse(queryString, "IdentityKey");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex); 
		}
	    TopDocs results;
		try {
			results = searcher.search(query, null, Integer.MAX_VALUE);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1); 
		}
	    for (ScoreDoc hit : results.scoreDocs) {
	    	Document doc;
			try {
				doc = searcher.doc(hit.doc);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
				throw new RuntimeException(e); 
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e); 
			}
			IndexableField field = doc.getField("IdentityKey");
			if (field == null) {
				continue;
			}
			Object identityKey = null;
			BytesRef br = field.binaryValue();
			if (br != null) {
				byte[] blob = br.bytes;
				try {
					identityKey = BlobHelper.deserializeBlob(blob);
					list.add(identityKey);
				} catch (Exception ex) {
					Logger.warning("Identity key deserialization error", ex);
				}
			} else {
				identityKey = field.stringValue();
				list.add(identityKey);
			}
	    }		
	    return list;
	}
}
