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
package com.netcrest.pado.index.provider.lucene.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

/**
 * Removing '.'
 * {@link Analyzer} for English.
 * 
 */
public final class ExtendedEnglishAnalyzer extends StopwordAnalyzerBase {
	private final CharArraySet stemExclusionSet;

	/**
	 * Returns an unmodifiable instance of the default stop words set.
	 * 
	 * @return default stop words set.
	 */
	public static CharArraySet getDefaultStopSet() {
		return DefaultSetHolder.DEFAULT_STOP_SET;
	}

	/**
	 * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer
	 * class accesses the static final set the first time.;
	 */
	private static class DefaultSetHolder {
		static final CharArraySet DEFAULT_STOP_SET = StandardAnalyzer.STOP_WORDS_SET;
	}

	/**
	 * Builds an analyzer with the default stop words:
	 * {@link #getDefaultStopSet}.
	 */
	public ExtendedEnglishAnalyzer(Version matchVersion) {
		this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
	}

	/**
	 * Builds an analyzer with the given stop words.
	 * 
	 * @param matchVersion
	 *            lucene compatibility version
	 * @param stopwords
	 *            a stopword set
	 */
	public ExtendedEnglishAnalyzer(Version matchVersion, CharArraySet stopwords) {
		this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
	}

	/**
	 * Builds an analyzer with the given stop words. If a non-empty stem
	 * exclusion set is provided this analyzer will add a
	 * {@link SetKeywordMarkerFilter} before stemming.
	 * 
	 * @param matchVersion
	 *            lucene compatibility version
	 * @param stopwords
	 *            a stopword set
	 * @param stemExclusionSet
	 *            a set of terms not to be stemmed
	 */
	public ExtendedEnglishAnalyzer(Version matchVersion,
			CharArraySet stopwords, CharArraySet stemExclusionSet) {
		super(matchVersion, stopwords);
		this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(
				matchVersion, stemExclusionSet));
	}

	/**
	 * Creates a
	 * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} which
	 * tokenizes all the text in the provided {@link Reader}.
	 * 
	 * @return A
	 *         {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	 *         built from an {@link StandardTokenizer} filtered with
	 *         {@link StandardFilter}, {@link EnglishPossessiveFilter},
	 *         {@link LowerCaseFilter}, {@link StopFilter} ,
	 *         {@link SetKeywordMarkerFilter} if a stem exclusion set is
	 *         provided and {@link PorterStemFilter}.
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		final Tokenizer source = new StandardTokenizer(matchVersion, reader);
		TokenStream result = new StandardFilter(matchVersion, source);
		// prior to this we get the classic behavior, standardfilter does it for
		// us.
		if (matchVersion.onOrAfter(Version.LUCENE_31))
			result = new EnglishPossessiveFilter(matchVersion, result);
		result = new LowerCaseFilter(matchVersion, result);
		result = new StopFilter(matchVersion, result, stopwords);
		if (!stemExclusionSet.isEmpty())
			result = new SetKeywordMarkerFilter(result, stemExclusionSet);
		result = new PorterStemFilter(result);
		return new TokenStreamComponents(source, result);
	}

	@Override
	protected Reader initReader(String fieldName, Reader reader) {
		// TODO Auto-generated method stub
		NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
		builder.add(".", " ");
		//builder.add("_", " ");
		NormalizeCharMap normMap = builder.build();
		return new MappingCharFilter(normMap, reader);
	}

}
