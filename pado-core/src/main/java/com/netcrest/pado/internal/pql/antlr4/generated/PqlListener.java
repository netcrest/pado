// Generated from Pql.g4 by ANTLR 4.5.2
package com.netcrest.pado.internal.pql.antlr4.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PqlParser}.
 */
public interface PqlListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PqlParser#pql_file}.
	 * @param ctx the parse tree
	 */
	void enterPql_file(PqlParser.Pql_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#pql_file}.
	 * @param ctx the parse tree
	 */
	void exitPql_file(PqlParser.Pql_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#sql_clause}.
	 * @param ctx the parse tree
	 */
	void enterSql_clause(PqlParser.Sql_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#sql_clause}.
	 * @param ctx the parse tree
	 */
	void exitSql_clause(PqlParser.Sql_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#dml_clause}.
	 * @param ctx the parse tree
	 */
	void enterDml_clause(PqlParser.Dml_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#dml_clause}.
	 * @param ctx the parse tree
	 */
	void exitDml_clause(PqlParser.Dml_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#ddl_clause}.
	 * @param ctx the parse tree
	 */
	void enterDdl_clause(PqlParser.Ddl_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#ddl_clause}.
	 * @param ctx the parse tree
	 */
	void exitDdl_clause(PqlParser.Ddl_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#select_statement}.
	 * @param ctx the parse tree
	 */
	void enterSelect_statement(PqlParser.Select_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#select_statement}.
	 * @param ctx the parse tree
	 */
	void exitSelect_statement(PqlParser.Select_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#create_vp}.
	 * @param ctx the parse tree
	 */
	void enterCreate_vp(PqlParser.Create_vpContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#create_vp}.
	 * @param ctx the parse tree
	 */
	void exitCreate_vp(PqlParser.Create_vpContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#vp_attribute}.
	 * @param ctx the parse tree
	 */
	void enterVp_attribute(PqlParser.Vp_attributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#vp_attribute}.
	 * @param ctx the parse tree
	 */
	void exitVp_attribute(PqlParser.Vp_attributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#drop_path}.
	 * @param ctx the parse tree
	 */
	void enterDrop_path(PqlParser.Drop_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#drop_path}.
	 * @param ctx the parse tree
	 */
	void exitDrop_path(PqlParser.Drop_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#drop_vp}.
	 * @param ctx the parse tree
	 */
	void enterDrop_vp(PqlParser.Drop_vpContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#drop_vp}.
	 * @param ctx the parse tree
	 */
	void exitDrop_vp(PqlParser.Drop_vpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitive_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimitive_expression(PqlParser.Primitive_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitive_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimitive_expression(PqlParser.Primitive_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subquery_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSubquery_expression(PqlParser.Subquery_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subquery_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSubquery_expression(PqlParser.Subquery_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bracket_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBracket_expression(PqlParser.Bracket_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bracket_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBracket_expression(PqlParser.Bracket_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binary_operator_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinary_operator_expression(PqlParser.Binary_operator_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binary_operator_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinary_operator_expression(PqlParser.Binary_operator_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unary_operator_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnary_operator_expression(PqlParser.Unary_operator_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unary_operator_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnary_operator_expression(PqlParser.Unary_operator_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code column_ref_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterColumn_ref_expression(PqlParser.Column_ref_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code column_ref_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitColumn_ref_expression(PqlParser.Column_ref_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code function_call_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call_expression(PqlParser.Function_call_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code function_call_expression}
	 * labeled alternative in {@link PqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call_expression(PqlParser.Function_call_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#constant_expression}.
	 * @param ctx the parse tree
	 */
	void enterConstant_expression(PqlParser.Constant_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#constant_expression}.
	 * @param ctx the parse tree
	 */
	void exitConstant_expression(PqlParser.Constant_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(PqlParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(PqlParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#search_condition_list}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_list(PqlParser.Search_condition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#search_condition_list}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_list(PqlParser.Search_condition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#search_condition}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition(PqlParser.Search_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#search_condition}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition(PqlParser.Search_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#search_condition_and}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_and(PqlParser.Search_condition_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#search_condition_and}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_and(PqlParser.Search_condition_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#search_condition_not}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_not(PqlParser.Search_condition_notContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#search_condition_not}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_not(PqlParser.Search_condition_notContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(PqlParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(PqlParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#query_expression}.
	 * @param ctx the parse tree
	 */
	void enterQuery_expression(PqlParser.Query_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#query_expression}.
	 * @param ctx the parse tree
	 */
	void exitQuery_expression(PqlParser.Query_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#query_specification}.
	 * @param ctx the parse tree
	 */
	void enterQuery_specification(PqlParser.Query_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#query_specification}.
	 * @param ctx the parse tree
	 */
	void exitQuery_specification(PqlParser.Query_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_clause(PqlParser.Order_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_clause(PqlParser.Order_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#order_by_expression}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_expression(PqlParser.Order_by_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#order_by_expression}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_expression(PqlParser.Order_by_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#group_by_item}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_item(PqlParser.Group_by_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#group_by_item}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_item(PqlParser.Group_by_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#nest_by_type}.
	 * @param ctx the parse tree
	 */
	void enterNest_by_type(PqlParser.Nest_by_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#nest_by_type}.
	 * @param ctx the parse tree
	 */
	void exitNest_by_type(PqlParser.Nest_by_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#select_list}.
	 * @param ctx the parse tree
	 */
	void enterSelect_list(PqlParser.Select_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#select_list}.
	 * @param ctx the parse tree
	 */
	void exitSelect_list(PqlParser.Select_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#select_list_elem}.
	 * @param ctx the parse tree
	 */
	void enterSelect_list_elem(PqlParser.Select_list_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#select_list_elem}.
	 * @param ctx the parse tree
	 */
	void exitSelect_list_elem(PqlParser.Select_list_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#path_source}.
	 * @param ctx the parse tree
	 */
	void enterPath_source(PqlParser.Path_sourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#path_source}.
	 * @param ctx the parse tree
	 */
	void exitPath_source(PqlParser.Path_sourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#path_source_item_joined}.
	 * @param ctx the parse tree
	 */
	void enterPath_source_item_joined(PqlParser.Path_source_item_joinedContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#path_source_item_joined}.
	 * @param ctx the parse tree
	 */
	void exitPath_source_item_joined(PqlParser.Path_source_item_joinedContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#path_source_item}.
	 * @param ctx the parse tree
	 */
	void enterPath_source_item(PqlParser.Path_source_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#path_source_item}.
	 * @param ctx the parse tree
	 */
	void exitPath_source_item(PqlParser.Path_source_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#join_part}.
	 * @param ctx the parse tree
	 */
	void enterJoin_part(PqlParser.Join_partContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#join_part}.
	 * @param ctx the parse tree
	 */
	void exitJoin_part(PqlParser.Join_partContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#path_with_hint}.
	 * @param ctx the parse tree
	 */
	void enterPath_with_hint(PqlParser.Path_with_hintContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#path_with_hint}.
	 * @param ctx the parse tree
	 */
	void exitPath_with_hint(PqlParser.Path_with_hintContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#as_path_alias}.
	 * @param ctx the parse tree
	 */
	void enterAs_path_alias(PqlParser.As_path_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#as_path_alias}.
	 * @param ctx the parse tree
	 */
	void exitAs_path_alias(PqlParser.As_path_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#path_alias}.
	 * @param ctx the parse tree
	 */
	void enterPath_alias(PqlParser.Path_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#path_alias}.
	 * @param ctx the parse tree
	 */
	void exitPath_alias(PqlParser.Path_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#to_path_hint}.
	 * @param ctx the parse tree
	 */
	void enterTo_path_hint(PqlParser.To_path_hintContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#to_path_hint}.
	 * @param ctx the parse tree
	 */
	void exitTo_path_hint(PqlParser.To_path_hintContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#with_path_hints}.
	 * @param ctx the parse tree
	 */
	void enterWith_path_hints(PqlParser.With_path_hintsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#with_path_hints}.
	 * @param ctx the parse tree
	 */
	void exitWith_path_hints(PqlParser.With_path_hintsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#path_hint}.
	 * @param ctx the parse tree
	 */
	void enterPath_hint(PqlParser.Path_hintContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#path_hint}.
	 * @param ctx the parse tree
	 */
	void exitPath_hint(PqlParser.Path_hintContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#column_alias_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias_list(PqlParser.Column_alias_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#column_alias_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias_list(PqlParser.Column_alias_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias(PqlParser.Column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias(PqlParser.Column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(PqlParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(PqlParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#path_name}.
	 * @param ctx the parse tree
	 */
	void enterPath_name(PqlParser.Path_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#path_name}.
	 * @param ctx the parse tree
	 */
	void exitPath_name(PqlParser.Path_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#path}.
	 * @param ctx the parse tree
	 */
	void enterPath(PqlParser.PathContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#path}.
	 * @param ctx the parse tree
	 */
	void exitPath(PqlParser.PathContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#vp_name}.
	 * @param ctx the parse tree
	 */
	void enterVp_name(PqlParser.Vp_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#vp_name}.
	 * @param ctx the parse tree
	 */
	void exitVp_name(PqlParser.Vp_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#vp}.
	 * @param ctx the parse tree
	 */
	void enterVp(PqlParser.VpContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#vp}.
	 * @param ctx the parse tree
	 */
	void exitVp(PqlParser.VpContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#full_column_name}.
	 * @param ctx the parse tree
	 */
	void enterFull_column_name(PqlParser.Full_column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#full_column_name}.
	 * @param ctx the parse tree
	 */
	void exitFull_column_name(PqlParser.Full_column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_list(PqlParser.Column_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_list(PqlParser.Column_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(PqlParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(PqlParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#null_notnull}.
	 * @param ctx the parse tree
	 */
	void enterNull_notnull(PqlParser.Null_notnullContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#null_notnull}.
	 * @param ctx the parse tree
	 */
	void exitNull_notnull(PqlParser.Null_notnullContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#default_value}.
	 * @param ctx the parse tree
	 */
	void enterDefault_value(PqlParser.Default_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#default_value}.
	 * @param ctx the parse tree
	 */
	void exitDefault_value(PqlParser.Default_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(PqlParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(PqlParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(PqlParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(PqlParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(PqlParser.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(PqlParser.SignContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(PqlParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(PqlParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#simple_id}.
	 * @param ctx the parse tree
	 */
	void enterSimple_id(PqlParser.Simple_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#simple_id}.
	 * @param ctx the parse tree
	 */
	void exitSimple_id(PqlParser.Simple_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparison_operator(PqlParser.Comparison_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparison_operator(PqlParser.Comparison_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link PqlParser#assignment_operator}.
	 * @param ctx the parse tree
	 */
	void enterAssignment_operator(PqlParser.Assignment_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link PqlParser#assignment_operator}.
	 * @param ctx the parse tree
	 */
	void exitAssignment_operator(PqlParser.Assignment_operatorContext ctx);
}