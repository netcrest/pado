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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import org.apache.lucene.util.Version;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.result.IndexableResult;
import com.netcrest.pado.index.result.ResultItem;
import com.netcrest.pado.index.result.ValueInfo;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.pql.PqlParser;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TopNLuceneSearch extends LuceneSearch

{
	public static final Version LUCENE_VERSION = Version.LUCENE_7_7_1;

	/**
	 * Contains all registered LuceneSearch objects. &gt;full-path,
	 * LuceneSearch&lt;.
	 */
	private final static Map<String, TopNLuceneSearch> luceneSearchMap = new HashMap<String, TopNLuceneSearch>();

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
	public final static String TIME_QUERY_PREDICATE = "StartValidTime:["
			+ MIN_DATE + " TO %s] AND EndValidTime:{%s TO " + MAX_DATE
			+ "] AND StartWrittenTime:[" + MIN_DATE
			+ " TO %s] AND EndWrittenTime:{%s TO " + MAX_DATE + "]";

	public final static String START_WRITTEN_TIME_RANGE_QUERY_PREDICATE = "StartValidTime:["
			+ MIN_DATE
			+ " TO %s] AND EndValidTime:[%s TO "
			+ MAX_DATE
			+ "] AND StartWrittenTime:[" + "%s" + " TO %s]";

	private TopNLuceneSearch() {
	}

	/**
	 * Returns LuceneSearch for the specified full path. It creates a new
	 * instance if LuceneSearch does not exist.
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public static TopNLuceneSearch getTopLuceneSearch(String fullPath) {
		TopNLuceneSearch search = luceneSearchMap.get(fullPath);
		if (search == null) {
			search = new TopNLuceneSearch();
			luceneSearchMap.put(fullPath, search);
		}
		return search;
	}

	protected Set<Object> getIdentityKeySet(String queryString, Directory dir) {
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
			StandardQueryParser parser = new StandardQueryParser(
					new StandardAnalyzer());
			query = parser.parse(queryString.replaceAll("\\-", "\\\\-"),
					"__doc");
		} catch (Exception ex) {
			// Lucene bug. Unable to serialize exception. Log the message and
			// throw a new exception with the string message.
			ex.printStackTrace();
			throw new PadoException(ex.getMessage());
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs results;
		try {
			results = searcher.search(query, Integer.MAX_VALUE);
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
					float docScore = hit.score;
					identityKeySet.add(temporalKey.getIdentityKey());
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		return identityKeySet;
	}

	protected Set<ITemporalKey> getTemporalKeySet(String queryString,
			Directory dir) {
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
			StandardQueryParser parser = new StandardQueryParser(
					new StandardAnalyzer());
			query = parser.parse(queryString.replaceAll("\\-", "\\\\-"),
					"__doc");
		} catch (Exception ex) {
			// Lucene 4.7 bug, internal message not serializable
			// Send message instead of nesting the cause.
			throw new RuntimeException(ex.getMessage());
		}

		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs results;
		try {
			results = searcher.search(query, Integer.MAX_VALUE);

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

	@Override
	public List combineAndSort(List entities, GridQuery criteria,
			boolean isMember) {
		// TODO Auto-generated method stub
		ITemporalKey tk = null;
		Map<String, Object> intermMap = new HashMap<String, Object>(
				entities.size());
		List<Object> finalResult = new ArrayList(entities.size());
		List<JsonLite> sortedSet = new ArrayList<JsonLite>(entities.size());
		String toCompare = PqlParser.getTextSearchPHRASE(criteria);
		String targetField = PqlParser.getTextSearchTargetField(criteria);
		if (!isMember) {
			int i = 0;
			for (Object obj : entities) {
				intermMap.put(String.valueOf(i), obj);
				IndexableResult<Integer, Object> indexableResult = (IndexableResult<Integer, Object>) obj;
				Object rsObj = indexableResult.getValue();
				TemporalEntry resultTemEntry = null;
				if (rsObj instanceof ResultItem) {
					ResultItem<Object> valueObj = (ResultItem<Object>) indexableResult
							.getValue();
					Object resultObj = ((ValueInfo) valueObj.getItem())
							.getValue();

					if (resultObj instanceof TemporalEntry) {
						resultTemEntry = ((TemporalEntry) resultObj);
					}
				} else if (rsObj instanceof TemporalEntry) {
					resultTemEntry = ((TemporalEntry) rsObj);
				}
				tk = resultTemEntry.getTemporalKey();
				ITemporalData temporaData = ((TemporalEntry) resultTemEntry)
						.getTemporalData();
				if (temporaData instanceof GemfireTemporalData) {
					JsonLite gemFireData = (JsonLite) ((GemfireTemporalData) temporaData)
							.getValue();
					gemFireData.put("Temp_index", String.valueOf(i));
					sortedSet.add(gemFireData);
				} else {
					// what to do it here ??
				}

				i++;
			}
		} else {
			int i = 0;
			for (Object obj : entities) {
				intermMap.put(String.valueOf(i), obj);
				ITemporalData temporaData = ((TemporalEntry) obj)
						.getTemporalData();
				tk = ((TemporalEntry) obj).getTemporalKey();
				JsonLite gemFireData = (JsonLite) ((GemfireTemporalData) temporaData)
						.getValue();
				String targetFieldValue = (String) gemFireData.get(targetField);
				double score = TopNLuceneSearch.getLSScore(toCompare,
						targetFieldValue);
				gemFireData.put(Constants.TEXT_SEARCH_SCORE, score);

				gemFireData.put("Temp_index", String.valueOf(i));
				sortedSet.add(gemFireData);
				i++;
			}

		}
		Collections.sort(sortedSet, new Comparator<JsonLite>() {
			@Override
			public int compare(JsonLite o1, JsonLite o2) {
				// TODO Auto-generated method stub
				Double o2Score = (Double) o2.get(Constants.TEXT_SEARCH_SCORE);
				Double o1Score = (Double) o1.get(Constants.TEXT_SEARCH_SCORE);
				return o2Score.doubleValue() == o1Score.doubleValue() ? 0
						: (o2Score.doubleValue() > o1Score.doubleValue() ? 1
								: -1);
			}
		});

		int j = 0;
		for (JsonLite lite : sortedSet) {
			String index = (String) lite.get("Temp_index");
			lite.remove("Temp_index");
			Object originalEntity = intermMap.get(index);
			finalResult.add(originalEntity);
			j++;
		}
		entities.clear();
		entities.addAll(finalResult);

		return finalResult;
	}

	public static double getLSScore(String fromValue, String targetFieldValue) {
		// TODO Auto-generated method stub
		double maxLen = Math.max(fromValue.length(), targetFieldValue.length());
		double lsDistance = StringUtils.getLevenshteinDistance(fromValue,
				targetFieldValue);
		return 1d - lsDistance / maxLen;
	}

}
