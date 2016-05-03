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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.index.exception.IndexMatrixException;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.provider.ITextSearchProvider;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.PqlParser;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalAdminBizLink;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class LuceneSearch implements ITextSearchProvider

{
	public static final Version LUCENE_VERSION = Version.LUCENE_47;

	/**
	 * Contains all registered LuceneSearch objects. &gt;full-path,
	 * LuceneSearch&lt;.
	 */
	private final static Map<String, LuceneSearch> luceneSearchMap = new HashMap<String, LuceneSearch>();

	private static final DateTool.Resolution TIME_RESOLUTION = DateTool.Resolution.DAY;

	public static final String MIN_DATE = "19700101";
	public static final String MAX_DATE = "20991231";
	public static long MIN_TIME;
	public static long MAX_TIME;

	static {
		try {
			MIN_TIME = DateTool.stringToTime(MIN_DATE, TIME_RESOLUTION);
			MAX_TIME = DateTool.stringToTime(MAX_DATE, TIME_RESOLUTION);
		} catch (ParseException e) {
			// ignore
		}
	}

	/**
	 * Time query predicate. date range is (inclusive, exclusive)
	 */
	public final static String TIME_QUERY_PREDICATE = "StartValidTime:[" + MIN_DATE + " TO %s] AND EndValidTime:{%s TO "
			+ MAX_DATE + "] AND StartWrittenTime:[" + MIN_DATE + " TO %s] AND EndWrittenTime:{%s TO " + MAX_DATE + "]";

	public final static String START_WRITTEN_TIME_RANGE_QUERY_PREDICATE = "StartValidTime:[" + MIN_DATE
			+ " TO %s] AND EndValidTime:[%s TO " + MAX_DATE + "] AND StartWrittenTime:[" + "%s" + " TO %s]";

	protected LuceneSearch()
	{
	}

	/**
	 * Returns true if LuceneSearch exists for the specified full path.
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public static boolean isLuceneSearch(String fullPath)
	{
		return luceneSearchMap.containsKey(fullPath);
	}

	/**
	 * Returns LuceneSearch for the specified full path. It creates a new
	 * instance if LuceneSearch does not exist.
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public static LuceneSearch getLuceneSearch(String fullPath)
	{
		LuceneSearch search = luceneSearchMap.get(fullPath);
		if (search == null) {
			search = new LuceneSearch();
			luceneSearchMap.put(fullPath, search);
		}
		return search;
	}

	/**
	 * Returns parser.
	 */
	public StandardQueryParser createParser()
	{
		StandardQueryParser parser = new StandardQueryParser(new StandardAnalyzer(LUCENE_VERSION));
		return parser;
	}

	/**
	 * Returns a list of temporal entries.
	 * 
	 * @param criteria
	 *            Index matrix query criteria
	 */
	public List<TemporalEntry> searchTemporal(GridQuery criteria)
	{
		PqlParser pqlParser = new PqlParser(criteria.getFullPath(), criteria.getQueryString());
		Set<ITemporalKey> temporalKeySet = getTemporalKeySet(pqlParser.getFullPath(), pqlParser.getParsedQuery(),
				criteria.getServerLimit());
		if (temporalKeySet == null || temporalKeySet.size() == 0) {
			return null;
		}

		String childPath = GridUtil.getChildPath(pqlParser.getFullPath());
		ITemporalBizLink temporalBiz = (ITemporalBizLink) PadoServerManager.getPadoServerManager().getCatalog()
				.newInstanceLocal("com.netcrest.pado.biz.ITemporalBiz",
						"com.netcrest.pado.biz.impl.gemfire.TemporalBizImplLocal", childPath);
		temporalBiz.getBizContext().getGridContextClient().setGridIds(criteria.getGridIds());
		ITemporalAdminBizLink temporalAdminBiz = temporalBiz.getTemporalAdminBiz();
		Map<ITemporalKey, ITemporalData> map = temporalAdminBiz.getAll(temporalKeySet);
		ArrayList<TemporalEntry> list;
		if (map == null) {
			list = new ArrayList(0);
		} else {
			list = new ArrayList(map.size());
		}
		for (Map.Entry<ITemporalKey, ITemporalData> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				TemporalEntry newEntry = TemporalInternalFactory.getTemporalInternalFactory()
						.createTemporalEntry(entry.getKey(), entry.getValue());
				list.add(newEntry);
			}
		}
		return list;
	}

	// TODO: This method attempts to include attachments. See if we can
	// reinstate it.
	private List<TemporalEntry> searchTemporal_old(GridQuery criteria, int limit)
	{
		Set<Object> identityKeySet = getIdentityKeySet(criteria.getFullPath(), criteria.getQueryString(), limit);

		if (identityKeySet == null || identityKeySet.size() == 0) {
			return null;
		}

		String childPath = GridUtil.getChildPath(criteria.getFullPath());
		ITemporalBizLink temporalBiz = (ITemporalBizLink) PadoServerManager.getPadoServerManager().getCatalog()
				.newInstanceLocal("com.netcrest.pado.biz.ITemporalBiz",
						"com.netcrest.pado.biz.impl.gemfire.TemporalBizImplLocal", childPath);
		temporalBiz.getBizContext().getGridContextClient().setGridIds(criteria.getGridIds());
		HashSet set = new HashSet(identityKeySet);
		AttachmentSetFactory factory = new AttachmentSetFactory();
		AttachmentSet as = factory.createAttachmentSet(set);
		as.setGridPath(childPath);
		Map<ITemporalKey, ITemporalData> maps[] = temporalBiz.getAttachmentsEntries(new AttachmentSet[] { as });
		Map<ITemporalKey, ITemporalData> map = maps[0];
		map.entrySet();
		ArrayList<TemporalEntry> list = new ArrayList(map.size() + 1);
		for (Map.Entry<ITemporalKey, ITemporalData> entry : map.entrySet()) {
			TemporalEntry newEntry = TemporalInternalFactory.getTemporalInternalFactory()
					.createTemporalEntry(entry.getKey(), entry.getValue());
			list.add(newEntry);
		}
		return list;
	}

	/**
	 * Returns a non-null set of temporal identity keys.
	 * 
	 * @param criteria
	 *            Index matrix query criteria
	 */
	public Set getIdentityKeySet(String fullPath, String queryString, int limit)
	{
		Cache cache = CacheFactory.getAnyInstance();
		Region<String, RAMDirectory> region = cache
				.getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_LUCENE));

		Directory directory = region.get(fullPath);
		Set identityKeySet;
		if (directory == null) {
			File file = new File("lucene" + fullPath);
			if (file.exists() == false) {
				return Collections.emptySet();
			}
			try {
				directory = new MMapDirectory(file);
				identityKeySet = getIdentityKeySet(queryString, directory, limit);
			} catch (IOException e) {
				throw new IndexMatrixException("Lucene index directory error. [query=" + queryString + ", fullPath="
						+ fullPath + "] " + e.getMessage(), e);
			} finally {
				if (directory != null) {
					try {
						directory.close();
					} catch (IOException e) {
						Logger.error(e);
					}
				}
			}
		} else {
			identityKeySet = getIdentityKeySet(queryString, directory, limit);
		}
		return identityKeySet;
	}

	protected Set<Object> getIdentityKeySet(String queryString, Directory dir, int limit)
	{
		Set<Object> identityKeySet = new HashSet<Object>();
		DirectoryReader reader;
		try {
			reader = DirectoryReader.open(dir);
		} catch (CorruptIndexException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}

		Query query;
		try {
			StandardQueryParser parser = new StandardQueryParser(new StandardAnalyzer(LUCENE_VERSION));
			query = parser.parse(queryString.replaceAll("\\-", "\\\\-"), "__doc");
		} catch (Exception ex) {
			// Lucene bug. Unable to serialize exception. Log the message and
			// throw a new exception with the string message.
			ex.printStackTrace();
			throw new PadoException(ex.getMessage());
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs results;
		try {
			if (limit < 0) {
				limit = Integer.MAX_VALUE;
			}
			results = searcher.search(query, null, Integer.MAX_VALUE);

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
				// IndexableField field = doc.getField("IdentityKey");
				// if (field == null) {
				// continue;
				// }
				// Object identityKey = field.stringValue();
				// if (identityKey == null) {
				// identityKey = field.numericValue();
				// }
				// if (identityKey == null) {
				// BytesRef br = field.binaryValue();
				// if (br != null) {
				// byte[] blob = br.bytes;
				// try {
				// identityKey = BlobHelper.deserializeBlob(blob);
				// identityKeySet.add(identityKey);
				// } catch (Exception ex) {
				// Logger.warning("Identity key deserialization error", ex);
				// }
				// } else {
				// identityKey = field.toString();
				// }
				// }
				LuceneField luceneField = new LuceneField();
				ITemporalKey temporalKey = luceneField.getTemporalKey(doc);
				if (temporalKey != null) {
					identityKeySet.add(temporalKey.getIdentityKey());
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		return identityKeySet;
	}

	/**
	 * Returns a set of temporal keys.
	 * 
	 * @param fullPath
	 *            Full path
	 * @param queryString
	 *            Lucene query string
	 * @param limit
	 *            Result set limit size. -1 for no limit.
	 */
	public Set<ITemporalKey> getTemporalKeySet(String fullPath, String queryString, int limit)
	{
		Cache cache = CacheFactory.getAnyInstance();
		Region<String, RAMDirectory> region = cache
				.getRegion(IndexMatrixUtil.getProperty(Constants.PROP_REGION_LUCENE));

		Directory directory = region.get(fullPath);
		Set<ITemporalKey> temporalKeySet;
		if (directory == null) {
			File file = new File("lucene" + fullPath);
			if (file.exists() == false) {
				return Collections.emptySet();
			}
			try {
				directory = new MMapDirectory(file);
				temporalKeySet = getTemporalKeySet(queryString, directory, limit);
			} catch (IOException e) {
				throw new IndexMatrixException("Lucene index directory error. [query=" + queryString + ", fullPath="
						+ fullPath + "] " + e.getMessage(), e);
			} finally {
				if (directory != null) {
					try {
						directory.close();
					} catch (IOException e) {
						Logger.error(e);
					}
				}
			}
		} else {
			temporalKeySet = getTemporalKeySet(queryString, directory, limit);

		}
		return temporalKeySet;
	}

	protected Set<ITemporalKey> getTemporalKeySet(String queryString, Directory dir, int limit)
	{
		Set<ITemporalKey> temporalKeySet = new HashSet<ITemporalKey>();
		DirectoryReader reader;
		try {
			reader = DirectoryReader.open(dir);
		} catch (CorruptIndexException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		Query query;
		try {
			StandardQueryParser parser = new StandardQueryParser(new StandardAnalyzer(LUCENE_VERSION));
			query = parser.parse(queryString.replaceAll("\\-", "\\\\-"), "__doc");
		} catch (Exception ex) {
			// Lucene 4.7 bug, internal message not serializable
			// Send message instead of nesting the cause.
			throw new RuntimeException(ex.getMessage());
		}

		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs results;
		try {
			if (limit < 0) {
				limit = Integer.MAX_VALUE;
			}
			results = searcher.search(query, null, limit);

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
				LuceneField luceneField = new LuceneField();
				ITemporalKey temporalKey = luceneField.getTemporalKey(doc);
				if (temporalKey != null) {
					temporalKeySet.add(temporalKey);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		return temporalKeySet;
	}

	public String getTimeQuery(long validAtTime, long asOfTime, String queryString)
	{
		if (queryString != null) {
			queryString = queryString.trim();
		}
		String predicate = getTimePredicate(validAtTime, asOfTime);
		if (queryString == null || queryString.length() == 0) {
			return predicate;
		} else {
			return predicate + " AND (" + queryString.replaceAll("\\-", "\\\\-") + ")";
		}
	}

	public String getTimePredicate(long validAtTime, long asOfTime)
	{
		if (validAtTime == -1) {
			validAtTime = System.currentTimeMillis();
		}
		if (asOfTime == -1) {
			asOfTime = System.currentTimeMillis();
		}
		String validAtTimeString = DateTool.timeToString(validAtTime, TIME_RESOLUTION);
		String asOfTimeString = DateTool.timeToString(asOfTime, TIME_RESOLUTION);

		return String.format(TIME_QUERY_PREDICATE, validAtTimeString, validAtTimeString, asOfTimeString,
				asOfTimeString);
	}

	public String getWrittenTimeRangeQuery(long validAtTime, long fromWrittenTime, long toWrittenTime,
			String queryString)
	{
		if (queryString != null) {
			queryString = queryString.trim();
		}
		String predicate = getWrittenTimeRangePredicate(validAtTime, fromWrittenTime, toWrittenTime);
		if (queryString == null || queryString.length() == 0) {
			return predicate;
		} else {
			return predicate + " AND (" + queryString.replaceAll("\\-", "\\\\-") + ")";
		}
	}

	public String getWrittenTimeRangePredicate(long validAtTime, long fromWrittenTime, long toWrittenTime)
	{
		if (validAtTime == -1) {
			validAtTime = System.currentTimeMillis();
		}
		if (fromWrittenTime == -1) {
			fromWrittenTime = System.currentTimeMillis();
		}
		if (toWrittenTime == -1) {
			toWrittenTime = System.currentTimeMillis();
		}
		String validAtTimeString = DateTool.timeToString(validAtTime, TIME_RESOLUTION);
		String fromWrittenTimeString = DateTool.timeToString(fromWrittenTime, TIME_RESOLUTION);
		String toWrittenTimeString = DateTool.timeToString(toWrittenTime, TIME_RESOLUTION);

		return String.format(START_WRITTEN_TIME_RANGE_QUERY_PREDICATE, validAtTimeString, validAtTimeString,
				fromWrittenTimeString, toWrittenTimeString);
	}

	@Override
	public List<?> combineAndSort(List<?> entities, GridQuery criteria, boolean isMember)
			throws UnsupportedOperationException
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
