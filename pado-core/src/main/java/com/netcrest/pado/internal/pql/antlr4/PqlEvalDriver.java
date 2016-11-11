package com.netcrest.pado.internal.pql.antlr4;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.internal.pql.antlr4.generated.PqlLexer;
import com.netcrest.pado.internal.pql.antlr4.generated.PqlParser;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

@SuppressWarnings({ "rawtypes" })
public class PqlEvalDriver
{
	private static boolean DEBUG = PadoUtil.getBoolean("debug", false);

	public static String[] getArgNames(String queryString)
	{
		// Determine args
		ArrayList<String> argList = new ArrayList<String>(3);
		String strPart = queryString;
		int beginIndex = 0;
		while (beginIndex != -1) {
			beginIndex = strPart.indexOf("${");
			if (beginIndex != -1) {
				int endIndex = strPart.indexOf("}");
				if (endIndex != -1) {
					String arg = strPart.substring(beginIndex + 2, endIndex);
					arg = arg.trim();
					if (argList.contains(arg) == false) {
						argList.add(arg);
					}
					strPart = strPart.substring(endIndex + 1);
				} else {
					beginIndex = -1;
				}
			}
		}
		return argList.toArray(new String[argList.size()]);
	}

	private static String applyArgs(String queryString, String... args)
	{
		if (args == null || args.length == 0) {
			return queryString;
		}

		String argNames[] = getArgNames(queryString);

		int i = 0;
		for (String argName : argNames) {
			if (i >= args.length) {
				break;
			}
			String arg = args[i];
			if (arg != null && arg.length() > 0) {
				if (arg.startsWith("'") && arg.endsWith("'")) {
					// do nothing
				}
				if (arg.startsWith("(") && arg.endsWith(")")) {
					// Example: '(choco*)' - Must enclose in quotes
					// so that the PQL parser can properly parse.
					arg = "'" + arg + "'";
				} else if (arg.endsWith(")")) {
					arg = "'" + arg + "'";
				} else if (arg.contains(" ")) {
					// Example: tofu choco*
					arg = "'(" + arg + ")'";
				} else {
					// Example: choco* - Must enclose in parentheses
					char endChar = arg.charAt(arg.length() - 1);
					switch (endChar) {
					case '*':
					case '?':
					case '~':
						arg = "'(" + arg + ")'";
						break;
					default:
						arg = "'" + arg + "'";
						break;
					}
				}
			} else {
				arg = "";
			}
			queryString = queryString.replaceAll("\\$\\{" + argName + "\\}", arg);
			i++;
		}
		return queryString;
	}

	private static List execute(boolean isQueryEntries, ITemporalBizLink temporalBiz, long validAtTime, long asOfTime, String joinQueryString,
			String... args) throws IOException
	{
		joinQueryString = applyArgs(joinQueryString, args);
		if (DEBUG) {
			System.out.println(joinQueryString);
		}

		InputStream is = new ByteArrayInputStream(joinQueryString.getBytes(StandardCharsets.UTF_8));
		ANTLRInputStream input = new ANTLRInputStream(is);
		PqlLexer lexer = new PqlLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		PqlParser parser = new PqlParser(tokens);
		parser.setBuildParseTree(true); // tell ANTLR to build a parse tree
		ParseTree tree = parser.pql_file(); // parse

		// First, visit tree nodes to pre-compile
		PqlEvalVisitor evalVisitor = new PqlEvalVisitor();
		evalVisitor.visit(tree);

		// Execute by traversing listener
		ParseTreeWalker walker = new ParseTreeWalker();
		PqlEvalListener evalProp = new PqlEvalListener(evalVisitor.getPathAliasMap(), temporalBiz, validAtTime, asOfTime, isQueryEntries, evalVisitor.isAggregation());
		walker.walk(evalProp, tree);

		return evalProp.getResults();
	}

