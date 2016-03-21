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
package com.netcrest.pado.biz.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.index.provider.lucene.DateTool;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.ClassUtil;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.temporal.TemporalUtil;
import com.netcrest.pado.util.IBulkLoader;

/**
 * SchemaInfo contains data file parsing and loading information. All file data
 * loader classes must implement {@link IFileLoader} which provides SchemInfo
 * constructed by the underlying file loader mechanics.
 * 
 * @author dpark
 * 
 */
public class SchemaInfo
{
	public final static String PROP_IS_KEY_COLUMNS = "IsKeyColumns";
	public final static String PROP_IS_KEY_AUTO_GEN = "IsKeyAutoGen";
	public final static String PROP_DELIMITER = "Delimiter";
	public final static String PROP_KEY_CLASS = "KeyClass";
	public final static String PROP_VALUE_CLASS = "ValueClass";
	public final static String PROP_KEY_TYPE_CLASS = "KeyTypeClass";
	public final static String PROP_ROUTING_KEY_CLASS = "RoutingKeyClass";
	public final static String PROP_GRID_PATH = "GridPath";
	public final static String PROP_BULK_LAODER_CLASS = "BulkLoaderClass";
	public final static String PROP_FILE_LOADER_CLASS = "FileLoaderClass";
	public final static String PROP_BATCH_SIZE = "BatchSize";
	public final static String PROP_DATE_FORMAT = "DateFormat";
	public final static String PROP_IS_CASE_SENSITIVE = "IsCaseSensitive";
	public final static String PROP_IS_TEMPORAL = "IsTemporal";
	public final static String PROP_IS_HISTORY = "IsHistory";
	public final static String PROP_TEMPORAL_TYPE = "TemporalType";
	public final static String PROP_TEMPORAL_START_VALID_TIME = "StartValidTime";
	public final static String PROP_TEMPORAL_END_VALID_TIME = "EndValidTime";
	public final static String PROP_TEMPORAL_WRITTEN_TIME = "WrittenTime";
	public final static String PROP_TEMPORAL_TIME_RESOLUTION = "TemporalTimeResolution";
	public final static String PROP_USER_NAME = "Username";
	public final static String PROP_SKIP_COLUMNS = "SkipColumns";
	public final static String PROP_ROUTING_KEY_INDEXES = "RoutingKeyIndexes";
	public final static String PROP_START_ROW = "StartRow";
	public final static String PROP_END_ROW = "EndRow";
	public final static String PROP_IS_SPLIT = "IsSplit";
	public final static String PROP_ROW_FILTER_CLASS = "RowFilterClass";
	public final static String PROP_ENTRY_FILTER_CLASS = "EntryFilterClass";
	public final static String PROP_CHARSET = "Charset";
	public final static String PROP_LINE_SEPARATOR = "LineSeparator";
	public final static String PROP_QUOTE_ESCAPE = "QuoteEscape";
	public final static String PROP_COMPOSITE_KEY_DELIMITER = "CompositeKeyDelimiter";

	private final static String DEFAULT_BULK_LOADER_CLASS_NAME = PadoUtil.getProperty(
			Constants.PROP_LOADER_DATA_BULK_LOADER_CLASS,
			PadoUtil.getProperty(Constants.PROP_CLASS_BULK_LOADER, Constants.DEFAULT_CLASS_BULK_LOADER));
	private final static String DEFAULT_FILE_LOADER_CLASS_NAME = PadoUtil.getProperty(
			Constants.PROP_LOADER_DATA_FILE_LOADER_CLASS,
			PadoUtil.getProperty(Constants.PROP_CLASS_FILE_LOADER, Constants.DEFAULT_CLASS_FILE_LOADER));

	private char schemaFileDelimiter;

