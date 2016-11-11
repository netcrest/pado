// Generated from Pql.g4 by ANTLR 4.5.2
package com.netcrest.pado.internal.pql.antlr4.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PqlVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PqlParser#pql_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPql_file(PqlParser.Pql_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#sql_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_clause(PqlParser.Sql_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#dml_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDml_clause(PqlParser.Dml_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#ddl_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdl_clause(PqlParser.Ddl_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#select_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_statement(PqlParser.Select_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#create_vp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_vp(PqlParser.Create_vpContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#vp_attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVp_attribute(PqlParser.Vp_attributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#drop_path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_path(PqlParser.Drop_pathContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#drop_vp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_vp(PqlParser.Drop_vpContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitive_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitive_expression(PqlParser.Primitive_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subquery_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery_expression(PqlParser.Subquery_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bracket_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracket_expression(PqlParser.Bracket_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binary_operator_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinary_operator_expression(PqlParser.Binary_operator_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unary_operator_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary_operator_expression(PqlParser.Unary_operator_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code column_ref_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_ref_expression(PqlParser.Column_ref_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code function_call_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_call_expression(PqlParser.Function_call_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#constant_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant_expression(PqlParser.Constant_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(PqlParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#search_condition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_list(PqlParser.Search_condition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#search_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition(PqlParser.Search_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#search_condition_and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_and(PqlParser.Search_condition_andContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#search_condition_not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_not(PqlParser.Search_condition_notContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(PqlParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#query_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery_expression(PqlParser.Query_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#query_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery_specification(PqlParser.Query_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#order_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_clause(PqlParser.Order_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#order_by_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_expression(PqlParser.Order_by_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#group_by_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_item(PqlParser.Group_by_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#nest_by_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNest_by_type(PqlParser.Nest_by_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#select_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_list(PqlParser.Select_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#select_list_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_list_elem(PqlParser.Select_list_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#path_source}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_source(PqlParser.Path_sourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#path_source_item_joined}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_source_item_joined(PqlParser.Path_source_item_joinedContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#path_source_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_source_item(PqlParser.Path_source_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#join_part}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_part(PqlParser.Join_partContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#path_with_hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_with_hint(PqlParser.Path_with_hintContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#as_path_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_path_alias(PqlParser.As_path_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#path_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_alias(PqlParser.Path_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#to_path_hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTo_path_hint(PqlParser.To_path_hintContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#with_path_hints}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_path_hints(PqlParser.With_path_hintsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#path_hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_hint(PqlParser.Path_hintContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#column_alias_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alias_list(PqlParser.Column_alias_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#column_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alias(PqlParser.Column_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(PqlParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#path_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_name(PqlParser.Path_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath(PqlParser.PathContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#vp_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVp_name(PqlParser.Vp_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#vp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVp(PqlParser.VpContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#full_column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFull_column_name(PqlParser.Full_column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#column_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name_list(PqlParser.Column_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(PqlParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#null_notnull}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNull_notnull(PqlParser.Null_notnullContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#default_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_value(PqlParser.Default_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(PqlParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(PqlParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSign(PqlParser.SignContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(PqlParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#simple_id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_id(PqlParser.Simple_idContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(PqlParser.Comparison_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PqlParser#assignment_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment_operator(PqlParser.Assignment_operatorContext ctx);
}