	public static List executeValues(ITemporalBizLink temporalBiz, long validAtTime, long asOfTime, String joinQueryString, String... args)
			throws IOException
	{
		return execute(false, temporalBiz, validAtTime, asOfTime, joinQueryString, args);
	}

	@SuppressWarnings("unchecked")
	public static List<TemporalEntry<ITemporalKey, ITemporalData>> executeEntries(ITemporalBizLink temporalBiz, long validAtTime, long asOfTime, 
			String joinQueryString, String... args) throws IOException
	{
		return execute(true, temporalBiz, validAtTime, asOfTime, joinQueryString, args);
	}
	
	@SuppressWarnings("unchecked")
	public static IScrollableResultSet executeValuesScrolled(ITemporalBizLink temporalBiz, GridQuery query,
			String joinQueryString, String... args) throws IOException
	{
		if (query == null) {
			return null;
		}
		long validAtTime = query.getValidAtTime();
		long asOfTime = query.getAsOfTime();
		List results = execute(false, temporalBiz, validAtTime, asOfTime, joinQueryString, args);
		LocalSrollableResultSet rs = new LocalSrollableResultSet(query, results);
		return rs;
	}

	@SuppressWarnings("unchecked")
	public static IScrollableResultSet executeEntriesScrolled(ITemporalBizLink temporalBiz, long validAtTime, long asOfTime, GridQuery query,
			String joinQueryString, String... args) throws IOException
	{
		query.getParam("");
		List results = execute(true, temporalBiz, validAtTime, asOfTime, joinQueryString, args);
		LocalSrollableResultSet rs = new LocalSrollableResultSet(query, results);
		return rs;
	}

	public static void main(String[] args) throws Exception
	{
		String inputFile = null;
		if (args.length > 0)
			inputFile = args[0];
		InputStream is = System.in;
		if (inputFile != null) {
			is = new FileInputStream(inputFile);
		}
		ANTLRInputStream input = new ANTLRInputStream(is);
		PqlLexer lexer = new PqlLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		PqlParser parser = new PqlParser(tokens);
		parser.setBuildParseTree(true); // tell ANTLR to build a parse tree
		ParseTree tree = parser.pql_file(); // parse
		// show tree in text form
		System.out.println(tree.toStringTree(parser));

		// Visitor
		PqlEvalVisitor evalVisitor = new PqlEvalVisitor();
		evalVisitor.visit(tree);
		// evalVisitor.visit(parser.comparison_operator());

		// Listener
		ParseTreeWalker walker = new ParseTreeWalker();
		PqlEvalListener evalProp = new PqlEvalListener(evalVisitor.getPathAliasMap());
		walker.walk(evalProp, tree);

		List results = evalProp.getResults();
		System.out.println("Final results:");
		int i = 0;
		for (Object object : results) {
			System.out.println("   " + ++i + ". " + object);
		}
	}

	public static void main2(String[] args) throws Exception
	{
		String joinQueryString = "";
		InputStream is = new ByteArrayInputStream(joinQueryString.getBytes(StandardCharsets.UTF_8));
		ANTLRInputStream input = new ANTLRInputStream(is);
		PqlLexer lexer = new PqlLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		PqlParser parser = new PqlParser(tokens);
		parser.setBuildParseTree(true); // tell ANTLR to build a parse tree
		ParseTree tree = parser.pql_file(); // parse
		// show tree in text form
		System.out.println(tree.toStringTree(parser));

		// Visitor
		PqlEvalVisitor evalVisitor = new PqlEvalVisitor();
		evalVisitor.visit(tree);
		// evalVisitor.visit(parser.comparison_operator());

		// Listener
		ParseTreeWalker walker = new ParseTreeWalker();
		PqlEvalListener evalProp = new PqlEvalListener(evalVisitor.getPathAliasMap());
		walker.walk(evalProp, tree);

		List results = evalProp.getResults();
		System.out.println("Final results:");
		int i = 0;
		for (Object object : results) {
			System.out.println("   " + ++i + ". " + object);
		}
	}
}