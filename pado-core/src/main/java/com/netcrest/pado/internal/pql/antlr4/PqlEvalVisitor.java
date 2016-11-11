package com.netcrest.pado.internal.pql.antlr4;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.core.IsAnything;

import com.netcrest.pado.internal.pql.antlr4.PathItem.JoinType;
import com.netcrest.pado.internal.pql.antlr4.generated.PqlBaseVisitor;
import com.netcrest.pado.internal.pql.antlr4.generated.PqlParser;

public class PqlEvalVisitor extends PqlBaseVisitor<Void>
{
	/**
	 * <alias, PathItem>
	 */
	Map<String, PathItem> pathAliasMap = new HashMap<String, PathItem>(10);

	private boolean isAggregation = true;

	public Void visitJoin_part(PqlParser.Join_partContext ctx)
	{
		PathItem pathItem = new PathItem();
		if (ctx.LEFT() != null) {
			pathItem.setType(JoinType.LEFT);
		} else if (ctx.RIGHT() != null) {
			pathItem.setType(JoinType.RIGHT);
		} else if (ctx.FULL() != null) {
			pathItem.setType(JoinType.FULL);
		}
		String pathAlias = null;
		try {
			pathAlias = ctx.path_source().path_source_item_joined().path_source_item().as_path_alias().getText();
			pathItem.setAlias(pathAlias);
			pathAliasMap.put(pathAlias, pathItem);
		} catch (NullPointerException ex) {
			
		}
		
		return visitChildren(ctx);
	}

	@Override
	public Void visitPath_source_item(PqlParser.Path_source_itemContext ctx)
	{
		String path = ctx.path_with_hint().path().getText();
		String pathAlias = ctx.as_path_alias().path_alias().id().simple_id().getText();
		setPath(pathAlias, path);
		return visitChildren(ctx);
	}

	@Override
	public Void visitComparison_operator(PqlParser.Comparison_operatorContext ctx)
	{
		String comparisonOperator = ctx.getText();
		if (comparisonOperator.equals(":") == false && comparisonOperator.equals("?") == false) {
			throw new RuntimeException(
					"Unsupported operator: " + comparisonOperator + ". This version supports only Lucene queries.");
		}
		return visitChildren(ctx);
	}

	@Override
	public Void visitPredicate(PqlParser.PredicateContext ctx)
	{
		PqlParser.ExpressionContext leftEc = ctx.expression(0);
		PqlParser.ExpressionContext rightEc = ctx.expression(1);
		String leftPathAlias = null;
		if (leftEc instanceof PqlParser.Column_ref_expressionContext) {
			PqlParser.Column_ref_expressionContext leftColumnRefEc = (PqlParser.Column_ref_expressionContext) leftEc;
			if (leftColumnRefEc.full_column_name().path() != null) {
				leftPathAlias = leftColumnRefEc.full_column_name().path().getText();
				if (isPath(leftPathAlias) == false) {
					throw new RuntimeException("Invalid path alias: " + leftPathAlias);
				}
			}
		}

		if (rightEc instanceof PqlParser.Column_ref_expressionContext) {
			PqlParser.Column_ref_expressionContext rightColumnRefEc = (PqlParser.Column_ref_expressionContext) rightEc;
			if (rightColumnRefEc.full_column_name().path() != null) {
				String rightPathAlias = rightColumnRefEc.full_column_name().path().getText();
				if (isPath(rightPathAlias) == false) {
					throw new RuntimeException("Invalid path alias: " + rightPathAlias);
				}
			}
		}
		return visitChildren(ctx);

	}

	public Void visitNest_by_type(PqlParser.Nest_by_typeContext ctx)
	{
		isAggregation = ctx.INHERITANCE() == null;
		return visitChildren(ctx);
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

	public Map<String, PathItem> getPathAliasMap()
	{
		return pathAliasMap;
	}

	public boolean isAggregation()
	{
		return isAggregation;
	}
}