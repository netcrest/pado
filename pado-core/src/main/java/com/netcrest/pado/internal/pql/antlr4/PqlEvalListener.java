package com.netcrest.pado.internal.pql.antlr4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.index.helper.BaseComparatorFactory;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.internal.pql.antlr4.PathItem.JoinType;
import com.netcrest.pado.internal.pql.antlr4.generated.PqlBaseListener;
import com.netcrest.pado.internal.pql.antlr4.generated.PqlParser;
import com.netcrest.pado.internal.pql.antlr4.generated.PqlParser.Order_by_expressionContext;
import com.netcrest.pado.internal.pql.antlr4.generated.PqlParser.To_path_hintContext;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PqlEvalListener extends PqlBaseListener
{

	/** maps nodes to string values with Map<ParseTree, String> */
	ParseTreeProperty<List> resultLists = new ParseTreeProperty<List>();
	/**
	 * <alias, PathItem>
	 */
	Map<String, PathItem> pathAliasMap = new HashMap<String, PathItem>(10);

	// <LSide pathAlias, results>
	Map<String, List> queryResultMap = new HashMap<String, List>(5);

	// Select projection set of <path alias, list of ColumnAliasPairs>
	Map<String, Set<ColumnAliasPair>> selectProjectionMap;

	// TO AGGREGATION and TO ONE column aliases <path alias, to column name>
	Map<String, ToInfo> toColumnNameMap = new HashMap<String, ToInfo>(5);

	private final static boolean DEBUG = PadoUtil.getBoolean("debug", false);

	private boolean isAggregation = true;
	private boolean isQueryEntries = false;

	private NodeFactory nodeFactory;

	private JoinType joinType = JoinType.INNER;

	private List results;

	public PqlEvalListener(Map<String, PathItem> pathAliasMap)
	{
		this(pathAliasMap, null, -1, -1, true, true);
	}

	public PqlEvalListener(Map<String, PathItem> pathAliasMap, ITemporalBizLink temporalBiz, long validAtTime,
			long asOfTime, boolean isQueryEntries, boolean isAggregation)
	{
		this.pathAliasMap = pathAliasMap;
		this.nodeFactory = new NodeFactory(this, isAggregation, temporalBiz, validAtTime, asOfTime);
		this.isQueryEntries = isQueryEntries;
		this.isAggregation = isAggregation;

	}

	@Override
	public void exitSelect_list_elem(PqlParser.Select_list_elemContext ctx)
	{

		PqlParser.ExpressionContext ec = ctx.expression();
		PqlParser.Column_aliasContext alc = ctx.column_alias();
		if (ec == null) {
			// All - * do nothing
		} else if (ec instanceof PqlParser.Column_ref_expressionContext) {
			if (selectProjectionMap == null) {
				selectProjectionMap = new HashMap<String, Set<ColumnAliasPair>>();
			}
			PqlParser.Column_ref_expressionContext columnRefEc = (PqlParser.Column_ref_expressionContext) ec;
			String pathAlias = columnRefEc.full_column_name().path().getText();
			if (isPath(pathAlias) == false) {
				throw new RuntimeException("Invalid path alias: " + pathAlias);
			}
			String columnName = columnRefEc.full_column_name().column_name().getText();
			String columnAlias = null;
			if (alc != null) {
				columnAlias = alc.getText();
			}
			Set<ColumnAliasPair> columnNameSet = selectProjectionMap.get(pathAlias);
			if (columnNameSet == null) {
				columnNameSet = new HashSet<ColumnAliasPair>();
				selectProjectionMap.put(pathAlias, columnNameSet);
			}
			columnNameSet.add(new ColumnAliasPair(columnName, columnAlias));
		}
	}

	@Override
	public void exitPath_source_item(PqlParser.Path_source_itemContext ctx)
	{
		String path = ctx.path_with_hint().path().getText();
		String pathAlias = ctx.as_path_alias().path_alias().id().simple_id().getText();
		setPath(pathAlias, path);

		// if (nodeFactory.rootNode == null) {
		// nodeFactory.rootNode = new NodeFactory.AliasNode(pathAlias);
		// }
	}

	/**
	 * Constructs ToInfos
	 */
	@Override
	public void enterPath_alias(PqlParser.Path_aliasContext ctx)
	{
		String pathAlias = ctx.id().getText();
		To_path_hintContext toPathHintCtx = ctx.to_path_hint();
		if (toPathHintCtx != null) {
			boolean isOne = toPathHintCtx.ONE() != null;
			String columnName;
			if (toPathHintCtx.column_alias() != null) {
				columnName = toPathHintCtx.column_alias().getText();
				toColumnNameMap.put(pathAlias, new ToInfo(columnName, isOne));
			}
		}
	}

	@Override
	public void enterJoin_part(PqlParser.Join_partContext ctx)
	{
		if (ctx.LEFT() != null) {
			joinType = JoinType.LEFT;
		} else if (ctx.RIGHT() != null) {
			joinType = JoinType.RIGHT;
		} else if (ctx.FULL() != null) {
			joinType = JoinType.FULL;
		} else {
			joinType = JoinType.INNER;
		}
	}

	class Predicate
	{
		boolean isLucene = true;
		String leftPathAlias;
		String leftPath;
		String leftColumnName;
		String operator;
		String rightPathAlias;
		String rightPath;
		String rightColumnName;
		String rightConstant;
		List rightResultList;

		@Override
		public String toString()
		{
			return "Predicate [isLucene=" + isLucene + ", leftPathAlias=" + leftPathAlias + ", leftPath=" + leftPath
					+ ", leftColumnName=" + leftColumnName + ", operator=" + operator + ", rightPathAlias="
					+ rightPathAlias + ", rightPath=" + rightPath + ", rightColumnName=" + rightColumnName
					+ ", rightConstant=" + rightConstant + "]";
		}

	}

	List<Predicate> predicateList = new ArrayList<Predicate>(5);

	@Override
	public void enterSearch_condition(PqlParser.Search_conditionContext ctx)
	{
		predicateList.clear();
		// if (ctx.search_condition_and() != null) {
		// List<Search_condition_andContext> list = ctx.search_condition_and();
		// for (Search_condition_andContext scac : list) {
		// List<Search_condition_notContext> list2 =
		// scac.search_condition_not();
		// for (Search_condition_notContext scnc : list2) {
		// PqlParser.Column_ref_expressionContext cre =
		// (PqlParser.Column_ref_expressionContext) scnc
		// .predicate().expression(0);
		// String leftPathAlias = cre.full_column_name().path().getText();
		// // System.out.println(leftPathAlias);
		// }
		// }
		// }
	}

	@Override
	public void exitSearch_condition(PqlParser.Search_conditionContext ctx)
	{
		// If nodeFactory is empty then it already made one or more passes
		// and found no entries. No need to continue searching. Immediately
		// return.
		if (nodeFactory.isEmpty()) {
			return;
		}

		String queryString = "";
		int i = 0;
		Predicate prevPredicate = null;
		for (Predicate predicate : predicateList) {
			boolean isSameQuery = false;
			if (prevPredicate != null && prevPredicate.rightConstant != null) {
				if (prevPredicate.leftPath.equals(predicate.leftPath)) {
					if (predicate.rightConstant != null) {
						isSameQuery = true;
					}
				}
			}

			if (isSameQuery) {
				// continue build queryString targeted the same path
				// TODO: Support OR
				queryString += " AND ";
			} else if (queryString.length() > 0) {
				executeQuery(prevPredicate, queryString);
				queryString = predicate.leftPath + "?";
			} else {
				queryString = predicate.leftPath + "?";
			}

			if (predicate.leftColumnName != null) {
				queryString += predicate.leftColumnName;
				if (predicate.operator != null) {
					queryString += predicate.operator;
				}
			}

			if (predicate.rightConstant != null) {
				if (predicate.isLucene) {
					if (predicate.rightConstant.startsWith("\"(")) {
						queryString += predicate.rightConstant.substring(1, predicate.rightConstant.length() - 1);
					} else if (predicate.rightConstant.startsWith("'")) {
						queryString += predicate.rightConstant.substring(1, predicate.rightConstant.length() - 1);
					} else {
						queryString += predicate.rightConstant;
					}

				} else {
					// TODO: OQL -- later
				}
			}

			prevPredicate = predicate;
			i++;
		}

		if (queryString.length() > 0) {
			executeQuery(prevPredicate, queryString);
		}
	}

	@Override
	public void enterPredicate(PqlParser.PredicateContext ctx)
	{
		enterPredicateValues(ctx);
	}

	// @Override
	// public void exitPredicate(PqlParser.PredicateContext ctx)
	// {
	// if (nodeFactory.isEmpty()) {
	// return;
	// }
	//
	// if (isQueryEntries) {
	// exitPredicateEntries(ctx);
	// } else {
	// exitPredicateValues(ctx);
	// }
	// }

	private void enterPredicateValues(PqlParser.PredicateContext ctx)
	{
		Predicate predicate = new Predicate();
		predicateList.add(predicate);

		PqlParser.ExpressionContext leftEc = ctx.expression(0);
		PqlParser.ExpressionContext rightEc = ctx.expression(1);
		predicate.operator = ctx.comparison_operator().getText();
		predicate.isLucene = predicate.operator.equals(":") || predicate.operator.equals("?");

		if (leftEc instanceof PqlParser.Column_ref_expressionContext) {
			PqlParser.Column_ref_expressionContext leftColumnRefEc = (PqlParser.Column_ref_expressionContext) leftEc;
			if (leftColumnRefEc.full_column_name().path() == null) {
				predicate.leftPathAlias = leftColumnRefEc.full_column_name().getText();
				predicate.leftPath = getPath(predicate.leftPathAlias);
			} else {
				predicate.leftPathAlias = leftColumnRefEc.full_column_name().path().getText();
				predicate.leftColumnName = leftColumnRefEc.full_column_name().column_name().getText();
				predicate.leftPath = getPath(predicate.leftPathAlias);

			}
		}

		if (rightEc instanceof PqlParser.Column_ref_expressionContext) {
			PqlParser.Column_ref_expressionContext rightColumnRefEc = (PqlParser.Column_ref_expressionContext) rightEc;
			if (rightColumnRefEc.full_column_name().path() == null) {
				predicate.rightConstant = rightColumnRefEc.full_column_name().getText();
			} else {
				predicate.rightPathAlias = rightColumnRefEc.full_column_name().path().getText();
				predicate.rightColumnName = rightColumnRefEc.full_column_name().column_name().getText();
				predicate.rightPath = getPath(predicate.rightPathAlias);
			}
		} else if (rightEc instanceof PqlParser.Primitive_expressionContext) {
			PqlParser.Primitive_expressionContext pContext = (PqlParser.Primitive_expressionContext) rightEc;
			PqlParser.ConstantContext constant = pContext.constant();
			predicate.rightConstant = constant.getText();
		}
	}

	/**
	 * Executes the specified predicate with the specified query.
	 * 
	 * @param predicate
	 *            Predicate
	 * @param leftQueryString
	 *            Query string that may be left hand side prefix or a complete
	 *            query.
	 */
	private void executeQuery(Predicate predicate, String queryString)
	{
		if (predicate.rightConstant != null) {
			executePredicateQuery(predicate, queryString);
		} else if (predicate.rightPath != null) {
			executePredicatePrefix(predicate, queryString);
		}
	}

	/**
	 * If Lucene, returns in the format of "("a" "b" "c")".
	 * 
	 * @param set
	 * @param isLucene
	 */
	private String buildQueryString(Set set, boolean isLucene)
	{
		String queryString = "";
		if (isLucene) {
			queryString += "(";
			int i = 0;
			for (Object obj : set) {
				if (obj != null) {
					if (i > 0) {
						queryString += " ";
					}
					queryString += "\"" + obj + "\"";
					i++;
				}
			}
			queryString += ")";
		} else {
			// OQL not supported at this time
			// predicate += "(";
			// int i = 0;
			// for (Object obj : set) {
			// if (i > 0) {
			// predicate += ",";
			// }
			// predicate += obj;
			// }
			// predicate += ")";
		}
		return queryString;
	}

	/**
	 * Executes the specified predicate with left hand side query prefix that
	 * may be used to determine the final query.
	 * 
	 * @param predicate
	 *            Predicate
	 * @param leftQueryPrefix
	 *            Left query prefix (partial query) without the right hand side,
	 *            which is to be determined in this method. Note that if the
	 *            search requires joining non-existent left results then this
	 *            prefix is ignored and a new one is built.
	 */
	private void executePredicatePrefix(Predicate predicate, String leftQueryPrefix)
	{
		String leftPathAlias = predicate.leftPathAlias;
		String leftPath = predicate.leftPath;
		String leftColumnName = predicate.leftColumnName;
		String rightPathAlias = predicate.rightPathAlias;
		String rightColumnName = predicate.rightColumnName;
		String rightPath = predicate.rightPath;
		String rightConstant = predicate.rightConstant;

		IScrollableResultSet<Map> leftScrollableResultSet = null;
		List<Map> leftList = null;

		// --------------- Right --------------
		IScrollableResultSet<Map> rightScrollableResultSet = null;
		List<Map> rightList = nodeFactory.getAliasResults(rightPathAlias);
		if (rightList == null) {

			// If left data exists then query the right side using
			// the left data.
			List<Map> leftList2 = nodeFactory.getAliasResults(leftPathAlias);
			if (leftList2 != null) {
				if (leftList2.size() > 0) {
					HashSet set = new HashSet(leftList2.size(), 1f);
					for (Map map : leftList2) {
						set.add(map.get(leftColumnName));
					}
					set.remove(null);
					if (set.size() == 0) {
						// This call effectively keeps if left join or removes
						// if inner join the left alias results.
						nodeFactory.joinEqualsExist(joinType, leftPathAlias, leftColumnName, rightPathAlias,
								rightColumnName);
						return;
					} else {
						String rightQueryString = rightPath + "?" + rightColumnName + predicate.operator
								+ buildQueryString(set, predicate.isLucene);
						if (PadoUtil.isPureClient()) {
							rightScrollableResultSet = nodeFactory.queryPredicateValuesScrolled(rightPathAlias,
									rightQueryString);
						} else {
							rightList = nodeFactory.queryPredicateValues(rightPathAlias, rightQueryString);
						}
					}
				}
			} else {

				// ------ Build the root node ----
				//
				// Neither left nor right results exist. This means we need to
				// create the root node results by gathering both side results.
				switch (joinType) {
				case RIGHT:
					String rightPredicate = rightPath + "?";
					// Execute the right side predicate
					if (PadoUtil.isPureClient()) {
						rightScrollableResultSet = nodeFactory.queryPredicateValuesScrolled(rightPathAlias,
								rightPredicate);
					} else {
						rightList = nodeFactory.queryPredicateValues(rightPathAlias, rightPredicate);
					}
					break;

				case FULL:
					// Not supported
					break;

				case LEFT:
				default:

					// Build query for both left and right.
					// If the right-side constant is not defined then this is
					// the most expensive routine. It must query all data from
					// left and right.
					String leftPredicate = leftPath + "?";
					if (rightConstant != null) {
						if (rightColumnName != null) {
							leftPredicate += leftColumnName + predicate.operator;
						}
						leftPredicate += rightConstant;
					}
					// Execute the left side predicate
					if (PadoUtil.isPureClient()) {
						leftScrollableResultSet = nodeFactory.queryPredicateValuesScrolled(leftPathAlias,
								leftPredicate);
					} else {
						leftList = nodeFactory.queryPredicateValues(leftPathAlias, leftPredicate);
					}

					// Query the right side
					if (rightConstant == null) {
						rightPredicate = rightPath + "?";
						if (PadoUtil.isPureClient()) {
							rightScrollableResultSet = nodeFactory.queryPredicateValuesScrolled(rightPathAlias,
									rightPredicate);
						} else {
							rightList = nodeFactory.queryPredicateValues(rightPathAlias, rightPredicate);
						}
					}
					if (rightList != null) {
						nodeFactory.joinRoot(joinType, leftPathAlias, leftColumnName, leftList, rightPathAlias,
								rightColumnName, rightList);
					} else if (rightScrollableResultSet != null) {
						nodeFactory.joinRoot(joinType, leftPathAlias, leftColumnName, leftScrollableResultSet,
								rightPathAlias, rightColumnName, rightScrollableResultSet);
					}
					return;
				}
			}
		}

		// --------------- Left --------------
		if (nodeFactory.isAliasExist(leftPathAlias) == false) {
			// LSide: Iterate the RSide results from the query above
			// to build the LSide query.
			HashSet set = null;
			if (rightList != null && rightList.size() > 0) {
				set = new HashSet(rightList.size(), 1f);
				for (Map map : rightList) {
					set.add(map.get(rightColumnName));
				}
				set.remove(null);
			} else if (rightScrollableResultSet != null && rightScrollableResultSet.getTotalSize() > 0) {
				set = new HashSet();
				do {
					List<Map> list = rightScrollableResultSet.toList();
					if (list.size() > 0) {
						for (Map map : list) {
							Object obj = map.get(rightColumnName);
							set.add(obj);
						}
					}
				} while (rightScrollableResultSet.nextSet());
				set.remove(null);
			} else {
				// This occurs if LEFT join with no right results or FULL join
				// which is not supported
				// TODO: error handle FULL join

			}
			if (set == null || set.size() == 0) {
				// This call effectively keeps if left join, or removes if inner
				// join the left alias results.
				nodeFactory.joinEqualsExist(joinType, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName);
				return;
			}

			// leftQueryString = leftPath + "?";
			// if (leftColumnName != null) {
			// leftQueryString += leftColumnName + predicate.operator;
			// }
			leftQueryPrefix += buildQueryString(set, predicate.isLucene);

			if (PadoUtil.isPureClient()) {
				leftScrollableResultSet = nodeFactory.queryPredicateValuesScrolled(leftPathAlias, leftQueryPrefix);
				if (DEBUG) {
					System.out.println("Left: " + predicate);
					do {
						List list = leftScrollableResultSet.toList();
						for (Object object : list) {
							System.out.println("   " + object);
						}
					} while (leftScrollableResultSet.nextSet());
				}
			} else {
				leftList = nodeFactory.queryPredicateValues(leftPathAlias, leftQueryPrefix);
				if (DEBUG) {
					System.out.println("Left: " + predicate);
					for (Object object : leftList) {
						System.out.println("   " + object);
					}
				}
			}

			if (nodeFactory.isRootExist()) {
				if (leftList != null) {
					nodeFactory.joinEqualsLeftResults(joinType, leftPathAlias, leftColumnName, rightPathAlias,
							rightColumnName, leftList);
				} else {
					nodeFactory.joinEqualsLeftResults(joinType, leftPathAlias, leftColumnName, rightPathAlias,
							rightColumnName, leftScrollableResultSet);
				}
				if (DEBUG) {
					nodeFactory.printRoot();
				}
				return;
			}
		}

		// if (nodeFactory.isRootExist() == false) {
		// if (rightList != null) {
		// nodeFactory.joinRoot(joinType, leftPathAlias, leftColumnName,
		// leftList, rightPathAlias, rightColumnName,
		// rightList);
		// } else if (rightScrollableResultSet != null) {
		// nodeFactory.joinRoot(joinType, leftPathAlias, leftColumnName,
		// leftScrollableResultSet, rightPathAlias,
		// rightColumnName, rightScrollableResultSet);
		// }
		// nodeFactory.printRoot();
		// } else

		if (nodeFactory.rootNode.exists(leftPathAlias) && nodeFactory.rootNode.exists(rightPathAlias)) {
			nodeFactory.joinEqualsExist(joinType, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName);
			if (DEBUG) {
				nodeFactory.printRoot();
			}
		}

	}

	/**
	 * Executes the specified predicate with the specified query. The query must
	 * be a complete query and must not be partial or a prefix.
	 * 
	 * @param predicate
	 *            Predicate
	 * @param queryString
	 *            Complete query to execute
	 */
	private void executePredicateQuery(Predicate predicate, String queryString)
	{
		// Execute query with the constant
		if (nodeFactory.isAliasExist(predicate.leftPathAlias) == false) {
			if (PadoUtil.isPureClient()) {
				IScrollableResultSet leftScrollableResultSet = nodeFactory
						.queryPredicateValuesScrolled(predicate.leftPathAlias, queryString);
				if (nodeFactory.isRootExist() == false) {
					nodeFactory.joinRoot(joinType, predicate.leftPathAlias, leftScrollableResultSet);
				} else {
					// These results are independent of the existing left
					// results. Join them by the its column name.
					nodeFactory.joinEqualsLeftResults(joinType, predicate.leftPathAlias, predicate.leftColumnName,
							predicate.leftPathAlias, predicate.leftColumnName, leftScrollableResultSet);
				}
			} else {
				List<Map> leftList = nodeFactory.queryPredicateValues(predicate.leftPathAlias, queryString);
				if (nodeFactory.isRootExist() == false) {
					nodeFactory.joinRoot(joinType, predicate.leftPathAlias, leftList);
				} else {
					// These results are independent of the existing left
					// results.
					// Join them by the its column name.
					nodeFactory.joinEqualsLeftResults(joinType, predicate.leftPathAlias, predicate.leftColumnName,
							predicate.leftPathAlias, predicate.leftColumnName, leftList);
				}
			}
		} else {
			nodeFactory.joinEqualsConstant(joinType, predicate.leftPathAlias, predicate.leftColumnName,
					predicate.rightConstant);
		}
	}

	/**
	 * This is an old method that handled the entire mechanics of querying and
	 * joining results.
	 * 
	 * @param ctx
	 * @deprecated
	 */
	private void exitPredicateValues(PqlParser.PredicateContext ctx)
	{
		PqlParser.ExpressionContext leftEc = ctx.expression(0);
		PqlParser.ExpressionContext rightEc = ctx.expression(1);
		String comparisonOperator = ctx.comparison_operator().getText();
		boolean isLucene = comparisonOperator.equals(":") || comparisonOperator.equals("?");
		String predicate = "";
		String leftPathAlias = null;
		String leftColumnName = null;
		List<Map> leftList = null;
		IScrollableResultSet<Map> leftScrollableResultSet = null;
		if (leftEc instanceof PqlParser.Column_ref_expressionContext) {
			PqlParser.Column_ref_expressionContext leftColumnRefEc = (PqlParser.Column_ref_expressionContext) leftEc;
			if (leftColumnRefEc.full_column_name().path() == null) {
				leftPathAlias = leftColumnRefEc.full_column_name().getText();
				String leftPath = getPath(leftPathAlias);
				predicate = leftPath;
			} else {
				leftPathAlias = leftColumnRefEc.full_column_name().path().getText();
				leftColumnName = leftColumnRefEc.full_column_name().column_name().getText();

				String leftPath = getPath(leftPathAlias);
				predicate = leftPath + "?" + leftColumnName;
			}
			predicate += comparisonOperator;
		}

		String luceneLiteral = null;

		if (rightEc instanceof PqlParser.Column_ref_expressionContext) {
			PqlParser.Column_ref_expressionContext rightColumnRefEc = (PqlParser.Column_ref_expressionContext) rightEc;
			if (rightColumnRefEc.full_column_name().path() == null) {
				luceneLiteral = rightColumnRefEc.full_column_name().getText();
			} else {
				String rightPathAlias = rightColumnRefEc.full_column_name().path().getText();
				String rightColumnName = rightColumnRefEc.full_column_name().column_name().getText();
				String rightPath = getPath(rightPathAlias);

				// --------------- Right --------------
				IScrollableResultSet<Map> rightScrollableResultSets = null;
				List<Map> rightList = nodeFactory.getAliasResults(rightPathAlias);
				if (rightList == null) {

					// Get all from the right side
					String rightPredicate = rightPath + "?";

					// Execute the right side predicate
					if (PadoUtil.isPureClient()) {
						rightScrollableResultSets = nodeFactory.queryPredicateValuesScrolled(rightPathAlias,
								rightPredicate);
						if (DEBUG) {
							System.out.println("Right: " + rightPredicate);
							do {
								List list = rightScrollableResultSets.toList();
								for (Object object : list) {
									System.out.println("   " + object);
								}
							} while (rightScrollableResultSets.nextSet());
						}
					} else {
						rightList = nodeFactory.queryPredicateValues(rightPathAlias, rightPredicate);
						if (DEBUG) {
							System.out.println("Right: " + rightPredicate);
							for (Object object : rightList) {
								System.out.println("   " + object);
							}
						}
					}
				}

				// --------------- Left --------------
				if (nodeFactory.isAliasExist(leftPathAlias) == false) {
					// LSide: Iterate the RSide results from the query above
					// to build the LSide query.
					if (joinType == JoinType.LEFT) {
						predicate = getPath(leftPathAlias) + "?";
					} else {
						if (rightList != null) {
							HashSet set = new HashSet(rightList.size(), 1f);
							for (Map map : rightList) {
								set.add(map.get(rightColumnName));
							}
							set.remove(null);
							if (isLucene) {
								predicate += "(";
								int i = 0;
								for (Object obj : set) {
									if (obj != null) {
										if (i > 0) {
											predicate += " ";
										}
										predicate += "\"" + obj + "\"";
										i++;
									}
								}
								predicate += ")";
							} else {
								// OQL not supported at this time
								// predicate += "(";
								// int i = 0;
								// for (Object obj : set) {
								// if (i > 0) {
								// predicate += ",";
								// }
								// predicate += obj;
								// }
								// predicate += ")";
							}
						} else {
							int i = 0;
							predicate += "(";
							do {
								List<Map> list = rightScrollableResultSets.toList();
								if (list.size() > 0) {
									HashSet set = new HashSet(list.size(), 1f);
									for (Map map : list) {
										Object obj = map.get(rightColumnName);
										set.add(obj);
									}
									for (Object obj : set) {
										if (obj != null) {
											if (i > 0) {
												predicate += " ";
											}
											predicate += "\"" + obj + "\" ";
											i++;
										}
									}
								}
							} while (rightScrollableResultSets.nextSet());
							predicate += ")";
						}
					}

					if (PadoUtil.isPureClient()) {
						leftScrollableResultSet = nodeFactory.queryPredicateValuesScrolled(leftPathAlias, predicate);
						if (DEBUG) {
							System.out.println("Left: " + predicate);
							do {
								List list = leftScrollableResultSet.toList();
								for (Object object : list) {
									System.out.println("   " + object);
								}
							} while (leftScrollableResultSet.nextSet());
						}
					} else {
						leftList = nodeFactory.queryPredicateValues(leftPathAlias, predicate);
						if (DEBUG) {
							System.out.println("Left: " + predicate);
							for (Object object : leftList) {
								System.out.println("   " + object);
							}
						}
					}

					if (nodeFactory.isRootExist()) {
						if (leftList != null) {
							nodeFactory.joinEqualsLeftResults(joinType, leftPathAlias, leftColumnName, rightPathAlias,
									rightColumnName, leftList);
						} else {
							nodeFactory.joinEqualsLeftResults(joinType, leftPathAlias, leftColumnName, rightPathAlias,
									rightColumnName, leftScrollableResultSet);
						}
						if (DEBUG) {
							nodeFactory.printRoot();
						}
						return;
					}
				}

				if (nodeFactory.isRootExist() == false) {
					if (rightList != null) {
						nodeFactory.joinRoot(joinType, leftPathAlias, leftColumnName, leftList, rightPathAlias,
								rightColumnName, rightList);
					} else if (rightScrollableResultSets != null) {
						nodeFactory.joinRoot(joinType, leftPathAlias, leftColumnName, leftScrollableResultSet,
								rightPathAlias, rightColumnName, rightScrollableResultSets);
					}
					nodeFactory.printRoot();
				} else if (nodeFactory.rootNode.exists(leftPathAlias) && nodeFactory.rootNode.exists(rightPathAlias)) {
					nodeFactory.joinEqualsExist(joinType, leftPathAlias, leftColumnName, rightPathAlias,
							rightColumnName);
					if (DEBUG) {
						nodeFactory.printRoot();
					}
				}
			}

		}

		if (luceneLiteral != null || rightEc instanceof PqlParser.Primitive_expressionContext) {
			Object typedValue = null;
			if (luceneLiteral != null) {
				typedValue = luceneLiteral;
				if (luceneLiteral.startsWith("\"(")) {
					predicate += luceneLiteral.substring(1, luceneLiteral.length() - 1);
				} else {
					predicate += luceneLiteral;
				}
			} else {
				PqlParser.Primitive_expressionContext pContext = (PqlParser.Primitive_expressionContext) rightEc;
				PqlParser.ConstantContext constant = pContext.constant();
				String value = constant.getText();

				if (isLucene) {
					if (value.startsWith("'")) {
						typedValue = value.substring(1, value.length() - 1);
					} else {
						typedValue = value;
					}
				} else {
					if (value.startsWith("'") == false) {
						typedValue = Integer.parseInt(value);
					}
				}
				predicate += typedValue;
			}

			// Execute query with the constant
			if (nodeFactory.isAliasExist(leftPathAlias) == false) {
				if (PadoUtil.isPureClient()) {
					leftScrollableResultSet = nodeFactory.queryPredicateValuesScrolled(leftPathAlias, predicate);
					if (nodeFactory.isRootExist() == false) {
						nodeFactory.joinRoot(joinType, leftPathAlias, leftScrollableResultSet);
					} else {
						// These results are independent of the existing left
						// results.
						// Join them by the its column name.
						nodeFactory.joinEqualsLeftResults(joinType, leftPathAlias, leftColumnName, leftPathAlias,
								leftColumnName, leftScrollableResultSet);
					}
				} else {
					leftList = nodeFactory.queryPredicateValues(leftPathAlias, predicate);
					if (nodeFactory.isRootExist() == false) {
						nodeFactory.joinRoot(joinType, leftPathAlias, leftList);
					} else {
						// These results are independent of the existing left
						// results.
						// Join them by the its column name.
						nodeFactory.joinEqualsLeftResults(joinType, leftPathAlias, leftColumnName, leftPathAlias,
								leftColumnName, leftList);
					}
				}
			} else {
				nodeFactory.joinEqualsConstant(joinType, leftPathAlias, leftColumnName, typedValue);
			}
			if (DEBUG) {
				System.out.println(predicate);
				nodeFactory.printRoot();
			}
		}
	}

	private void exitPredicateEntries(PqlParser.PredicateContext ctx)
	{
		PqlParser.ExpressionContext leftEc = ctx.expression(0);
		PqlParser.ExpressionContext rightEc = ctx.expression(1);
		String comparisonOperator = ctx.comparison_operator().getText();
		boolean isLucene = comparisonOperator.equals(":");
		String predicate = "";
		String leftPathAlias = null;
		String leftColumnName = null;
		List<TemporalEntry<ITemporalKey, ITemporalData>> leftList = null;
		IScrollableResultSet<TemporalEntry<ITemporalKey, ITemporalData>> leftScrollableResultSet = null;
		if (leftEc instanceof PqlParser.Column_ref_expressionContext) {
			PqlParser.Column_ref_expressionContext leftColumnRefEc = (PqlParser.Column_ref_expressionContext) leftEc;
			if (leftColumnRefEc.full_column_name().path() == null) {
				leftPathAlias = leftColumnRefEc.full_column_name().getText();
				String leftPath = getPath(leftPathAlias);
				predicate = leftPath;
			} else {
				leftPathAlias = leftColumnRefEc.full_column_name().path().getText();
				leftColumnName = leftColumnRefEc.full_column_name().column_name().getText();

				String leftPath = getPath(leftPathAlias);
				predicate = leftPath + "?" + leftColumnName;
			}
			predicate += comparisonOperator;
		}

		String luceneLiteral = null;

		if (rightEc instanceof PqlParser.Column_ref_expressionContext) {
			PqlParser.Column_ref_expressionContext rightColumnRefEc = (PqlParser.Column_ref_expressionContext) rightEc;
			if (rightColumnRefEc.full_column_name().path() == null) {
				luceneLiteral = rightColumnRefEc.full_column_name().getText();
			} else {
				String rightPathAlias = rightColumnRefEc.full_column_name().path().getText();
				String rightColumnName = rightColumnRefEc.full_column_name().column_name().getText();
				String rightPath = getPath(rightPathAlias);

				// --------------- Right --------------
				IScrollableResultSet<TemporalEntry<ITemporalKey, ITemporalData>> rightScrollableResultSets = null;
				List<TemporalEntry<ITemporalKey, ITemporalData>> rightList = nodeFactory
						.getAliasResults(rightPathAlias);
				if (rightList == null) {

					// Get all from the right side
					String rightPredicate = rightPath + "?";

					// Execute the right side predicate
					if (PadoUtil.isPureClient()) {
						rightScrollableResultSets = nodeFactory.queryPredicateEntriesScrolled(rightPathAlias,
								rightPredicate);
						if (DEBUG) {
							System.out.println("Right: " + rightPredicate);
							do {
								List list = rightScrollableResultSets.toList();
								for (Object object : list) {
									System.out.println("   " + object);
								}
							} while (rightScrollableResultSets.nextSet());
						}
					} else {
						rightList = nodeFactory.queryPredicateEntries(rightPathAlias, rightPredicate);
						if (DEBUG) {
							System.out.println("Right: " + rightPredicate);
							for (Object object : rightList) {
								System.out.println("   " + object);
							}
						}
					}
				}

				// --------------- Left --------------
				if (nodeFactory.isAliasExist(leftPathAlias) == false) {
					// LSide: Iterate the RSide results from the query above
					// to build the LSide query.
					if (rightList != null) {
						HashSet set = new HashSet(rightList.size(), 1f);
						for (TemporalEntry te : rightList) {
							Map map = (Map) te.getTemporalData().getValue();
							set.add(map.get(rightColumnName));
						}
						if (isLucene) {
							predicate += "(";
							int i = 0;
							for (Object obj : set) {
								if (i > 0) {
									predicate += " ";
								}
								predicate += "\"" + obj + "\"";
								i++;
							}
							predicate += ")";
						} else {
							// OQL not supported at this time
							// predicate += "(";
							// int i = 0;
							// for (Object obj : set) {
							// if (i > 0) {
							// predicate += ",";
							// }
							// predicate += obj;
							// }
							// predicate += ")";
						}
					} else {
						predicate += "(";
						do {
							List<TemporalEntry<ITemporalKey, ITemporalData>> list = rightScrollableResultSets.toList();
							if (list.size() > 0) {

								for (TemporalEntry<ITemporalKey, ITemporalData> te : list) {
									Map map = (Map) te.getTemporalData().getValue();
									Object obj = map.get(rightColumnName);
									if (obj != null) {
										predicate += "\"" + obj + "\" ";
									}
								}
							}
						} while (rightScrollableResultSets.nextSet());
						predicate += ")";
					}

					if (PadoUtil.isPureClient()) {
						leftScrollableResultSet = nodeFactory.queryPredicateEntriesScrolled(leftPathAlias, predicate);
						if (DEBUG) {
							System.out.println("Left: " + predicate);
							do {
								List list = leftScrollableResultSet.toList();
								for (Object object : list) {
									System.out.println("   " + object);
								}
							} while (leftScrollableResultSet.nextSet());
						}
					} else {
						leftList = nodeFactory.queryPredicateEntries(leftPathAlias, predicate);
						if (DEBUG) {
							System.out.println("Left: " + predicate);
							for (Object object : leftList) {
								System.out.println("   " + object);
							}
						}
					}

					if (nodeFactory.rootNode.valueNodeList != null) {
						if (leftList != null) {
							nodeFactory.joinEqualsLeftEntries(joinType, leftPathAlias, leftColumnName, rightPathAlias,
									rightColumnName, leftList);
						} else {
							nodeFactory.joinEqualsLeftEntries(joinType, leftPathAlias, leftColumnName, rightPathAlias,
									rightColumnName, leftScrollableResultSet);
						}
						if (DEBUG) {
							nodeFactory.printRoot();
						}
						return;
					}
				}

				if (nodeFactory.isRootExist() == false) {
					if (rightList != null) {
						nodeFactory.joinRootEntries(joinType, leftPathAlias, leftColumnName, leftList, rightPathAlias,
								rightColumnName, rightList);
					} else if (rightScrollableResultSets != null) {
						nodeFactory.joinRootEntries(joinType, leftPathAlias, leftColumnName, leftList, rightPathAlias,
								rightColumnName, rightScrollableResultSets);
					}
					nodeFactory.printRoot();
				} else if (nodeFactory.rootNode.exists(leftPathAlias) && nodeFactory.rootNode.exists(rightPathAlias)) {
					nodeFactory.joinEqualsExist(joinType, leftPathAlias, leftColumnName, rightPathAlias,
							rightColumnName);
					if (DEBUG) {
						nodeFactory.printRoot();
					}
				}
			}

		}

		if (luceneLiteral != null || rightEc instanceof PqlParser.Primitive_expressionContext) {
			Object typedValue = null;
			if (luceneLiteral != null) {
				typedValue = luceneLiteral;
				if (luceneLiteral.startsWith("\"(")) {
					predicate += luceneLiteral.substring(1, luceneLiteral.length() - 1);
				} else {
					predicate += luceneLiteral;
				}
			} else {
				PqlParser.Primitive_expressionContext pContext = (PqlParser.Primitive_expressionContext) rightEc;
				PqlParser.ConstantContext constant = pContext.constant();
				String value = constant.getText();

				if (isLucene == false) {
					if (value.startsWith("'")) {
						typedValue = value.substring(1, value.length() - 1);
					} else {
						typedValue = Integer.parseInt(value);
					}
				} else {
					typedValue = value;
				}
				predicate += constant.getText();
			}

			// Execute query with the constant
			if (nodeFactory.isAliasExist(leftPathAlias) == false) {
				if (PadoUtil.isPureClient()) {
					leftScrollableResultSet = nodeFactory.queryPredicateEntriesScrolled(leftPathAlias, predicate);
					if (nodeFactory.isRootExist() == false) {
						nodeFactory.joinRootEntries(joinType, leftPathAlias, leftScrollableResultSet);
					}
					// else {
					// nodeFactory.joinEqualsLeftResults(leftPathAlias,
					// leftColumnName, leftList);
					// }
				} else {
					leftList = nodeFactory.queryPredicateEntries(leftPathAlias, predicate);
					if (nodeFactory.isRootExist() == false) {
						nodeFactory.joinRootEntries(leftPathAlias, leftList);
					}
				}
			} else {
				nodeFactory.joinEqualsConstantEntries(joinType, leftPathAlias, leftColumnName, typedValue);
			}
			if (DEBUG) {
				System.out.println(predicate);
				nodeFactory.printRoot();
			}
		}
	}

	@Override
	public void exitOrder_by_clause(PqlParser.Order_by_clauseContext ctx)
	{
		List results = getResults();
		List<Order_by_expressionContext> orderByList = ctx.order_by_expression();
		if (orderByList != null) {
			for (Order_by_expressionContext obec : orderByList) {
				boolean sortAsc = obec.DESC() == null;
				String columnName = obec.expression().getText();
				results = orderBy(results, columnName, sortAsc);

				// TODO: Support multi columns
				break;
			}
		}
	}

	private List orderBy(List results, String columnName, boolean sortAsc)
	{
		Object object = getNonNullValueObject(results, columnName);
		if (object != null) {
			BaseComparatorFactory cf = new BaseComparatorFactory();
			Comparator comparator = cf.getComparator(object, columnName, sortAsc, false /* sortKey */);
			if (comparator != null) {
				Collections.sort(results, comparator);
			}
		}
		return results;
	}

	/**
	 * Returns the first non-null value for the specified field anem found in
	 * the specified list. A on-null value is required in order to determine the
	 * return type when comparing values for "order by". This method is for Map
	 * objects that are dynamic in nature in that the their values must be
	 * scanned in order to determine the field type.
	 * 
	 * @param list
	 *            List containing data objects to scan for non-null valued
	 *            objects.
	 * @param fieldName
	 *            Field name
	 * @return null if list is empty or all values in the list objects are null.
	 */
	protected Object getNonNullValueObject(List list, String fieldName)
	{
		if (fieldName == null) {
			return null;
		}
		Object retObj = null;
		if (list.size() > 0) {
			retObj = list.get(0);
		}
		if (retObj == null) {
			return null;
		}

		if (retObj instanceof Map) {
			for (Object object : list) {
				Object fieldValue = ((Map) object).get(fieldName);
				if (fieldValue != null) {
					retObj = object;
					break;
				}
			}
		}
		return retObj;
	}

	public void setValue(ParseTree node, List list)
	{
		resultLists.put(node, list);
	}

	public List getValue(ParseTree node)
	{
		return resultLists.get(node);
	}

	public void setPath(String pathAlias, String path)
	{
		PathItem pathItem = pathAliasMap.get(pathAlias);
		if (pathItem == null) {
			pathItem = new PathItem();
			pathItem.setPath(path);
			pathAliasMap.put(pathAlias, pathItem);
		} else {
			pathItem.setPath(path);
		}
	}

	public String getPath(String pathAlias)
	{
		PathItem pathItem = pathAliasMap.get(pathAlias);
		if (pathItem == null) {
			return pathAlias;
		} else {
			return pathItem.getPath();
		}
	}

	public boolean isPath(String pathAlias)
	{
		return pathAliasMap.containsKey(pathAlias);
	}

	public JoinType getJoinType(String pathAlias)
	{
		PathItem pathItem = pathAliasMap.get(pathAlias);
		if (pathItem == null) {
			return JoinType.INNER;
		} else {
			return pathItem.getJoinType();
		}
	}

	public List getResults()
	{
		if (results != null) {
			return results;
		}
		results = getResults(nodeFactory.rootNode);
		return results;
	}

	private List<Map> getResults(NodeFactory.AliasNode aliasNode)
	{
		if (selectProjectionMap != null) {
			return getResultsSelectProjection(aliasNode);
		} else if (isAggregation) {
			return getResultsAggregation(aliasNode);
		} else {
			return getResultsInheritance(aliasNode);
		}
	}

	private List<Map> getResultsAggregation(NodeFactory.AliasNode aliasNode)
	{
		List<Map> resultMapList = new ArrayList();
		// All - *
		if (aliasNode.valueNodeList != null) {
			for (NodeFactory.ValueNode valueNode : aliasNode.valueNodeList) {
				Map resultMap = new JsonLite((Map) valueNode.value);
				if (valueNode.aliasNodeList != null) {
					for (NodeFactory.AliasNode an : valueNode.aliasNodeList) {
						List<Map> list = getResults(an);
						ToInfo toInfo = getToInfo(an.alias);
						if (toInfo == null) {
							resultMap.put(an.alias, list);
						} else {
							String columnName;
							if (toInfo.columnName != null && toInfo.columnName.length() > 0) {
								columnName = toInfo.columnName;
							} else {
								columnName = an.alias;
							}
							if (toInfo.isOne) {
								if (list.size() > 0) {
									resultMap.put(columnName, list.get(0));
								} else {
									resultMap.put(columnName, list);
								}
							} else {
								resultMap.put(columnName, list);
							}
						}
						resultMapList.add(resultMap);
					}
				} else {
					resultMapList.add(resultMap);
				}
			}
		}
		return resultMapList;
	}

	private List<Map> getResultsInheritance(NodeFactory.AliasNode aliasNode)
	{
		List<Map> resultMapList = new ArrayList();
		// All - *
		if (aliasNode.valueNodeList != null) {
			for (NodeFactory.ValueNode valueNode : aliasNode.valueNodeList) {
				Map resultMap = new JsonLite((Map) valueNode.value);
				if (valueNode.aliasNodeList != null) {
					for (NodeFactory.AliasNode an : valueNode.aliasNodeList) {
						List<Map> list = getResults(an);
						if (list.size() == 1) {
							resultMap.putAll(list.get(0));
							resultMapList.add(resultMap);
						} else {
							for (Map anMap : list) {
								Map rm2 = new JsonLite(resultMap);
								rm2.putAll(anMap);
								resultMapList.add(rm2);
							}
						}
					}
				} else {
					resultMapList.add(resultMap);
				}
			}
		}
		return resultMapList;
	}

	private List<Map> getResultsSelectProjection(NodeFactory.AliasNode aliasNode)
	{
		List<Map> resultMapList = new ArrayList();
		Set<ColumnAliasPair> columnAliasPairSet = selectProjectionMap.get(aliasNode.alias);
		if (aliasNode.valueNodeList != null && columnAliasPairSet != null) {
			for (NodeFactory.ValueNode valueNode : aliasNode.valueNodeList) {
				Map resultMap = new JsonLite();
				for (ColumnAliasPair pair : columnAliasPairSet) {
					Map map = (Map) valueNode.value;
					if (pair.columnAlias != null) {
						resultMap.put(pair.columnAlias, map.get(pair.columnName));
					} else {
						resultMap.put(pair.columnName, map.get(pair.columnName));
					}
				}
				if (valueNode.aliasNodeList != null) {
					for (NodeFactory.AliasNode an : valueNode.aliasNodeList) {
						List<Map> list = getResults(an);
						if (list.size() == 1) {
							resultMap.putAll(list.get(0));
							resultMapList.add(resultMap);
						} else {
							for (Map anMap : list) {
								Map rm2 = new JsonLite(resultMap);
								rm2.putAll(anMap);
								resultMapList.add(rm2);
							}
						}
					}
				} else {
					resultMapList.add(resultMap);
				}
			}
		}

		return resultMapList;
	}

	// private List<TemporalEntry<ITemporalKey, ITemporalData>>
	// getResultsEntries(NodeFactory.AliasNode aliasNode)
	// {
	// List<TemporalEntry<ITemporalKey, ITemporalData>> resultMapList = new
	// ArrayList<TemporalEntry<ITemporalKey, ITemporalData>>();
	// if (selectProjectionMap == null) {
	// // All - *
	// if (aliasNode.valueNodeList != null) {
	// for (NodeFactory.ValueNode valueNode : aliasNode.valueNodeList) {
	// TemporalEntry<ITemporalKey, ITemporalData> te =
	// (TemporalEntry<ITemporalKey, ITemporalData>)valueNode.value;
	// Map resultMap = (Map)te.getTemporalData().getValue();
	// if (valueNode.aliasNodeList != null) {
	// for (NodeFactory.AliasNode an : valueNode.aliasNodeList) {
	// List<TemporalEntry<ITemporalKey, ITemporalData>> list =
	// getResultsEntries(an);
	// if (list.size() == 1) {
	// resultMap.putAll(list.get(0));
	// resultMapList.add(te);
	// } else {
	// for (Map anMap : list) {
	// Map rm2 = new JsonLite(resultMap);
	// rm2.putAll(anMap);
	// resultMapList.add(rm2);
	// }
	// }
	// }
	// } else {
	// resultMapList.add(resultMap);
	// }
	// }
	// }
	//
	// } else {
	// Set<String> columnNameSet = selectProjectionMap.get(aliasNode.alias);
	// if (aliasNode.valueNodeList != null) {
	// for (NodeFactory.ValueNode valueNode : aliasNode.valueNodeList) {
	// Map resultMap = new JsonLite();
	// for (String columnName : columnNameSet) {
	// Map map = (Map) valueNode.value;
	// resultMap.put(columnName, map.get(columnName));
	// }
	// if (valueNode.aliasNodeList != null) {
	// for (NodeFactory.AliasNode an : valueNode.aliasNodeList) {
	// List<Map> list = getResults(an);
	// if (list.size() == 1) {
	// resultMap.putAll(list.get(0));
	// resultMapList.add(resultMap);
	// } else {
	// for (Map anMap : list) {
	// Map rm2 = new JsonLite(resultMap);
	// rm2.putAll(anMap);
	// resultMapList.add(rm2);
	// }
	// }
	// }
	// } else {
	// resultMapList.add(resultMap);
	// }
	// }
	// }
	// }
	//
	// return resultMapList;
	// }

	ToInfo getToInfo(String pathAlias)
	{
		return toColumnNameMap.get(pathAlias);
	}

	String getToColumnName(String pathAlias)
	{
		ToInfo toInfo = getToInfo(pathAlias);
		if (toInfo == null) {
			return pathAlias;
		} else {
			return toInfo.columnName;
		}
	}

	class ColumnAliasPair
	{
		ColumnAliasPair(String columnName, String columnAlias)
		{
			this.columnName = columnName;
			this.columnAlias = columnAlias;
		}

		String columnName;
		String columnAlias;
	}

	class ToInfo
	{
		ToInfo(String columnName, boolean isOne)
		{
			this.columnName = columnName;
			this.isOne = isOne;
		}

		String columnName;

		/**
		 * true if one-to-one, false, if one-to-many. Default: false, i.e.,
		 * one-to-many
		 */
		boolean isOne;
	}

}