	private String schemaType;
	private boolean isKeyColumns;
	private boolean isKeyAutoGen = false;
	private String gridPath;
	private char delimiter = 29; // default is ASCII ALT (029)
	private char quoteEscape = '"';
	private String lineSeparator = "\n";
	private Class<?> keyClass = String.class;
	private Class<?> valueClass;
	private KeyType keyType;
	private String keyTypeClassName;
	private Class<?> routingKeyClass;
	private ColumnItem[] allColumnItems;
	private HashSet<String> skipColumnSet = new HashSet<String>(1);
	private String[] pkColumnNames;
	private String[] pkIndexNames;
	private String[] routingKeyIndexNames = new String[0];
	private int[] routingKeyIndexes = new int[0];
	private String[] valueColumnNames;
	private Class<?>[] valueColumnTypes;
	private Class<?> bulkLoaderClass;
	private Class<?> fileLoaderClass;
	private Class<?> rowFilterClass;
	private Class<?> entryFilterClass;
	private int batchSize = Integer
			.parseInt(PadoUtil.getProperty(Constants.PROP_LOADER_DATA_BULK_LOAD_BATCH_SIZE, "5000"));
	private String dateFormat = "MM/dd/yyyy HH:mm:ss";
	private boolean columnNamesCaseSensitive = true;
	private boolean isSplit = true;
	private String charset = "US-ASCII";
	private String compositeKeyDelimiter = ".";

	private boolean isTemporal = true;
	private String temporalType = "eternal";
	private Date temporalStartTime = TemporalUtil.MIN_DATE;
	private Date temporalEndTime = TemporalUtil.MAX_DATE;
	private Date temporalWrittenTime = temporalStartTime;
	private DateTool.Resolution temporalTimeResolution = DateTool.Resolution.DAY;
	private String username;
	private int startRow = 1;
	private int endRow = 0;
	private boolean isHistory = false;
	private int keyStartIndex = -1;
	private int temporalStartIndex = -1;
	private int valueStartIndex;

	public SchemaInfo(String schemaType, File schemaFile) throws FileLoaderException
	{
		this(schemaType, schemaFile, ',');
	}

	public SchemaInfo(String schemaType, File schemaFile, char schemaFileDelimiter) throws FileLoaderException
	{
		if (schemaFile == null) {
			throw new FileLoaderException("Schema file not found. schema-type=" + schemaType);
		}
		this.schemaType = schemaType;
		this.schemaFileDelimiter = schemaFileDelimiter;

		// Supports CSV schema files only
		// For XML, read the schema file to validate.
		loadCsvSchema(schemaFile);
	}

	private void loadCsvSchema(File schemaFile)
	{
		BufferedReader reader = null;
		int lineCount = 0;
		String temporalStartTimeStr = null;
		String temporalEndTimeStr = null;
		String temporalWrittenTimeStr = null;
		try {
			reader = new BufferedReader(new FileReader(schemaFile));
			String line;
			ArrayList<ColumnItem> columnItemList = new ArrayList<ColumnItem>();
			ArrayList<String> pkList = new ArrayList<String>(4);
			ArrayList<String> temporalList = new ArrayList<String>(4);
			ArrayList<ColumnItem> pkColumnItemList = new ArrayList<ColumnItem>(4);
			ArrayList<String> routingKeyList = new ArrayList<String>(4);
			ArrayList<String> valueColumnNameList = new ArrayList<String>();
			ArrayList<Class<?>> valueColumnTypeList = new ArrayList<Class<?>>();
			while ((line = reader.readLine()) != null) {
				lineCount++;
				String line2 = line.trim();
				if (line2.startsWith("#")) {
					continue;
				}

				String split[] = line.split("=");
				if (line.endsWith("=")) {
					String tmp[] = new String[2];
					tmp[0] = split[0];
					tmp[1] = "";
					split = tmp;
				}
				if (split.length >= 2) {
					String property = split[0].trim();
					String value;
					if (property.equalsIgnoreCase(PROP_DELIMITER)) {
						value = split[1];
					} else {
						value = split[1].trim();
					}
					if (value.length() == 0) {
						continue;
					}
					if (property.equalsIgnoreCase(PROP_IS_KEY_COLUMNS)) {
						this.isKeyColumns = value.equalsIgnoreCase("true");
					} else if (property.equalsIgnoreCase(PROP_IS_KEY_AUTO_GEN)) {
						this.isKeyAutoGen = value.equalsIgnoreCase("true");
					} else if (property.equalsIgnoreCase(PROP_DELIMITER)) {
						char[] carray = value.toCharArray();
						if (carray != null && carray.length > 0) {
							this.delimiter = carray[0];
						}
					} else if (property.equalsIgnoreCase(PROP_KEY_CLASS)) {
						if (this.keyClass == null) {
							this.keyClass = ClassUtil.getType(value);
							if (this.keyClass == null) {
								this.keyClass = Class.forName(value);
							}
						}
					} else if (property.equalsIgnoreCase(PROP_VALUE_CLASS)) {
						if (this.valueClass == null) {
							this.valueClass = Class.forName(value);
						}
					} else if (property.equalsIgnoreCase(PROP_KEY_TYPE_CLASS)) {
						if (this.keyType == null) {
							this.keyTypeClassName = value;
							try {
								Class<?> clazz = Class.forName(value);
								KeyType[] types = (KeyType[]) clazz.getEnumConstants();
								if (types != null) {
									this.keyType = types[0];
								}
							} catch (ClassNotFoundException ex) {
								// Ignore. If the key type class is not defined
								// then the class name can be used to generate
								// the key type class.
							}
						}
					} else if (property.equalsIgnoreCase(PROP_ROUTING_KEY_CLASS)) {
						if (this.routingKeyClass == null) {
							this.routingKeyClass = Class.forName(value);
						}
					} else if (property.equalsIgnoreCase(PROP_ROW_FILTER_CLASS)) {
						if (this.rowFilterClass == null) {
							this.rowFilterClass = Class.forName(value);
						}
					} else if (property.equalsIgnoreCase(PROP_ENTRY_FILTER_CLASS)) {
						if (this.entryFilterClass == null) {
							this.entryFilterClass = Class.forName(value);
						}
					} else if (property.equalsIgnoreCase(PROP_GRID_PATH)) {
						this.gridPath = value;
					} else if (property.equalsIgnoreCase(PROP_BULK_LAODER_CLASS)) {
						this.bulkLoaderClass = Class.forName(value);
					} else if (property.equalsIgnoreCase(PROP_FILE_LOADER_CLASS)) {
						this.fileLoaderClass = Class.forName(value);
					} else if (property.equalsIgnoreCase(PROP_BATCH_SIZE)) {
						this.batchSize = Integer.parseInt(value);
					} else if (property.equalsIgnoreCase(PROP_DATE_FORMAT)) {
						this.dateFormat = value;
					} else if (property.equalsIgnoreCase(PROP_IS_CASE_SENSITIVE)) {
						this.columnNamesCaseSensitive = value.equalsIgnoreCase("true");
					} else if (property.equalsIgnoreCase(PROP_IS_TEMPORAL)) {
						this.isTemporal = value.equalsIgnoreCase("true");
					} else if (property.equalsIgnoreCase(PROP_IS_HISTORY)) {
						this.isHistory = value.equalsIgnoreCase("true");
					} else if (property.equalsIgnoreCase(PROP_TEMPORAL_TYPE)) {
						this.temporalType = value;
					} else if (property.equalsIgnoreCase(PROP_TEMPORAL_START_VALID_TIME)) {
						temporalStartTimeStr = value;
					} else if (property.equalsIgnoreCase(PROP_TEMPORAL_END_VALID_TIME)) {
						temporalEndTimeStr = value;
					} else if (property.equalsIgnoreCase(PROP_TEMPORAL_WRITTEN_TIME)) {
						temporalWrittenTimeStr = value;
					} else if (property.equalsIgnoreCase(PROP_TEMPORAL_TIME_RESOLUTION)) {
						temporalTimeResolution = DateTool.Resolution.valueOf(value.toUpperCase());
					} else if (property.equalsIgnoreCase(PROP_USER_NAME)) {
						username = value;
					} else if (property.equalsIgnoreCase(PROP_SKIP_COLUMNS)) {
						String skipColumns[] = getTokens(value, ',');
						skipColumnSet = new HashSet<String>(skipColumns.length, 1f);
						if (skipColumns != null) {
							for (String column : skipColumns) {
								skipColumnSet.add(column.trim());
							}
						}
					} else if (property.equalsIgnoreCase(PROP_ROUTING_KEY_INDEXES)) {
						String rkarray[] = getTokens(value, ',');
						routingKeyIndexes = new int[rkarray.length];
						for (int i = 0; i < rkarray.length; i++) {
							routingKeyIndexes[i] = Integer.parseInt(rkarray[i]);
						}
					} else if (property.equalsIgnoreCase(PROP_START_ROW)) {
						this.startRow = Integer.parseInt(value);
					} else if (property.equalsIgnoreCase(PROP_END_ROW)) {
						this.endRow = Integer.parseInt(value);
					} else if (property.equalsIgnoreCase(PROP_IS_SPLIT)) {
						this.isSplit = value.equalsIgnoreCase("true");
					} else if (property.equalsIgnoreCase(PROP_LINE_SEPARATOR)) {
						this.lineSeparator = value;
					} else if (property.equalsIgnoreCase(PROP_QUOTE_ESCAPE)) {
						char[] carray = value.toCharArray();
						if (carray != null && carray.length > 0) {
							this.quoteEscape = carray[0];
						}
					} else if (property.equalsIgnoreCase(PROP_COMPOSITE_KEY_DELIMITER)) {
						this.compositeKeyDelimiter = value;
					}
				} else {
					String tokens[] = getTokens(line, schemaFileDelimiter);
					if (tokens != null) {
						if (tokens.length > 0) {
							ColumnItem ci = new ColumnItem();
							ci.name = tokens[0].trim();
							if (tokens.length > 1) {
								String typeName = tokens[1].trim();
								ci.type = ClassUtil.getType(tokens[1].trim());
								if (ci.type == null) {
									ci.type = Class.forName(typeName);
								}
							} else {
								ci.type = String.class;
							}
							if (tokens.length > 2) {
								String token = tokens[2].trim();
								if (token.equalsIgnoreCase(ColumnCategory.Primary.name())) {
									ci.category = ColumnCategory.Primary;
									pkList.add(ci.name);
									if (isKeyColumns == false) {
										valueColumnNameList.add(ci.name);
										valueColumnTypeList.add(ci.type);
									}
								}
								if (token.equalsIgnoreCase(ColumnCategory.PrimaryRouting.name())) {
									ci.category = ColumnCategory.PrimaryRouting;
									pkList.add(ci.name);
									if (isKeyColumns == false) {
										valueColumnNameList.add(ci.name);
										valueColumnTypeList.add(ci.type);
									}
									ci.isRoutingKey = true;
									routingKeyList.add(ci.name);
								} else if (token.equalsIgnoreCase(ColumnCategory.Temporal.name())) {
									ci.category = ColumnCategory.Temporal;
									temporalList.add(ci.name);
									if (isKeyColumns == false) {
										valueColumnNameList.add(ci.name);
										valueColumnTypeList.add(ci.type);
									}
								} else if (token.equalsIgnoreCase(ColumnCategory.Value.name())) {
									ci.category = ColumnCategory.Value;
									valueColumnNameList.add(ci.name);
									valueColumnTypeList.add(ci.type);
								}
							} else {
								valueColumnNameList.add(ci.name);
								valueColumnTypeList.add(ci.type);
							}
							// PrimaryKeyIndex
							if (tokens.length > 3) {
								String token = tokens[3].trim();
								if (token.length() > 0) {
									int primaryKeyIndex = Integer.parseInt(token);
									ci.primaryKeyIndex = primaryKeyIndex;
								}
								if (ci.primaryKeyIndex != -1) {
									pkColumnItemList.add(ci);
								}
							}
						}
					}
				}
			}
			this.allColumnItems = columnItemList.toArray(new ColumnItem[columnItemList.size()]);
			this.pkColumnNames = pkList.toArray(new String[pkList.size()]);

			// pkIndexNames must in the sequence that is defined by the index
			// numbers in the schema file
			this.pkIndexNames = new String[pkColumnItemList.size()];
			for (ColumnItem ci : pkColumnItemList) {
				pkIndexNames[ci.getPrimaryKeyIndex()] = ci.name;
			}

			this.routingKeyIndexNames = routingKeyList.toArray(new String[routingKeyList.size()]);
			this.valueColumnNames = valueColumnNameList.toArray(new String[valueColumnNameList.size()]);
			this.valueColumnTypes = valueColumnTypeList.toArray(new Class[valueColumnTypeList.size()]);
			if (this.bulkLoaderClass == null) {
				this.bulkLoaderClass = Class.forName(DEFAULT_BULK_LOADER_CLASS_NAME);
			}
			if (this.fileLoaderClass == null) {
				this.fileLoaderClass = Class.forName(DEFAULT_FILE_LOADER_CLASS_NAME);
			}
			SimpleDateFormat sdf = new SimpleDateFormat(this.dateFormat);
			if (temporalStartTimeStr != null) {
				this.temporalStartTime = sdf.parse(temporalStartTimeStr);
			}
			if (temporalEndTimeStr != null) {
				this.temporalEndTime = sdf.parse(temporalEndTimeStr);
			}
			if (temporalWrittenTimeStr != null) {
				this.temporalWrittenTime = sdf.parse(temporalWrittenTimeStr);
			}

			if (isKeyColumns) {
				keyStartIndex = 0;
			} else {
				keyStartIndex = -1;
			}
			if (temporalList.size() == 0) {
				temporalStartIndex = -1;
			} else {
				temporalStartIndex = pkList.size();
			}
			if (isKeyColumns) {
				valueStartIndex = pkList.size() + temporalList.size();
			} else {
				valueStartIndex = 0;
			}

			if (keyType != null) {
				Set<String> nameSet = keyType.getNameSet();
				for (String columnName : valueColumnNames) {
					if (isSkipColumn(columnName) == false && nameSet.contains(columnName) == false) {
						throw new FileLoaderException(
								"Error occurred while reading file schema file " + schemaFile.getAbsolutePath()
										+ ". Field name undefined in " + keyTypeClassName + ": " + columnName);
					}
				}
			}

		} catch (Exception ex) {
			throw new FileLoaderException("Error occurred while reading file schema file "
					+ schemaFile.getAbsolutePath() + ". Line number: " + lineCount, ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public IBulkLoader createBulkLoader() throws InstantiationException, IllegalAccessException
	{
		IBulkLoader bulkLoader = (IBulkLoader) bulkLoaderClass.newInstance();
		bulkLoader.setPath(gridPath);
		return bulkLoader;
	}

	public IFileLoader createFileLoader() throws InstantiationException, IllegalAccessException
	{
		return (IFileLoader) fileLoaderClass.newInstance();
	}

	/**
	 * Returns the schema file delimiter (separator). The default is ','. The
	 * schema file delimiter applies only for delimited files. It does not apply
	 * to XML files.
	 */
	public char getSchemaFileDelimiter()
	{
		return schemaFileDelimiter;
	}

	public String getSchemaType()
	{
		return schemaType;
	}

	public String getGridPath()
	{
		return gridPath;
	}

	public char getDelimiter()
	{
		return delimiter;
	}

	public String getLineSeparator()
	{
		return lineSeparator;
	}

	public char getQuoteEscape()
	{
		return quoteEscape;
	}

	public String getCompositeKeyDelimiter()
	{
		return compositeKeyDelimiter;
	}

	public Class<?> getKeyClass()
	{
		return keyClass;
	}

	public ColumnItem[] getAllColumnItems()
	{
		return allColumnItems;
	}

	public String[] getPkColumnNames()
	{
		return pkColumnNames;
	}

	public String[] getPkIndexNames()
	{
		return pkIndexNames;
	}

	/**
	 * Returns routing key index names. Always returns a non-null array. The
	 * array length is zero if undefined. Routing key indexes overrides routing
	 * key index names.
	 */
	public String[] getRoutingKeyIndexNames()
	{
		return routingKeyIndexNames;
	}

	/**
	 * Returns routing key indexes. Always returns a non-null array. The array
	 * length is zero undefined. Routing key indexes overrides routing key index
	 * names.
	 */
	public int[] getRoutingKeyIndexes()
	{
		return routingKeyIndexes;
	}

	public String[] getValueColumnNames()
	{
		return valueColumnNames;
	}

	public Class<?>[] getValueColumnTypes()
	{
		return valueColumnTypes;
	}

	public Class<?> getBulkLoaderClass()
	{
		return bulkLoaderClass;
	}

	public Class<?> getFileLoaderClass()
	{
		return fileLoaderClass;
	}

	public int getBatchSize()
	{
		return batchSize;
	}

	public String getDateFormat()
	{
		return dateFormat;
	}

	public boolean isKeyColumns()
	{
		return isKeyColumns;
	}

	public boolean isKeyAutoGen()
	{
		return isKeyAutoGen;
	}

	public boolean isColumnNamesCaseSensitive()
	{
		return columnNamesCaseSensitive;
	}

	public Class<?> getValueClass()
	{
		return valueClass;
	}

	public Class<?> getRowFilterClass()
	{
		return rowFilterClass;
	}

	public Class<?> getEntryFilterClass()
	{
		return entryFilterClass;
	}

	public KeyType getKeyType()
	{
		return keyType;
	}

	public String getKeyTypeClassName()
	{
		return keyTypeClassName;
	}

	public Class<?> getRoutingKeyClass()
	{
		return routingKeyClass;
	}

	public boolean isSkipColumn(String columnName)
	{
		return skipColumnSet.contains(columnName);
	}

	public Set<String> getSkipColumnSet()
	{
		return skipColumnSet;
	}

	public boolean isTemporal()
	{
		return isTemporal;
	}

	public String getTemporalType()
	{
		return temporalType;
	}

	public Date getTemporalStartTime()
	{
		return temporalStartTime;
	}

	public Date getTemporalEndTime()
	{
		return temporalEndTime;
	}

	public Date getTemporalWrittenTime()
	{
		return temporalWrittenTime;
	}

	public DateTool.Resolution getTemporalTimeResolution()
	{
		return temporalTimeResolution;
	}

	public String getUsername()
	{
		return username;
	}

	public int getStartRow()
	{
		return startRow;
	}

	public void setStartRow(int startRow)
	{
		if (startRow <= 0) {
			this.startRow = 0;
		} else {
			this.startRow = startRow;
		}
	}

	public int getEndRow()
	{
		return endRow;
	}

	public boolean isHistory()
	{
		return isHistory;
	}

	public int getKeyStartIndex()
	{
		return keyStartIndex;
	}

	public int getTemporalStartIndex()
	{
		return temporalStartIndex;
	}

	public int getValueStartIndex()
	{
		return valueStartIndex;
	}

	public boolean isSplit()
	{
		return isSplit;
	}

	public String getCharset()
	{
		return charset;
	}

	@Override
	public String toString()
	{
		return "SchemaInfo [schemaFileDelimiter=" + schemaFileDelimiter + ", schemaType=" + schemaType
				+ ", isKeyColumns=" + isKeyColumns + ", isKeyAutoGen=" + isKeyAutoGen + ", gridPath=" + gridPath
				+ ", delimiter=" + delimiter + ", quoteEscape=" + quoteEscape + ", lineSeparator=" + lineSeparator
				+ ", keyClass=" + keyClass + ", valueClass=" + valueClass + ", keyType=" + keyType
				+ ", keyTypeClassName=" + keyTypeClassName + ", routingKeyClass=" + routingKeyClass
				+ ", allColumnItems=" + Arrays.toString(allColumnItems) + ", skipColumnSet=" + skipColumnSet
				+ ", pkColumnNames=" + Arrays.toString(pkColumnNames) + ", pkIndexNames="
				+ Arrays.toString(pkIndexNames) + ", routingKeyIndexNames=" + Arrays.toString(routingKeyIndexNames)
				+ ", routingKeyIndexes=" + Arrays.toString(routingKeyIndexes) + ", valueColumnNames="
				+ Arrays.toString(valueColumnNames) + ", valueColumnTypes=" + Arrays.toString(valueColumnTypes)
				+ ", bulkLoaderClass=" + bulkLoaderClass + ", fileLoaderClass=" + fileLoaderClass + ", rowFilterClass="
				+ rowFilterClass + ", entryFilterClass=" + entryFilterClass + ", batchSize=" + batchSize
				+ ", dateFormat=" + dateFormat + ", columnNamesCaseSensitive=" + columnNamesCaseSensitive + ", isSplit="
				+ isSplit + ", charset=" + charset + ", compositeKeyDelimiter=" + compositeKeyDelimiter
				+ ", isTemporal=" + isTemporal + ", temporalType=" + temporalType + ", temporalStartTime="
				+ temporalStartTime + ", temporalEndTime=" + temporalEndTime + ", temporalWrittenTime="
				+ temporalWrittenTime + ", temporalTimeResolution=" + temporalTimeResolution + ", username=" + username
				+ ", startRow=" + startRow + ", endRow=" + endRow + ", isHistory=" + isHistory + ", keyStartIndex="
				+ keyStartIndex + ", temporalStartIndex=" + temporalStartIndex + ", valueStartIndex=" + valueStartIndex
				+ "]";
	}

	private static String[] getTokens(String line, char delimiter)
	{
		if (line.length() == 0) {
			return null;
		}
		// HBAN,23.82,300,23.79,800,"Thu, ""test"", 'hello' Jun 08 09:41:19 EDT
		// 2006",99895,1094931009,82,99895,8,HBAN
		ArrayList<String> list = new ArrayList<String>();
		boolean openQuote = false;
		String value = "";
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (c == delimiter) {
				if (openQuote == false) {
					value = value.trim();
					if (value.startsWith("\"") && value.indexOf(" ") != -1) {
						value = value.substring(1);
						if (value.endsWith("\"")) {
							value = value.substring(0, value.length() - 1);
						}
					}

					list.add(value);
					value = "";
					continue;
				}
			} else if (c == '"') {
				openQuote = !openQuote;
			}
			value += c;
		}
		list.add(value);
		return (String[]) list.toArray(new String[0]);
	}

	enum ColumnCategory
	{
		Primary, PrimaryRouting, Temporal, Value
	}

	class ColumnItem
	{
		private String name;
		private Class<?> type;
		private ColumnCategory category = ColumnCategory.Value;
		private boolean isRoutingKey;
		private int primaryKeyIndex = -1;

		public String getName()
		{
			return name;
		}

		public Class<?> getType()
		{
			return type;
		}

		public ColumnCategory getCategory()
		{
			return category;
		}

		public boolean isRoutingKey()
		{
			return isRoutingKey;
		}

		public int getPrimaryKeyIndex()
		{
			return primaryKeyIndex;
		}

		@Override
		public String toString()
		{
			return "ColumnItem [name=" + name + ", type=" + type + ", category=" + category + "]";
		}
	}
}