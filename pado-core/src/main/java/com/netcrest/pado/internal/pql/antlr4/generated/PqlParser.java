// Generated from Pql.g4 by ANTLR 4.5.2
package com.netcrest.pado.internal.pql.antlr4.generated;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, ADD=2, AGGREGATION=3, ALL=4, ALTER=5, AND=6, ANY=7, AS=8, ASC=9, 
		AUTHORIZATION=10, BACKUP=11, BEGIN=12, BETWEEN=13, BREAK=14, BROWSE=15, 
		BULK=16, BY=17, CASCADE=18, CASE=19, CHANGETABLE=20, CHANGES=21, CHECK=22, 
		CHECKPOINT=23, CLOSE=24, CLUSTERED=25, COALESCE=26, COLLATE=27, COLUMN=28, 
		COMMIT=29, COMPUTE=30, CONSTRAINT=31, CONTAINS=32, CONTAINSTABLE=33, CONTINUE=34, 
		CONVERT=35, CREATE=36, CROSS=37, CURRENT=38, CURRENT_DATE=39, CURRENT_TIME=40, 
		CURRENT_TIMESTAMP=41, CURRENT_USER=42, CURSOR=43, DATABASE=44, DBCC=45, 
		DEALLOCATE=46, DECLARE=47, DEFAULT=48, DELETE=49, DENY=50, DESC=51, DISK=52, 
		DISTINCT=53, DISTRIBUTED=54, DOUBLE=55, DROP=56, DUMP=57, ELSE=58, END=59, 
		ERRLVL=60, ESCAPE=61, EXCEPT=62, EXEC=63, EXECUTE=64, EXISTS=65, EXIT=66, 
		EXTERNAL=67, FETCH=68, FILE=69, FILLFACTOR=70, FOR=71, FORCESEEK=72, FOREIGN=73, 
		FREETEXT=74, FREETEXTTABLE=75, FROM=76, FULL=77, FUNCTION=78, GOTO=79, 
		GRANT=80, GROUP=81, HAS=82, HAVING=83, IDENTITY=84, IDENTITYCOL=85, IDENTITY_INSERT=86, 
		IF=87, IN=88, INDEX=89, INHERITANCE=90, INNER=91, INSERT=92, INTERSECT=93, 
		INTO=94, IS=95, JOIN=96, KEY=97, KILL=98, LEFT=99, LIKE=100, LINENO=101, 
		LOAD=102, MANY=103, MERGE=104, NATIONAL=105, NEST=106, NOCHECK=107, NONCLUSTERED=108, 
		NOT=109, NULL=110, NULLIF=111, ONE=112, OF=113, OFF=114, OFFSETS=115, 
		ON=116, OPEN=117, OPENDATASOURCE=118, OPENQUERY=119, OPENROWSET=120, OPENXML=121, 
		OPTION=122, OR=123, ORDER=124, OUTER=125, OVER=126, PERCENT=127, PIVOT=128, 
		PLAN=129, PRECISION=130, PRIMARY=131, PRINT=132, PROC=133, PROCEDURE=134, 
		PUBLIC=135, RAISERROR=136, READ=137, READTEXT=138, RECONFIGURE=139, REFERENCES=140, 
		REPLICATION=141, RESTORE=142, RESTRICT=143, RETURN=144, REVERT=145, REVOKE=146, 
		RIGHT=147, ROLLBACK=148, ROWCOUNT=149, ROWGUIDCOL=150, RULE=151, SAVE=152, 
		SCHEMA=153, SECURITYAUDIT=154, SELECT=155, SEMANTICKEYPHRASETABLE=156, 
		SEMANTICSIMILARITYDETAILSTABLE=157, SEMANTICSIMILARITYTABLE=158, SESSION_USER=159, 
		SET=160, SETUSER=161, SHUTDOWN=162, SOME=163, STATISTICS=164, SYSTEM_USER=165, 
		TEXTSIZE=166, THEN=167, TO=168, TOP=169, TRAN=170, TRANSACTION=171, TRIGGER=172, 
		TRUNCATE=173, TRY_CONVERT=174, TSEQUAL=175, UNION=176, UNIQUE=177, UNPIVOT=178, 
		UPDATE=179, UPDATETEXT=180, USE=181, USER=182, VALUES=183, VARYING=184, 
		VP=185, WAITFOR=186, WHEN=187, WHERE=188, WHILE=189, WITH=190, WITHIN=191, 
		WRITETEXT=192, ABSOLUTE=193, APPLY=194, AUTO=195, AVG=196, BASE64=197, 
		BINARY_CHECKSUM=198, CALLER=199, CAST=200, CATCH=201, CHECKSUM=202, CHECKSUM_AGG=203, 
		COMMITTED=204, CONCAT=205, COOKIE=206, COUNT=207, COUNT_BIG=208, DATEADD=209, 
		DATEDIFF=210, DATENAME=211, DATEPART=212, DELAY=213, DELETED=214, DENSE_RANK=215, 
		DISABLE=216, DYNAMIC=217, ENCRYPTION=218, FAST=219, FAST_FORWARD=220, 
		FIRST=221, FOLLOWING=222, FORWARD_ONLY=223, FULLSCAN=224, GLOBAL=225, 
		GO=226, GROUPING=227, GROUPING_ID=228, HASH=229, INSENSITIVE=230, INSERTED=231, 
		ISOLATION=232, KEEPFIXED=233, KEYSET=234, LAST=235, LEVEL=236, LOCAL=237, 
		LOCK_ESCALATION=238, LOGIN=239, LOOP=240, MARK=241, MAX=242, MIN=243, 
		MIN_ACTIVE_ROWVERSION=244, MODIFY=245, NEXT=246, NAME=247, NOCOUNT=248, 
		NOEXPAND=249, NORECOMPUTE=250, NTILE=251, NUMBER=252, OFFSET=253, ONLY=254, 
		OPTIMISTIC=255, OPTIMIZE=256, OUT=257, OUTPUT=258, OWNER=259, PARTITION=260, 
		PATH=261, PRECEDING=262, PRIOR=263, RANGE=264, RANK=265, READONLY=266, 
		READ_ONLY=267, RECOMPILE=268, RELATIVE=269, REMOTE=270, REPEATABLE=271, 
		ROOT=272, ROW=273, ROWGUID=274, ROWS=275, ROW_NUMBER=276, SAMPLE=277, 
		SCHEMABINDING=278, SCROLL=279, SCROLL_LOCKS=280, SELF=281, SERIALIZABLE=282, 
		SNAPSHOT=283, SPATIAL_WINDOW_MAX_CELLS=284, STATIC=285, STATS_STREAM=286, 
		STDEV=287, STDEVP=288, SUM=289, THROW=290, TIES=291, TIME=292, TRY=293, 
		TYPE=294, TYPE_WARNING=295, UNBOUNDED=296, UNCOMMITTED=297, UNKNOWN=298, 
		USING=299, VAR=300, VARP=301, VP_METADATA=302, WORK=303, XML=304, XMLNAMESPACES=305, 
		DOLLAR_ACTION=306, SPACE=307, COMMENT=308, LINE_COMMENT=309, DOUBLE_QUOTE_ID=310, 
		SQUARE_BRACKET_ID=311, DECIMAL=312, ID=313, STRING=314, BINARY=315, FLOAT=316, 
		REAL=317, EQUAL=318, GREATER=319, LESS=320, EXCLAMATION=321, PLUS_ASSIGN=322, 
		MINUS_ASSIGN=323, MULT_ASSIGN=324, DIV_ASSIGN=325, MOD_ASSIGN=326, AND_ASSIGN=327, 
		XOR_ASSIGN=328, OR_ASSIGN=329, DOT=330, UNDERLINE=331, AT=332, SHARP=333, 
		DOLLAR=334, LR_BRACKET=335, RR_BRACKET=336, COMMA=337, SEMI=338, COLON=339, 
		STAR=340, DIVIDE=341, MODULE=342, PLUS=343, MINUS=344, BIT_NOT=345, BIT_OR=346, 
		BIT_AND=347, BIT_XOR=348;
	public static final int
		RULE_pql_file = 0, RULE_sql_clause = 1, RULE_dml_clause = 2, RULE_ddl_clause = 3, 
		RULE_select_statement = 4, RULE_create_vp = 5, RULE_vp_attribute = 6, 
		RULE_drop_path = 7, RULE_drop_vp = 8, RULE_expression = 9, RULE_constant_expression = 10, 
		RULE_subquery = 11, RULE_search_condition_list = 12, RULE_search_condition = 13, 
		RULE_search_condition_and = 14, RULE_search_condition_not = 15, RULE_predicate = 16, 
		RULE_query_expression = 17, RULE_query_specification = 18, RULE_order_by_clause = 19, 
		RULE_order_by_expression = 20, RULE_group_by_item = 21, RULE_nest_by_type = 22, 
		RULE_select_list = 23, RULE_select_list_elem = 24, RULE_path_source = 25, 
		RULE_path_source_item_joined = 26, RULE_path_source_item = 27, RULE_join_part = 28, 
		RULE_path_with_hint = 29, RULE_as_path_alias = 30, RULE_path_alias = 31, 
		RULE_to_path_hint = 32, RULE_with_path_hints = 33, RULE_path_hint = 34, 
		RULE_column_alias_list = 35, RULE_column_alias = 36, RULE_expression_list = 37, 
		RULE_path_name = 38, RULE_path = 39, RULE_vp_name = 40, RULE_vp = 41, 
		RULE_full_column_name = 42, RULE_column_name_list = 43, RULE_column_name = 44, 
		RULE_null_notnull = 45, RULE_default_value = 46, RULE_constant = 47, RULE_number = 48, 
		RULE_sign = 49, RULE_id = 50, RULE_simple_id = 51, RULE_comparison_operator = 52, 
		RULE_assignment_operator = 53;
	public static final String[] ruleNames = {
		"pql_file", "sql_clause", "dml_clause", "ddl_clause", "select_statement", 
		"create_vp", "vp_attribute", "drop_path", "drop_vp", "expression", "constant_expression", 
		"subquery", "search_condition_list", "search_condition", "search_condition_and", 
		"search_condition_not", "predicate", "query_expression", "query_specification", 
		"order_by_clause", "order_by_expression", "group_by_item", "nest_by_type", 
		"select_list", "select_list_elem", "path_source", "path_source_item_joined", 
		"path_source_item", "join_part", "path_with_hint", "as_path_alias", "path_alias", 
		"to_path_hint", "with_path_hints", "path_hint", "column_alias_list", "column_alias", 
		"expression_list", "path_name", "path", "vp_name", "vp", "full_column_name", 
		"column_name_list", "column_name", "null_notnull", "default_value", "constant", 
		"number", "sign", "id", "simple_id", "comparison_operator", "assignment_operator"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'?'", null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, "'='", "'>'", "'<'", "'!'", "'+='", 
		"'-='", "'*='", "'/='", "'%='", "'&='", "'^='", "'|='", "'.'", "'_'", 
		"'@'", "'#'", "'$'", "'('", "')'", "','", "';'", "':'", "'*'", "'/'", 
		"'%'", "'+'", "'-'", "'~'", "'|'", "'&'", "'^'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "ADD", "AGGREGATION", "ALL", "ALTER", "AND", "ANY", "AS", 
		"ASC", "AUTHORIZATION", "BACKUP", "BEGIN", "BETWEEN", "BREAK", "BROWSE", 
		"BULK", "BY", "CASCADE", "CASE", "CHANGETABLE", "CHANGES", "CHECK", "CHECKPOINT", 
		"CLOSE", "CLUSTERED", "COALESCE", "COLLATE", "COLUMN", "COMMIT", "COMPUTE", 
		"CONSTRAINT", "CONTAINS", "CONTAINSTABLE", "CONTINUE", "CONVERT", "CREATE", 
		"CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", 
		"CURRENT_USER", "CURSOR", "DATABASE", "DBCC", "DEALLOCATE", "DECLARE", 
		"DEFAULT", "DELETE", "DENY", "DESC", "DISK", "DISTINCT", "DISTRIBUTED", 
		"DOUBLE", "DROP", "DUMP", "ELSE", "END", "ERRLVL", "ESCAPE", "EXCEPT", 
		"EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL", "FETCH", "FILE", "FILLFACTOR", 
		"FOR", "FORCESEEK", "FOREIGN", "FREETEXT", "FREETEXTTABLE", "FROM", "FULL", 
		"FUNCTION", "GOTO", "GRANT", "GROUP", "HAS", "HAVING", "IDENTITY", "IDENTITYCOL", 
		"IDENTITY_INSERT", "IF", "IN", "INDEX", "INHERITANCE", "INNER", "INSERT", 
		"INTERSECT", "INTO", "IS", "JOIN", "KEY", "KILL", "LEFT", "LIKE", "LINENO", 
		"LOAD", "MANY", "MERGE", "NATIONAL", "NEST", "NOCHECK", "NONCLUSTERED", 
		"NOT", "NULL", "NULLIF", "ONE", "OF", "OFF", "OFFSETS", "ON", "OPEN", 
		"OPENDATASOURCE", "OPENQUERY", "OPENROWSET", "OPENXML", "OPTION", "OR", 
		"ORDER", "OUTER", "OVER", "PERCENT", "PIVOT", "PLAN", "PRECISION", "PRIMARY", 
		"PRINT", "PROC", "PROCEDURE", "PUBLIC", "RAISERROR", "READ", "READTEXT", 
		"RECONFIGURE", "REFERENCES", "REPLICATION", "RESTORE", "RESTRICT", "RETURN", 
		"REVERT", "REVOKE", "RIGHT", "ROLLBACK", "ROWCOUNT", "ROWGUIDCOL", "RULE", 
		"SAVE", "SCHEMA", "SECURITYAUDIT", "SELECT", "SEMANTICKEYPHRASETABLE", 
		"SEMANTICSIMILARITYDETAILSTABLE", "SEMANTICSIMILARITYTABLE", "SESSION_USER", 
		"SET", "SETUSER", "SHUTDOWN", "SOME", "STATISTICS", "SYSTEM_USER", "TEXTSIZE", 
		"THEN", "TO", "TOP", "TRAN", "TRANSACTION", "TRIGGER", "TRUNCATE", "TRY_CONVERT", 
		"TSEQUAL", "UNION", "UNIQUE", "UNPIVOT", "UPDATE", "UPDATETEXT", "USE", 
		"USER", "VALUES", "VARYING", "VP", "WAITFOR", "WHEN", "WHERE", "WHILE", 
		"WITH", "WITHIN", "WRITETEXT", "ABSOLUTE", "APPLY", "AUTO", "AVG", "BASE64", 
		"BINARY_CHECKSUM", "CALLER", "CAST", "CATCH", "CHECKSUM", "CHECKSUM_AGG", 
		"COMMITTED", "CONCAT", "COOKIE", "COUNT", "COUNT_BIG", "DATEADD", "DATEDIFF", 
		"DATENAME", "DATEPART", "DELAY", "DELETED", "DENSE_RANK", "DISABLE", "DYNAMIC", 
		"ENCRYPTION", "FAST", "FAST_FORWARD", "FIRST", "FOLLOWING", "FORWARD_ONLY", 
		"FULLSCAN", "GLOBAL", "GO", "GROUPING", "GROUPING_ID", "HASH", "INSENSITIVE", 
		"INSERTED", "ISOLATION", "KEEPFIXED", "KEYSET", "LAST", "LEVEL", "LOCAL", 
		"LOCK_ESCALATION", "LOGIN", "LOOP", "MARK", "MAX", "MIN", "MIN_ACTIVE_ROWVERSION", 
		"MODIFY", "NEXT", "NAME", "NOCOUNT", "NOEXPAND", "NORECOMPUTE", "NTILE", 
		"NUMBER", "OFFSET", "ONLY", "OPTIMISTIC", "OPTIMIZE", "OUT", "OUTPUT", 
		"OWNER", "PARTITION", "PATH", "PRECEDING", "PRIOR", "RANGE", "RANK", "READONLY", 
		"READ_ONLY", "RECOMPILE", "RELATIVE", "REMOTE", "REPEATABLE", "ROOT", 
		"ROW", "ROWGUID", "ROWS", "ROW_NUMBER", "SAMPLE", "SCHEMABINDING", "SCROLL", 
		"SCROLL_LOCKS", "SELF", "SERIALIZABLE", "SNAPSHOT", "SPATIAL_WINDOW_MAX_CELLS", 
		"STATIC", "STATS_STREAM", "STDEV", "STDEVP", "SUM", "THROW", "TIES", "TIME", 
		"TRY", "TYPE", "TYPE_WARNING", "UNBOUNDED", "UNCOMMITTED", "UNKNOWN", 
		"USING", "VAR", "VARP", "VP_METADATA", "WORK", "XML", "XMLNAMESPACES", 
		"DOLLAR_ACTION", "SPACE", "COMMENT", "LINE_COMMENT", "DOUBLE_QUOTE_ID", 
		"SQUARE_BRACKET_ID", "DECIMAL", "ID", "STRING", "BINARY", "FLOAT", "REAL", 
		"EQUAL", "GREATER", "LESS", "EXCLAMATION", "PLUS_ASSIGN", "MINUS_ASSIGN", 
		"MULT_ASSIGN", "DIV_ASSIGN", "MOD_ASSIGN", "AND_ASSIGN", "XOR_ASSIGN", 
		"OR_ASSIGN", "DOT", "UNDERLINE", "AT", "SHARP", "DOLLAR", "LR_BRACKET", 
		"RR_BRACKET", "COMMA", "SEMI", "COLON", "STAR", "DIVIDE", "MODULE", "PLUS", 
		"MINUS", "BIT_NOT", "BIT_OR", "BIT_AND", "BIT_XOR"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Pql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class Pql_fileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(PqlParser.EOF, 0); }
		public List<Sql_clauseContext> sql_clause() {
			return getRuleContexts(Sql_clauseContext.class);
		}
		public Sql_clauseContext sql_clause(int i) {
			return getRuleContext(Sql_clauseContext.class,i);
		}
		public Pql_fileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pql_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPql_file(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPql_file(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPql_file(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Pql_fileContext pql_file() throws RecognitionException {
		Pql_fileContext _localctx = new Pql_fileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_pql_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CREATE || _la==DROP || _la==SELECT || _la==LR_BRACKET) {
				{
				{
				setState(108);
				sql_clause();
				}
				}
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(114);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Sql_clauseContext extends ParserRuleContext {
		public Dml_clauseContext dml_clause() {
			return getRuleContext(Dml_clauseContext.class,0);
		}
		public Ddl_clauseContext ddl_clause() {
			return getRuleContext(Ddl_clauseContext.class,0);
		}
		public Sql_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sql_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSql_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSql_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSql_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Sql_clauseContext sql_clause() throws RecognitionException {
		Sql_clauseContext _localctx = new Sql_clauseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_sql_clause);
		try {
			setState(118);
			switch (_input.LA(1)) {
			case SELECT:
			case LR_BRACKET:
				enterOuterAlt(_localctx, 1);
				{
				setState(116);
				dml_clause();
				}
				break;
			case CREATE:
			case DROP:
				enterOuterAlt(_localctx, 2);
				{
				setState(117);
				ddl_clause();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Dml_clauseContext extends ParserRuleContext {
		public Select_statementContext select_statement() {
			return getRuleContext(Select_statementContext.class,0);
		}
		public Dml_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dml_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterDml_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitDml_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitDml_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dml_clauseContext dml_clause() throws RecognitionException {
		Dml_clauseContext _localctx = new Dml_clauseContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_dml_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(120);
			select_statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ddl_clauseContext extends ParserRuleContext {
		public Create_vpContext create_vp() {
			return getRuleContext(Create_vpContext.class,0);
		}
		public Drop_pathContext drop_path() {
			return getRuleContext(Drop_pathContext.class,0);
		}
		public Drop_vpContext drop_vp() {
			return getRuleContext(Drop_vpContext.class,0);
		}
		public Ddl_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ddl_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterDdl_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitDdl_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitDdl_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ddl_clauseContext ddl_clause() throws RecognitionException {
		Ddl_clauseContext _localctx = new Ddl_clauseContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_ddl_clause);
		try {
			setState(125);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(122);
				create_vp();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(123);
				drop_path();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(124);
				drop_vp();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_statementContext extends ParserRuleContext {
		public Query_expressionContext query_expression() {
			return getRuleContext(Query_expressionContext.class,0);
		}
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Select_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSelect_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSelect_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSelect_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_statementContext select_statement() throws RecognitionException {
		Select_statementContext _localctx = new Select_statementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_select_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			query_expression();
			setState(129);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(128);
				order_by_clause();
				}
			}

			setState(132);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(131);
				match(SEMI);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Create_vpContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(PqlParser.CREATE, 0); }
		public TerminalNode VP() { return getToken(PqlParser.VP, 0); }
		public VpContext vp() {
			return getRuleContext(VpContext.class,0);
		}
		public TerminalNode AS() { return getToken(PqlParser.AS, 0); }
		public Select_statementContext select_statement() {
			return getRuleContext(Select_statementContext.class,0);
		}
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public List<TerminalNode> WITH() { return getTokens(PqlParser.WITH); }
		public TerminalNode WITH(int i) {
			return getToken(PqlParser.WITH, i);
		}
		public List<Vp_attributeContext> vp_attribute() {
			return getRuleContexts(Vp_attributeContext.class);
		}
		public Vp_attributeContext vp_attribute(int i) {
			return getRuleContext(Vp_attributeContext.class,i);
		}
		public TerminalNode CHECK() { return getToken(PqlParser.CHECK, 0); }
		public TerminalNode OPTION() { return getToken(PqlParser.OPTION, 0); }
		public Create_vpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_vp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterCreate_vp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitCreate_vp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitCreate_vp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_vpContext create_vp() throws RecognitionException {
		Create_vpContext _localctx = new Create_vpContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_create_vp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(CREATE);
			setState(135);
			match(VP);
			setState(136);
			vp();
			setState(148);
			_la = _input.LA(1);
			if (_la==LR_BRACKET) {
				{
				setState(137);
				match(LR_BRACKET);
				setState(138);
				column_name();
				setState(143);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(139);
					match(COMMA);
					setState(140);
					column_name();
					}
					}
					setState(145);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(146);
				match(RR_BRACKET);
				}
			}

			setState(159);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(150);
				match(WITH);
				setState(151);
				vp_attribute();
				setState(156);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(152);
					match(COMMA);
					setState(153);
					vp_attribute();
					}
					}
					setState(158);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(161);
			match(AS);
			setState(162);
			select_statement();
			setState(166);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(163);
				match(WITH);
				setState(164);
				match(CHECK);
				setState(165);
				match(OPTION);
				}
			}

			setState(169);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(168);
				match(SEMI);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vp_attributeContext extends ParserRuleContext {
		public TerminalNode ENCRYPTION() { return getToken(PqlParser.ENCRYPTION, 0); }
		public TerminalNode SCHEMABINDING() { return getToken(PqlParser.SCHEMABINDING, 0); }
		public TerminalNode VP_METADATA() { return getToken(PqlParser.VP_METADATA, 0); }
		public Vp_attributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vp_attribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterVp_attribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitVp_attribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitVp_attribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Vp_attributeContext vp_attribute() throws RecognitionException {
		Vp_attributeContext _localctx = new Vp_attributeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_vp_attribute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			_la = _input.LA(1);
			if ( !(_la==ENCRYPTION || _la==SCHEMABINDING || _la==VP_METADATA) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Drop_pathContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(PqlParser.DROP, 0); }
		public TerminalNode PATH() { return getToken(PqlParser.PATH, 0); }
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public TerminalNode IF() { return getToken(PqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(PqlParser.EXISTS, 0); }
		public Drop_pathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterDrop_path(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitDrop_path(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitDrop_path(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_pathContext drop_path() throws RecognitionException {
		Drop_pathContext _localctx = new Drop_pathContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_drop_path);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			match(DROP);
			setState(174);
			match(PATH);
			setState(177);
			_la = _input.LA(1);
			if (_la==IF) {
				{
				setState(175);
				match(IF);
				setState(176);
				match(EXISTS);
				}
			}

			setState(179);
			path();
			setState(181);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(180);
				match(SEMI);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Drop_vpContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(PqlParser.DROP, 0); }
		public TerminalNode VP() { return getToken(PqlParser.VP, 0); }
		public List<VpContext> vp() {
			return getRuleContexts(VpContext.class);
		}
		public VpContext vp(int i) {
			return getRuleContext(VpContext.class,i);
		}
		public TerminalNode IF() { return getToken(PqlParser.IF, 0); }
		public TerminalNode EXISTS() { return getToken(PqlParser.EXISTS, 0); }
		public Drop_vpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_vp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterDrop_vp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitDrop_vp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitDrop_vp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_vpContext drop_vp() throws RecognitionException {
		Drop_vpContext _localctx = new Drop_vpContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_drop_vp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			match(DROP);
			setState(184);
			match(VP);
			setState(187);
			_la = _input.LA(1);
			if (_la==IF) {
				{
				setState(185);
				match(IF);
				setState(186);
				match(EXISTS);
				}
			}

			setState(189);
			vp();
			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(190);
				match(COMMA);
				setState(191);
				vp();
				}
				}
				setState(196);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(198);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(197);
				match(SEMI);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Primitive_expressionContext extends ExpressionContext {
		public TerminalNode DEFAULT() { return getToken(PqlParser.DEFAULT, 0); }
		public TerminalNode NULL() { return getToken(PqlParser.NULL, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public Primitive_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPrimitive_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPrimitive_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPrimitive_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Subquery_expressionContext extends ExpressionContext {
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public Subquery_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSubquery_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSubquery_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSubquery_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Bracket_expressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Bracket_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterBracket_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitBracket_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitBracket_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Binary_operator_expressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public Binary_operator_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterBinary_operator_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitBinary_operator_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitBinary_operator_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Unary_operator_expressionContext extends ExpressionContext {
		public Token op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Unary_operator_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterUnary_operator_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitUnary_operator_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitUnary_operator_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Column_ref_expressionContext extends ExpressionContext {
		public Full_column_nameContext full_column_name() {
			return getRuleContext(Full_column_nameContext.class,0);
		}
		public Column_ref_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterColumn_ref_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitColumn_ref_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitColumn_ref_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Function_call_expressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode COLLATE() { return getToken(PqlParser.COLLATE, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Function_call_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterFunction_call_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitFunction_call_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitFunction_call_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 18;
		enterRecursionRule(_localctx, 18, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				_localctx = new Unary_operator_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(201);
				match(BIT_NOT);
				setState(202);
				expression(5);
				}
				break;
			case 2:
				{
				_localctx = new Unary_operator_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(203);
				((Unary_operator_expressionContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
					((Unary_operator_expressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				} else {
					consume();
				}
				setState(204);
				expression(3);
				}
				break;
			case 3:
				{
				_localctx = new Primitive_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(205);
				match(DEFAULT);
				}
				break;
			case 4:
				{
				_localctx = new Primitive_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(206);
				match(NULL);
				}
				break;
			case 5:
				{
				_localctx = new Primitive_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(207);
				constant();
				}
				break;
			case 6:
				{
				_localctx = new Column_ref_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(208);
				full_column_name();
				}
				break;
			case 7:
				{
				_localctx = new Bracket_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(209);
				match(LR_BRACKET);
				setState(210);
				expression(0);
				setState(211);
				match(RR_BRACKET);
				}
				break;
			case 8:
				{
				_localctx = new Subquery_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(213);
				match(LR_BRACKET);
				setState(214);
				subquery();
				setState(215);
				match(RR_BRACKET);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(234);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(232);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
					case 1:
						{
						_localctx = new Binary_operator_expressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(219);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(220);
						((Binary_operator_expressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 340)) & ~0x3f) == 0 && ((1L << (_la - 340)) & ((1L << (STAR - 340)) | (1L << (DIVIDE - 340)) | (1L << (MODULE - 340)))) != 0)) ) {
							((Binary_operator_expressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						} else {
							consume();
						}
						setState(221);
						expression(5);
						}
						break;
					case 2:
						{
						_localctx = new Binary_operator_expressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(222);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(223);
						((Binary_operator_expressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 343)) & ~0x3f) == 0 && ((1L << (_la - 343)) & ((1L << (PLUS - 343)) | (1L << (MINUS - 343)) | (1L << (BIT_OR - 343)) | (1L << (BIT_AND - 343)) | (1L << (BIT_XOR - 343)))) != 0)) ) {
							((Binary_operator_expressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						} else {
							consume();
						}
						setState(224);
						expression(3);
						}
						break;
					case 3:
						{
						_localctx = new Binary_operator_expressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(225);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(226);
						comparison_operator();
						setState(227);
						expression(2);
						}
						break;
					case 4:
						{
						_localctx = new Function_call_expressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(229);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(230);
						match(COLLATE);
						setState(231);
						id();
						}
						break;
					}
					} 
				}
				setState(236);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Constant_expressionContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(PqlParser.NULL, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public Constant_expressionContext constant_expression() {
			return getRuleContext(Constant_expressionContext.class,0);
		}
		public Constant_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterConstant_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitConstant_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitConstant_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constant_expressionContext constant_expression() throws RecognitionException {
		Constant_expressionContext _localctx = new Constant_expressionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_constant_expression);
		try {
			setState(243);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(237);
				match(NULL);
				}
				break;
			case DECIMAL:
			case STRING:
			case BINARY:
			case FLOAT:
			case REAL:
			case DOLLAR:
			case PLUS:
			case MINUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(238);
				constant();
				}
				break;
			case LR_BRACKET:
				enterOuterAlt(_localctx, 3);
				{
				setState(239);
				match(LR_BRACKET);
				setState(240);
				constant_expression();
				setState(241);
				match(RR_BRACKET);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubqueryContext extends ParserRuleContext {
		public Select_statementContext select_statement() {
			return getRuleContext(Select_statementContext.class,0);
		}
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSubquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSubquery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			select_statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Search_condition_listContext extends ParserRuleContext {
		public List<Search_conditionContext> search_condition() {
			return getRuleContexts(Search_conditionContext.class);
		}
		public Search_conditionContext search_condition(int i) {
			return getRuleContext(Search_conditionContext.class,i);
		}
		public Search_condition_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSearch_condition_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSearch_condition_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSearch_condition_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_condition_listContext search_condition_list() throws RecognitionException {
		Search_condition_listContext _localctx = new Search_condition_listContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_search_condition_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			search_condition();
			setState(252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(248);
				match(COMMA);
				setState(249);
				search_condition();
				}
				}
				setState(254);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Search_conditionContext extends ParserRuleContext {
		public List<Search_condition_andContext> search_condition_and() {
			return getRuleContexts(Search_condition_andContext.class);
		}
		public Search_condition_andContext search_condition_and(int i) {
			return getRuleContext(Search_condition_andContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(PqlParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(PqlParser.OR, i);
		}
		public Search_conditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSearch_condition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSearch_condition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSearch_condition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_conditionContext search_condition() throws RecognitionException {
		Search_conditionContext _localctx = new Search_conditionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_search_condition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(255);
			search_condition_and();
			setState(260);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(256);
				match(OR);
				setState(257);
				search_condition_and();
				}
				}
				setState(262);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Search_condition_andContext extends ParserRuleContext {
		public List<Search_condition_notContext> search_condition_not() {
			return getRuleContexts(Search_condition_notContext.class);
		}
		public Search_condition_notContext search_condition_not(int i) {
			return getRuleContext(Search_condition_notContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(PqlParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(PqlParser.AND, i);
		}
		public Search_condition_andContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSearch_condition_and(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSearch_condition_and(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSearch_condition_and(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_condition_andContext search_condition_and() throws RecognitionException {
		Search_condition_andContext _localctx = new Search_condition_andContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_search_condition_and);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
			search_condition_not();
			setState(268);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(264);
				match(AND);
				setState(265);
				search_condition_not();
				}
				}
				setState(270);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Search_condition_notContext extends ParserRuleContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode NOT() { return getToken(PqlParser.NOT, 0); }
		public Search_condition_notContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSearch_condition_not(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSearch_condition_not(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSearch_condition_not(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_condition_notContext search_condition_not() throws RecognitionException {
		Search_condition_notContext _localctx = new Search_condition_notContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_search_condition_not);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(272);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(271);
				match(NOT);
				}
			}

			setState(274);
			predicate();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public TerminalNode EXISTS() { return getToken(PqlParser.EXISTS, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public TerminalNode ALL() { return getToken(PqlParser.ALL, 0); }
		public TerminalNode SOME() { return getToken(PqlParser.SOME, 0); }
		public TerminalNode ANY() { return getToken(PqlParser.ANY, 0); }
		public TerminalNode BETWEEN() { return getToken(PqlParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(PqlParser.AND, 0); }
		public TerminalNode NOT() { return getToken(PqlParser.NOT, 0); }
		public TerminalNode IN() { return getToken(PqlParser.IN, 0); }
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public TerminalNode LIKE() { return getToken(PqlParser.LIKE, 0); }
		public TerminalNode ESCAPE() { return getToken(PqlParser.ESCAPE, 0); }
		public TerminalNode IS() { return getToken(PqlParser.IS, 0); }
		public Null_notnullContext null_notnull() {
			return getRuleContext(Null_notnullContext.class,0);
		}
		public Search_conditionContext search_condition() {
			return getRuleContext(Search_conditionContext.class,0);
		}
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_predicate);
		int _la;
		try {
			setState(331);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(276);
				match(EXISTS);
				setState(277);
				match(LR_BRACKET);
				setState(278);
				subquery();
				setState(279);
				match(RR_BRACKET);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(281);
				expression(0);
				setState(282);
				comparison_operator();
				setState(283);
				expression(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(285);
				expression(0);
				setState(286);
				comparison_operator();
				setState(287);
				_la = _input.LA(1);
				if ( !(_la==ALL || _la==ANY || _la==SOME) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				setState(288);
				match(LR_BRACKET);
				setState(289);
				subquery();
				setState(290);
				match(RR_BRACKET);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(292);
				expression(0);
				setState(294);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(293);
					match(NOT);
					}
				}

				setState(296);
				match(BETWEEN);
				setState(297);
				expression(0);
				setState(298);
				match(AND);
				setState(299);
				expression(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(301);
				expression(0);
				setState(303);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(302);
					match(NOT);
					}
				}

				setState(305);
				match(IN);
				setState(306);
				match(LR_BRACKET);
				setState(309);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(307);
					subquery();
					}
					break;
				case 2:
					{
					setState(308);
					expression_list();
					}
					break;
				}
				setState(311);
				match(RR_BRACKET);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(313);
				expression(0);
				setState(315);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(314);
					match(NOT);
					}
				}

				setState(317);
				match(LIKE);
				setState(318);
				expression(0);
				setState(321);
				_la = _input.LA(1);
				if (_la==ESCAPE) {
					{
					setState(319);
					match(ESCAPE);
					setState(320);
					expression(0);
					}
				}

				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(323);
				expression(0);
				setState(324);
				match(IS);
				setState(325);
				null_notnull();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(327);
				match(LR_BRACKET);
				setState(328);
				search_condition();
				setState(329);
				match(RR_BRACKET);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Query_expressionContext extends ParserRuleContext {
		public Query_specificationContext query_specification() {
			return getRuleContext(Query_specificationContext.class,0);
		}
		public Query_expressionContext query_expression() {
			return getRuleContext(Query_expressionContext.class,0);
		}
		public Query_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterQuery_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitQuery_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitQuery_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Query_expressionContext query_expression() throws RecognitionException {
		Query_expressionContext _localctx = new Query_expressionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_query_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338);
			switch (_input.LA(1)) {
			case SELECT:
				{
				setState(333);
				query_specification();
				}
				break;
			case LR_BRACKET:
				{
				setState(334);
				match(LR_BRACKET);
				setState(335);
				query_expression();
				setState(336);
				match(RR_BRACKET);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Query_specificationContext extends ParserRuleContext {
		public Path_nameContext into_path;
		public Search_conditionContext where;
		public Search_conditionContext having;
		public TerminalNode SELECT() { return getToken(PqlParser.SELECT, 0); }
		public Select_listContext select_list() {
			return getRuleContext(Select_listContext.class,0);
		}
		public TerminalNode INTO() { return getToken(PqlParser.INTO, 0); }
		public TerminalNode FROM() { return getToken(PqlParser.FROM, 0); }
		public List<Path_sourceContext> path_source() {
			return getRuleContexts(Path_sourceContext.class);
		}
		public Path_sourceContext path_source(int i) {
			return getRuleContext(Path_sourceContext.class,i);
		}
		public TerminalNode WHERE() { return getToken(PqlParser.WHERE, 0); }
		public TerminalNode GROUP() { return getToken(PqlParser.GROUP, 0); }
		public List<TerminalNode> BY() { return getTokens(PqlParser.BY); }
		public TerminalNode BY(int i) {
			return getToken(PqlParser.BY, i);
		}
		public List<Group_by_itemContext> group_by_item() {
			return getRuleContexts(Group_by_itemContext.class);
		}
		public Group_by_itemContext group_by_item(int i) {
			return getRuleContext(Group_by_itemContext.class,i);
		}
		public TerminalNode HAVING() { return getToken(PqlParser.HAVING, 0); }
		public TerminalNode NEST() { return getToken(PqlParser.NEST, 0); }
		public Nest_by_typeContext nest_by_type() {
			return getRuleContext(Nest_by_typeContext.class,0);
		}
		public Path_nameContext path_name() {
			return getRuleContext(Path_nameContext.class,0);
		}
		public List<Search_conditionContext> search_condition() {
			return getRuleContexts(Search_conditionContext.class);
		}
		public Search_conditionContext search_condition(int i) {
			return getRuleContext(Search_conditionContext.class,i);
		}
		public Query_specificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query_specification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterQuery_specification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitQuery_specification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitQuery_specification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Query_specificationContext query_specification() throws RecognitionException {
		Query_specificationContext _localctx = new Query_specificationContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_query_specification);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(340);
			match(SELECT);
			setState(341);
			select_list();
			setState(344);
			_la = _input.LA(1);
			if (_la==INTO) {
				{
				setState(342);
				match(INTO);
				setState(343);
				((Query_specificationContext)_localctx).into_path = path_name();
				}
			}

			setState(355);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(346);
				match(FROM);
				setState(347);
				path_source();
				setState(352);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(348);
					match(COMMA);
					setState(349);
					path_source();
					}
					}
					setState(354);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(359);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(357);
				match(WHERE);
				setState(358);
				((Query_specificationContext)_localctx).where = search_condition();
				}
			}

			setState(371);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(361);
				match(GROUP);
				setState(362);
				match(BY);
				setState(363);
				group_by_item();
				setState(368);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(364);
					match(COMMA);
					setState(365);
					group_by_item();
					}
					}
					setState(370);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(375);
			_la = _input.LA(1);
			if (_la==HAVING) {
				{
				setState(373);
				match(HAVING);
				setState(374);
				((Query_specificationContext)_localctx).having = search_condition();
				}
			}

			setState(380);
			_la = _input.LA(1);
			if (_la==NEST) {
				{
				setState(377);
				match(NEST);
				setState(378);
				match(BY);
				setState(379);
				nest_by_type();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Order_by_clauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(PqlParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(PqlParser.BY, 0); }
		public List<Order_by_expressionContext> order_by_expression() {
			return getRuleContexts(Order_by_expressionContext.class);
		}
		public Order_by_expressionContext order_by_expression(int i) {
			return getRuleContext(Order_by_expressionContext.class,i);
		}
		public TerminalNode OFFSET() { return getToken(PqlParser.OFFSET, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> ROW() { return getTokens(PqlParser.ROW); }
		public TerminalNode ROW(int i) {
			return getToken(PqlParser.ROW, i);
		}
		public List<TerminalNode> ROWS() { return getTokens(PqlParser.ROWS); }
		public TerminalNode ROWS(int i) {
			return getToken(PqlParser.ROWS, i);
		}
		public TerminalNode FETCH() { return getToken(PqlParser.FETCH, 0); }
		public TerminalNode ONLY() { return getToken(PqlParser.ONLY, 0); }
		public TerminalNode FIRST() { return getToken(PqlParser.FIRST, 0); }
		public TerminalNode NEXT() { return getToken(PqlParser.NEXT, 0); }
		public Order_by_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_order_by_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterOrder_by_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitOrder_by_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitOrder_by_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Order_by_clauseContext order_by_clause() throws RecognitionException {
		Order_by_clauseContext _localctx = new Order_by_clauseContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_order_by_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(382);
			match(ORDER);
			setState(383);
			match(BY);
			setState(384);
			order_by_expression();
			setState(389);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(385);
				match(COMMA);
				setState(386);
				order_by_expression();
				}
				}
				setState(391);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(403);
			_la = _input.LA(1);
			if (_la==OFFSET) {
				{
				setState(392);
				match(OFFSET);
				setState(393);
				expression(0);
				setState(394);
				_la = _input.LA(1);
				if ( !(_la==ROW || _la==ROWS) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				setState(401);
				_la = _input.LA(1);
				if (_la==FETCH) {
					{
					setState(395);
					match(FETCH);
					setState(396);
					_la = _input.LA(1);
					if ( !(_la==FIRST || _la==NEXT) ) {
					_errHandler.recoverInline(this);
					} else {
						consume();
					}
					setState(397);
					expression(0);
					setState(398);
					_la = _input.LA(1);
					if ( !(_la==ROW || _la==ROWS) ) {
					_errHandler.recoverInline(this);
					} else {
						consume();
					}
					setState(399);
					match(ONLY);
					}
				}

				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Order_by_expressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ASC() { return getToken(PqlParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(PqlParser.DESC, 0); }
		public Order_by_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_order_by_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterOrder_by_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitOrder_by_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitOrder_by_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Order_by_expressionContext order_by_expression() throws RecognitionException {
		Order_by_expressionContext _localctx = new Order_by_expressionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_order_by_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			expression(0);
			setState(407);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(406);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Group_by_itemContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Group_by_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_group_by_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterGroup_by_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitGroup_by_item(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitGroup_by_item(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Group_by_itemContext group_by_item() throws RecognitionException {
		Group_by_itemContext _localctx = new Group_by_itemContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_group_by_item);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(409);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Nest_by_typeContext extends ParserRuleContext {
		public TerminalNode AGGREGATION() { return getToken(PqlParser.AGGREGATION, 0); }
		public TerminalNode INHERITANCE() { return getToken(PqlParser.INHERITANCE, 0); }
		public Nest_by_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nest_by_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterNest_by_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitNest_by_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitNest_by_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Nest_by_typeContext nest_by_type() throws RecognitionException {
		Nest_by_typeContext _localctx = new Nest_by_typeContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_nest_by_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(411);
			_la = _input.LA(1);
			if ( !(_la==AGGREGATION || _la==INHERITANCE) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_listContext extends ParserRuleContext {
		public List<Select_list_elemContext> select_list_elem() {
			return getRuleContexts(Select_list_elemContext.class);
		}
		public Select_list_elemContext select_list_elem(int i) {
			return getRuleContext(Select_list_elemContext.class,i);
		}
		public Select_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSelect_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSelect_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSelect_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_listContext select_list() throws RecognitionException {
		Select_listContext _localctx = new Select_listContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_select_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(413);
			select_list_elem();
			setState(418);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(414);
				match(COMMA);
				setState(415);
				select_list_elem();
				}
				}
				setState(420);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_list_elemContext extends ParserRuleContext {
		public Path_nameContext path_name() {
			return getRuleContext(Path_nameContext.class,0);
		}
		public TerminalNode IDENTITY() { return getToken(PqlParser.IDENTITY, 0); }
		public TerminalNode ROWGUID() { return getToken(PqlParser.ROWGUID, 0); }
		public Column_aliasContext column_alias() {
			return getRuleContext(Column_aliasContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(PqlParser.AS, 0); }
		public Select_list_elemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_list_elem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSelect_list_elem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSelect_list_elem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSelect_list_elem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_list_elemContext select_list_elem() throws RecognitionException {
		Select_list_elemContext _localctx = new Select_list_elemContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_select_list_elem);
		int _la;
		try {
			setState(442);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(424);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(421);
					path_name();
					setState(422);
					match(DOT);
					}
				}

				setState(429);
				switch (_input.LA(1)) {
				case STAR:
					{
					setState(426);
					match(STAR);
					}
					break;
				case DOLLAR:
					{
					setState(427);
					match(DOLLAR);
					setState(428);
					_la = _input.LA(1);
					if ( !(_la==IDENTITY || _la==ROWGUID) ) {
					_errHandler.recoverInline(this);
					} else {
						consume();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(431);
				column_alias();
				setState(432);
				match(EQUAL);
				setState(433);
				expression(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(435);
				expression(0);
				setState(440);
				_la = _input.LA(1);
				if (_la==AS || _la==FORCESEEK || ((((_la - 193)) & ~0x3f) == 0 && ((1L << (_la - 193)) & ((1L << (ABSOLUTE - 193)) | (1L << (APPLY - 193)) | (1L << (AUTO - 193)) | (1L << (AVG - 193)) | (1L << (BASE64 - 193)) | (1L << (CALLER - 193)) | (1L << (CAST - 193)) | (1L << (CATCH - 193)) | (1L << (CHECKSUM_AGG - 193)) | (1L << (COMMITTED - 193)) | (1L << (CONCAT - 193)) | (1L << (COOKIE - 193)) | (1L << (COUNT - 193)) | (1L << (COUNT_BIG - 193)) | (1L << (DELAY - 193)) | (1L << (DELETED - 193)) | (1L << (DENSE_RANK - 193)) | (1L << (DISABLE - 193)) | (1L << (DYNAMIC - 193)) | (1L << (ENCRYPTION - 193)) | (1L << (FAST - 193)) | (1L << (FAST_FORWARD - 193)) | (1L << (FIRST - 193)) | (1L << (FOLLOWING - 193)) | (1L << (FORWARD_ONLY - 193)) | (1L << (FULLSCAN - 193)) | (1L << (GLOBAL - 193)) | (1L << (GO - 193)) | (1L << (GROUPING - 193)) | (1L << (GROUPING_ID - 193)) | (1L << (HASH - 193)) | (1L << (INSENSITIVE - 193)) | (1L << (INSERTED - 193)) | (1L << (ISOLATION - 193)) | (1L << (KEEPFIXED - 193)) | (1L << (KEYSET - 193)) | (1L << (LAST - 193)) | (1L << (LEVEL - 193)) | (1L << (LOCAL - 193)) | (1L << (LOCK_ESCALATION - 193)) | (1L << (LOGIN - 193)) | (1L << (LOOP - 193)) | (1L << (MARK - 193)) | (1L << (MAX - 193)) | (1L << (MIN - 193)) | (1L << (MODIFY - 193)) | (1L << (NEXT - 193)) | (1L << (NAME - 193)) | (1L << (NOCOUNT - 193)) | (1L << (NOEXPAND - 193)) | (1L << (NORECOMPUTE - 193)) | (1L << (NTILE - 193)) | (1L << (NUMBER - 193)) | (1L << (OFFSET - 193)) | (1L << (ONLY - 193)) | (1L << (OPTIMISTIC - 193)) | (1L << (OPTIMIZE - 193)))) != 0) || ((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & ((1L << (OUT - 257)) | (1L << (OUTPUT - 257)) | (1L << (OWNER - 257)) | (1L << (PARTITION - 257)) | (1L << (PATH - 257)) | (1L << (PRECEDING - 257)) | (1L << (PRIOR - 257)) | (1L << (RANGE - 257)) | (1L << (RANK - 257)) | (1L << (READONLY - 257)) | (1L << (READ_ONLY - 257)) | (1L << (RECOMPILE - 257)) | (1L << (RELATIVE - 257)) | (1L << (REMOTE - 257)) | (1L << (REPEATABLE - 257)) | (1L << (ROOT - 257)) | (1L << (ROW - 257)) | (1L << (ROWGUID - 257)) | (1L << (ROWS - 257)) | (1L << (ROW_NUMBER - 257)) | (1L << (SAMPLE - 257)) | (1L << (SCHEMABINDING - 257)) | (1L << (SCROLL - 257)) | (1L << (SCROLL_LOCKS - 257)) | (1L << (SELF - 257)) | (1L << (SERIALIZABLE - 257)) | (1L << (SNAPSHOT - 257)) | (1L << (SPATIAL_WINDOW_MAX_CELLS - 257)) | (1L << (STATIC - 257)) | (1L << (STATS_STREAM - 257)) | (1L << (STDEV - 257)) | (1L << (STDEVP - 257)) | (1L << (SUM - 257)) | (1L << (THROW - 257)) | (1L << (TIES - 257)) | (1L << (TIME - 257)) | (1L << (TRY - 257)) | (1L << (TYPE - 257)) | (1L << (TYPE_WARNING - 257)) | (1L << (UNBOUNDED - 257)) | (1L << (UNCOMMITTED - 257)) | (1L << (UNKNOWN - 257)) | (1L << (USING - 257)) | (1L << (VAR - 257)) | (1L << (VARP - 257)) | (1L << (VP_METADATA - 257)) | (1L << (WORK - 257)) | (1L << (XML - 257)) | (1L << (XMLNAMESPACES - 257)) | (1L << (DOUBLE_QUOTE_ID - 257)) | (1L << (SQUARE_BRACKET_ID - 257)) | (1L << (ID - 257)) | (1L << (STRING - 257)))) != 0)) {
					{
					setState(437);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(436);
						match(AS);
						}
					}

					setState(439);
					column_alias();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_sourceContext extends ParserRuleContext {
		public Path_source_item_joinedContext path_source_item_joined() {
			return getRuleContext(Path_source_item_joinedContext.class,0);
		}
		public Path_sourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_source; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPath_source(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPath_source(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPath_source(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_sourceContext path_source() throws RecognitionException {
		Path_sourceContext _localctx = new Path_sourceContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_path_source);
		try {
			setState(449);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(444);
				path_source_item_joined();
				}
				break;
			case LR_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(445);
				match(LR_BRACKET);
				setState(446);
				path_source_item_joined();
				setState(447);
				match(RR_BRACKET);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_source_item_joinedContext extends ParserRuleContext {
		public Path_source_itemContext path_source_item() {
			return getRuleContext(Path_source_itemContext.class,0);
		}
		public List<Join_partContext> join_part() {
			return getRuleContexts(Join_partContext.class);
		}
		public Join_partContext join_part(int i) {
			return getRuleContext(Join_partContext.class,i);
		}
		public Path_source_item_joinedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_source_item_joined; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPath_source_item_joined(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPath_source_item_joined(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPath_source_item_joined(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_source_item_joinedContext path_source_item_joined() throws RecognitionException {
		Path_source_item_joinedContext _localctx = new Path_source_item_joinedContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_path_source_item_joined);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(451);
			path_source_item();
			setState(455);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(452);
					join_part();
					}
					} 
				}
				setState(457);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_source_itemContext extends ParserRuleContext {
		public Path_with_hintContext path_with_hint() {
			return getRuleContext(Path_with_hintContext.class,0);
		}
		public As_path_aliasContext as_path_alias() {
			return getRuleContext(As_path_aliasContext.class,0);
		}
		public Path_source_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_source_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPath_source_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPath_source_item(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPath_source_item(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_source_itemContext path_source_item() throws RecognitionException {
		Path_source_itemContext _localctx = new Path_source_itemContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_path_source_item);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(458);
			path_with_hint();
			setState(460);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				{
				setState(459);
				as_path_alias();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Join_partContext extends ParserRuleContext {
		public Token join_type;
		public Token join_hint;
		public TerminalNode JOIN() { return getToken(PqlParser.JOIN, 0); }
		public Path_sourceContext path_source() {
			return getRuleContext(Path_sourceContext.class,0);
		}
		public TerminalNode ON() { return getToken(PqlParser.ON, 0); }
		public Search_conditionContext search_condition() {
			return getRuleContext(Search_conditionContext.class,0);
		}
		public TerminalNode LEFT() { return getToken(PqlParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(PqlParser.RIGHT, 0); }
		public TerminalNode FULL() { return getToken(PqlParser.FULL, 0); }
		public TerminalNode INNER() { return getToken(PqlParser.INNER, 0); }
		public TerminalNode OUTER() { return getToken(PqlParser.OUTER, 0); }
		public TerminalNode LOOP() { return getToken(PqlParser.LOOP, 0); }
		public TerminalNode HASH() { return getToken(PqlParser.HASH, 0); }
		public TerminalNode MERGE() { return getToken(PqlParser.MERGE, 0); }
		public TerminalNode REMOTE() { return getToken(PqlParser.REMOTE, 0); }
		public TerminalNode CROSS() { return getToken(PqlParser.CROSS, 0); }
		public TerminalNode APPLY() { return getToken(PqlParser.APPLY, 0); }
		public Join_partContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_part; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterJoin_part(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitJoin_part(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitJoin_part(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Join_partContext join_part() throws RecognitionException {
		Join_partContext _localctx = new Join_partContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_join_part);
		int _la;
		try {
			setState(488);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(469);
				switch (_input.LA(1)) {
				case INNER:
				case JOIN:
				case MERGE:
				case HASH:
				case LOOP:
				case REMOTE:
					{
					setState(463);
					_la = _input.LA(1);
					if (_la==INNER) {
						{
						setState(462);
						match(INNER);
						}
					}

					}
					break;
				case FULL:
				case LEFT:
				case RIGHT:
					{
					setState(465);
					((Join_partContext)_localctx).join_type = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==FULL || _la==LEFT || _la==RIGHT) ) {
						((Join_partContext)_localctx).join_type = (Token)_errHandler.recoverInline(this);
					} else {
						consume();
					}
					setState(467);
					_la = _input.LA(1);
					if (_la==OUTER) {
						{
						setState(466);
						match(OUTER);
						}
					}

					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(472);
				_la = _input.LA(1);
				if (_la==MERGE || ((((_la - 229)) & ~0x3f) == 0 && ((1L << (_la - 229)) & ((1L << (HASH - 229)) | (1L << (LOOP - 229)) | (1L << (REMOTE - 229)))) != 0)) {
					{
					setState(471);
					((Join_partContext)_localctx).join_hint = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==MERGE || ((((_la - 229)) & ~0x3f) == 0 && ((1L << (_la - 229)) & ((1L << (HASH - 229)) | (1L << (LOOP - 229)) | (1L << (REMOTE - 229)))) != 0)) ) {
						((Join_partContext)_localctx).join_hint = (Token)_errHandler.recoverInline(this);
					} else {
						consume();
					}
					}
				}

				setState(474);
				match(JOIN);
				setState(475);
				path_source();
				setState(476);
				match(ON);
				setState(477);
				search_condition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(479);
				match(CROSS);
				setState(480);
				match(JOIN);
				setState(481);
				path_source();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(482);
				match(CROSS);
				setState(483);
				match(APPLY);
				setState(484);
				path_source();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(485);
				match(OUTER);
				setState(486);
				match(APPLY);
				setState(487);
				path_source();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_with_hintContext extends ParserRuleContext {
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public With_path_hintsContext with_path_hints() {
			return getRuleContext(With_path_hintsContext.class,0);
		}
		public Path_with_hintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_with_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPath_with_hint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPath_with_hint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPath_with_hint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_with_hintContext path_with_hint() throws RecognitionException {
		Path_with_hintContext _localctx = new Path_with_hintContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_path_with_hint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(490);
			path();
			setState(492);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(491);
				with_path_hints();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class As_path_aliasContext extends ParserRuleContext {
		public Path_aliasContext path_alias() {
			return getRuleContext(Path_aliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(PqlParser.AS, 0); }
		public As_path_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_as_path_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterAs_path_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitAs_path_alias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitAs_path_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final As_path_aliasContext as_path_alias() throws RecognitionException {
		As_path_aliasContext _localctx = new As_path_aliasContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_as_path_alias);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(495);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(494);
				match(AS);
				}
			}

			setState(497);
			path_alias();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_aliasContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public To_path_hintContext to_path_hint() {
			return getRuleContext(To_path_hintContext.class,0);
		}
		public With_path_hintsContext with_path_hints() {
			return getRuleContext(With_path_hintsContext.class,0);
		}
		public Path_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPath_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPath_alias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPath_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_aliasContext path_alias() throws RecognitionException {
		Path_aliasContext _localctx = new Path_aliasContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_path_alias);
		try {
			setState(507);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(499);
				id();
				setState(501);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
				case 1:
					{
					setState(500);
					to_path_hint();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(503);
				id();
				setState(505);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
				case 1:
					{
					setState(504);
					with_path_hints();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class To_path_hintContext extends ParserRuleContext {
		public Column_aliasContext column_alias() {
			return getRuleContext(Column_aliasContext.class,0);
		}
		public TerminalNode TO() { return getToken(PqlParser.TO, 0); }
		public TerminalNode MANY() { return getToken(PqlParser.MANY, 0); }
		public TerminalNode ONE() { return getToken(PqlParser.ONE, 0); }
		public To_path_hintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_to_path_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterTo_path_hint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitTo_path_hint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitTo_path_hint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final To_path_hintContext to_path_hint() throws RecognitionException {
		To_path_hintContext _localctx = new To_path_hintContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_to_path_hint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(511);
			_la = _input.LA(1);
			if (_la==TO) {
				{
				setState(509);
				match(TO);
				setState(510);
				_la = _input.LA(1);
				if ( !(_la==MANY || _la==ONE) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
			}

			setState(513);
			column_alias();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class With_path_hintsContext extends ParserRuleContext {
		public List<Path_hintContext> path_hint() {
			return getRuleContexts(Path_hintContext.class);
		}
		public Path_hintContext path_hint(int i) {
			return getRuleContext(Path_hintContext.class,i);
		}
		public TerminalNode WITH() { return getToken(PqlParser.WITH, 0); }
		public With_path_hintsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_with_path_hints; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterWith_path_hints(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitWith_path_hints(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitWith_path_hints(this);
			else return visitor.visitChildren(this);
		}
	}

	public final With_path_hintsContext with_path_hints() throws RecognitionException {
		With_path_hintsContext _localctx = new With_path_hintsContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_with_path_hints);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(515);
				match(WITH);
				}
			}

			setState(518);
			match(LR_BRACKET);
			setState(519);
			path_hint();
			setState(524);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(520);
				match(COMMA);
				setState(521);
				path_hint();
				}
				}
				setState(526);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(527);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_hintContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(PqlParser.ID, 0); }
		public Path_hintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPath_hint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPath_hint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPath_hint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_hintContext path_hint() throws RecognitionException {
		Path_hintContext _localctx = new Path_hintContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_path_hint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(529);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_alias_listContext extends ParserRuleContext {
		public List<Column_aliasContext> column_alias() {
			return getRuleContexts(Column_aliasContext.class);
		}
		public Column_aliasContext column_alias(int i) {
			return getRuleContext(Column_aliasContext.class,i);
		}
		public Column_alias_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_alias_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterColumn_alias_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitColumn_alias_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitColumn_alias_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_alias_listContext column_alias_list() throws RecognitionException {
		Column_alias_listContext _localctx = new Column_alias_listContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_column_alias_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(531);
			match(LR_BRACKET);
			setState(532);
			column_alias();
			setState(537);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(533);
				match(COMMA);
				setState(534);
				column_alias();
				}
				}
				setState(539);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(540);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_aliasContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode STRING() { return getToken(PqlParser.STRING, 0); }
		public Column_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterColumn_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitColumn_alias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitColumn_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_aliasContext column_alias() throws RecognitionException {
		Column_aliasContext _localctx = new Column_aliasContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_column_alias);
		try {
			setState(544);
			switch (_input.LA(1)) {
			case FORCESEEK:
			case ABSOLUTE:
			case APPLY:
			case AUTO:
			case AVG:
			case BASE64:
			case CALLER:
			case CAST:
			case CATCH:
			case CHECKSUM_AGG:
			case COMMITTED:
			case CONCAT:
			case COOKIE:
			case COUNT:
			case COUNT_BIG:
			case DELAY:
			case DELETED:
			case DENSE_RANK:
			case DISABLE:
			case DYNAMIC:
			case ENCRYPTION:
			case FAST:
			case FAST_FORWARD:
			case FIRST:
			case FOLLOWING:
			case FORWARD_ONLY:
			case FULLSCAN:
			case GLOBAL:
			case GO:
			case GROUPING:
			case GROUPING_ID:
			case HASH:
			case INSENSITIVE:
			case INSERTED:
			case ISOLATION:
			case KEEPFIXED:
			case KEYSET:
			case LAST:
			case LEVEL:
			case LOCAL:
			case LOCK_ESCALATION:
			case LOGIN:
			case LOOP:
			case MARK:
			case MAX:
			case MIN:
			case MODIFY:
			case NEXT:
			case NAME:
			case NOCOUNT:
			case NOEXPAND:
			case NORECOMPUTE:
			case NTILE:
			case NUMBER:
			case OFFSET:
			case ONLY:
			case OPTIMISTIC:
			case OPTIMIZE:
			case OUT:
			case OUTPUT:
			case OWNER:
			case PARTITION:
			case PATH:
			case PRECEDING:
			case PRIOR:
			case RANGE:
			case RANK:
			case READONLY:
			case READ_ONLY:
			case RECOMPILE:
			case RELATIVE:
			case REMOTE:
			case REPEATABLE:
			case ROOT:
			case ROW:
			case ROWGUID:
			case ROWS:
			case ROW_NUMBER:
			case SAMPLE:
			case SCHEMABINDING:
			case SCROLL:
			case SCROLL_LOCKS:
			case SELF:
			case SERIALIZABLE:
			case SNAPSHOT:
			case SPATIAL_WINDOW_MAX_CELLS:
			case STATIC:
			case STATS_STREAM:
			case STDEV:
			case STDEVP:
			case SUM:
			case THROW:
			case TIES:
			case TIME:
			case TRY:
			case TYPE:
			case TYPE_WARNING:
			case UNBOUNDED:
			case UNCOMMITTED:
			case UNKNOWN:
			case USING:
			case VAR:
			case VARP:
			case VP_METADATA:
			case WORK:
			case XML:
			case XMLNAMESPACES:
			case DOUBLE_QUOTE_ID:
			case SQUARE_BRACKET_ID:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(542);
				id();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(543);
				match(STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Expression_listContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Expression_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterExpression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitExpression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitExpression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_listContext expression_list() throws RecognitionException {
		Expression_listContext _localctx = new Expression_listContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_expression_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(546);
			expression(0);
			setState(551);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(547);
				match(COMMA);
				setState(548);
				expression(0);
				}
				}
				setState(553);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_nameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(PqlParser.ID, 0); }
		public Path_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPath_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPath_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPath_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_nameContext path_name() throws RecognitionException {
		Path_nameContext _localctx = new Path_nameContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_path_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(554);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathContext extends ParserRuleContext {
		public List<Path_nameContext> path_name() {
			return getRuleContexts(Path_nameContext.class);
		}
		public Path_nameContext path_name(int i) {
			return getRuleContext(Path_nameContext.class,i);
		}
		public PathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitPath(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitPath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathContext path() throws RecognitionException {
		PathContext _localctx = new PathContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_path);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(556);
			path_name();
			setState(561);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DIVIDE) {
				{
				{
				setState(557);
				match(DIVIDE);
				setState(558);
				path_name();
				}
				}
				setState(563);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vp_nameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(PqlParser.ID, 0); }
		public Vp_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vp_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterVp_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitVp_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitVp_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Vp_nameContext vp_name() throws RecognitionException {
		Vp_nameContext _localctx = new Vp_nameContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_vp_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(564);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VpContext extends ParserRuleContext {
		public List<Vp_nameContext> vp_name() {
			return getRuleContexts(Vp_nameContext.class);
		}
		public Vp_nameContext vp_name(int i) {
			return getRuleContext(Vp_nameContext.class,i);
		}
		public VpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterVp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitVp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitVp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VpContext vp() throws RecognitionException {
		VpContext _localctx = new VpContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_vp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(566);
			vp_name();
			setState(571);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DIVIDE) {
				{
				{
				setState(567);
				match(DIVIDE);
				setState(568);
				vp_name();
				}
				}
				setState(573);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Full_column_nameContext extends ParserRuleContext {
		public Column_nameContext column_name() {
			return getRuleContext(Column_nameContext.class,0);
		}
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public Full_column_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_full_column_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterFull_column_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitFull_column_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitFull_column_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Full_column_nameContext full_column_name() throws RecognitionException {
		Full_column_nameContext _localctx = new Full_column_nameContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_full_column_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(577);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(574);
				path();
				setState(575);
				match(DOT);
				}
				break;
			}
			setState(579);
			column_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_name_listContext extends ParserRuleContext {
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public Column_name_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterColumn_name_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitColumn_name_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitColumn_name_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_name_listContext column_name_list() throws RecognitionException {
		Column_name_listContext _localctx = new Column_name_listContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_column_name_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(581);
			column_name();
			setState(586);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(582);
				match(COMMA);
				setState(583);
				column_name();
				}
				}
				setState(588);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_nameContext extends ParserRuleContext {
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public TerminalNode DOT() { return getToken(PqlParser.DOT, 0); }
		public Column_aliasContext column_alias() {
			return getRuleContext(Column_aliasContext.class,0);
		}
		public Path_aliasContext path_alias() {
			return getRuleContext(Path_aliasContext.class,0);
		}
		public Column_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterColumn_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitColumn_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitColumn_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_nameContext column_name() throws RecognitionException {
		Column_nameContext _localctx = new Column_nameContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_column_name);
		try {
			setState(602);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(592);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
				case 1:
					{
					setState(589);
					id();
					setState(590);
					match(DOT);
					}
					break;
				}
				setState(594);
				id();
				setState(596);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
				case 1:
					{
					setState(595);
					column_alias();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(599);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
				case 1:
					{
					setState(598);
					path_alias();
					}
					break;
				}
				setState(601);
				id();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Null_notnullContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(PqlParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(PqlParser.NOT, 0); }
		public Null_notnullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_null_notnull; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterNull_notnull(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitNull_notnull(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitNull_notnull(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Null_notnullContext null_notnull() throws RecognitionException {
		Null_notnullContext _localctx = new Null_notnullContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_null_notnull);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(605);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(604);
				match(NOT);
				}
			}

			setState(607);
			match(NULL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Default_valueContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(PqlParser.NULL, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public Default_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_default_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterDefault_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitDefault_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitDefault_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Default_valueContext default_value() throws RecognitionException {
		Default_valueContext _localctx = new Default_valueContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_default_value);
		try {
			setState(611);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(609);
				match(NULL);
				}
				break;
			case DECIMAL:
			case STRING:
			case BINARY:
			case FLOAT:
			case REAL:
			case DOLLAR:
			case PLUS:
			case MINUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(610);
				constant();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstantContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(PqlParser.STRING, 0); }
		public TerminalNode BINARY() { return getToken(PqlParser.BINARY, 0); }
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public TerminalNode REAL() { return getToken(PqlParser.REAL, 0); }
		public TerminalNode FLOAT() { return getToken(PqlParser.FLOAT, 0); }
		public SignContext sign() {
			return getRuleContext(SignContext.class,0);
		}
		public TerminalNode DECIMAL() { return getToken(PqlParser.DECIMAL, 0); }
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_constant);
		int _la;
		try {
			setState(625);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(613);
				match(STRING);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(614);
				match(BINARY);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(615);
				number();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(617);
				_la = _input.LA(1);
				if (_la==PLUS || _la==MINUS) {
					{
					setState(616);
					sign();
					}
				}

				setState(619);
				_la = _input.LA(1);
				if ( !(_la==FLOAT || _la==REAL) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(621);
				_la = _input.LA(1);
				if (_la==PLUS || _la==MINUS) {
					{
					setState(620);
					sign();
					}
				}

				setState(623);
				match(DOLLAR);
				setState(624);
				_la = _input.LA(1);
				if ( !(_la==DECIMAL || _la==FLOAT) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode DECIMAL() { return getToken(PqlParser.DECIMAL, 0); }
		public SignContext sign() {
			return getRuleContext(SignContext.class,0);
		}
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(628);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(627);
				sign();
				}
			}

			setState(630);
			match(DECIMAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SignContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(PqlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(PqlParser.MINUS, 0); }
		public SignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SignContext sign() throws RecognitionException {
		SignContext _localctx = new SignContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_sign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(632);
			_la = _input.LA(1);
			if ( !(_la==PLUS || _la==MINUS) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdContext extends ParserRuleContext {
		public Simple_idContext simple_id() {
			return getRuleContext(Simple_idContext.class,0);
		}
		public TerminalNode DOUBLE_QUOTE_ID() { return getToken(PqlParser.DOUBLE_QUOTE_ID, 0); }
		public TerminalNode SQUARE_BRACKET_ID() { return getToken(PqlParser.SQUARE_BRACKET_ID, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_id);
		try {
			setState(637);
			switch (_input.LA(1)) {
			case FORCESEEK:
			case ABSOLUTE:
			case APPLY:
			case AUTO:
			case AVG:
			case BASE64:
			case CALLER:
			case CAST:
			case CATCH:
			case CHECKSUM_AGG:
			case COMMITTED:
			case CONCAT:
			case COOKIE:
			case COUNT:
			case COUNT_BIG:
			case DELAY:
			case DELETED:
			case DENSE_RANK:
			case DISABLE:
			case DYNAMIC:
			case ENCRYPTION:
			case FAST:
			case FAST_FORWARD:
			case FIRST:
			case FOLLOWING:
			case FORWARD_ONLY:
			case FULLSCAN:
			case GLOBAL:
			case GO:
			case GROUPING:
			case GROUPING_ID:
			case HASH:
			case INSENSITIVE:
			case INSERTED:
			case ISOLATION:
			case KEEPFIXED:
			case KEYSET:
			case LAST:
			case LEVEL:
			case LOCAL:
			case LOCK_ESCALATION:
			case LOGIN:
			case LOOP:
			case MARK:
			case MAX:
			case MIN:
			case MODIFY:
			case NEXT:
			case NAME:
			case NOCOUNT:
			case NOEXPAND:
			case NORECOMPUTE:
			case NTILE:
			case NUMBER:
			case OFFSET:
			case ONLY:
			case OPTIMISTIC:
			case OPTIMIZE:
			case OUT:
			case OUTPUT:
			case OWNER:
			case PARTITION:
			case PATH:
			case PRECEDING:
			case PRIOR:
			case RANGE:
			case RANK:
			case READONLY:
			case READ_ONLY:
			case RECOMPILE:
			case RELATIVE:
			case REMOTE:
			case REPEATABLE:
			case ROOT:
			case ROW:
			case ROWGUID:
			case ROWS:
			case ROW_NUMBER:
			case SAMPLE:
			case SCHEMABINDING:
			case SCROLL:
			case SCROLL_LOCKS:
			case SELF:
			case SERIALIZABLE:
			case SNAPSHOT:
			case SPATIAL_WINDOW_MAX_CELLS:
			case STATIC:
			case STATS_STREAM:
			case STDEV:
			case STDEVP:
			case SUM:
			case THROW:
			case TIES:
			case TIME:
			case TRY:
			case TYPE:
			case TYPE_WARNING:
			case UNBOUNDED:
			case UNCOMMITTED:
			case UNKNOWN:
			case USING:
			case VAR:
			case VARP:
			case VP_METADATA:
			case WORK:
			case XML:
			case XMLNAMESPACES:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(634);
				simple_id();
				}
				break;
			case DOUBLE_QUOTE_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(635);
				match(DOUBLE_QUOTE_ID);
				}
				break;
			case SQUARE_BRACKET_ID:
				enterOuterAlt(_localctx, 3);
				{
				setState(636);
				match(SQUARE_BRACKET_ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Simple_idContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(PqlParser.ID, 0); }
		public TerminalNode ABSOLUTE() { return getToken(PqlParser.ABSOLUTE, 0); }
		public TerminalNode APPLY() { return getToken(PqlParser.APPLY, 0); }
		public TerminalNode AUTO() { return getToken(PqlParser.AUTO, 0); }
		public TerminalNode AVG() { return getToken(PqlParser.AVG, 0); }
		public TerminalNode BASE64() { return getToken(PqlParser.BASE64, 0); }
		public TerminalNode CALLER() { return getToken(PqlParser.CALLER, 0); }
		public TerminalNode CAST() { return getToken(PqlParser.CAST, 0); }
		public TerminalNode CATCH() { return getToken(PqlParser.CATCH, 0); }
		public TerminalNode CHECKSUM_AGG() { return getToken(PqlParser.CHECKSUM_AGG, 0); }
		public TerminalNode COMMITTED() { return getToken(PqlParser.COMMITTED, 0); }
		public TerminalNode CONCAT() { return getToken(PqlParser.CONCAT, 0); }
		public TerminalNode COOKIE() { return getToken(PqlParser.COOKIE, 0); }
		public TerminalNode COUNT() { return getToken(PqlParser.COUNT, 0); }
		public TerminalNode COUNT_BIG() { return getToken(PqlParser.COUNT_BIG, 0); }
		public TerminalNode DELAY() { return getToken(PqlParser.DELAY, 0); }
		public TerminalNode DELETED() { return getToken(PqlParser.DELETED, 0); }
		public TerminalNode DENSE_RANK() { return getToken(PqlParser.DENSE_RANK, 0); }
		public TerminalNode DISABLE() { return getToken(PqlParser.DISABLE, 0); }
		public TerminalNode DYNAMIC() { return getToken(PqlParser.DYNAMIC, 0); }
		public TerminalNode ENCRYPTION() { return getToken(PqlParser.ENCRYPTION, 0); }
		public TerminalNode FAST() { return getToken(PqlParser.FAST, 0); }
		public TerminalNode FAST_FORWARD() { return getToken(PqlParser.FAST_FORWARD, 0); }
		public TerminalNode FIRST() { return getToken(PqlParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(PqlParser.FOLLOWING, 0); }
		public TerminalNode FORCESEEK() { return getToken(PqlParser.FORCESEEK, 0); }
		public TerminalNode FORWARD_ONLY() { return getToken(PqlParser.FORWARD_ONLY, 0); }
		public TerminalNode FULLSCAN() { return getToken(PqlParser.FULLSCAN, 0); }
		public TerminalNode GLOBAL() { return getToken(PqlParser.GLOBAL, 0); }
		public TerminalNode GO() { return getToken(PqlParser.GO, 0); }
		public TerminalNode GROUPING() { return getToken(PqlParser.GROUPING, 0); }
		public TerminalNode GROUPING_ID() { return getToken(PqlParser.GROUPING_ID, 0); }
		public TerminalNode HASH() { return getToken(PqlParser.HASH, 0); }
		public TerminalNode INSENSITIVE() { return getToken(PqlParser.INSENSITIVE, 0); }
		public TerminalNode INSERTED() { return getToken(PqlParser.INSERTED, 0); }
		public TerminalNode ISOLATION() { return getToken(PqlParser.ISOLATION, 0); }
		public TerminalNode KEYSET() { return getToken(PqlParser.KEYSET, 0); }
		public TerminalNode KEEPFIXED() { return getToken(PqlParser.KEEPFIXED, 0); }
		public TerminalNode LAST() { return getToken(PqlParser.LAST, 0); }
		public TerminalNode LEVEL() { return getToken(PqlParser.LEVEL, 0); }
		public TerminalNode LOCAL() { return getToken(PqlParser.LOCAL, 0); }
		public TerminalNode LOCK_ESCALATION() { return getToken(PqlParser.LOCK_ESCALATION, 0); }
		public TerminalNode LOGIN() { return getToken(PqlParser.LOGIN, 0); }
		public TerminalNode LOOP() { return getToken(PqlParser.LOOP, 0); }
		public TerminalNode MARK() { return getToken(PqlParser.MARK, 0); }
		public TerminalNode MAX() { return getToken(PqlParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(PqlParser.MIN, 0); }
		public TerminalNode MODIFY() { return getToken(PqlParser.MODIFY, 0); }
		public TerminalNode NAME() { return getToken(PqlParser.NAME, 0); }
		public TerminalNode NEXT() { return getToken(PqlParser.NEXT, 0); }
		public TerminalNode NOCOUNT() { return getToken(PqlParser.NOCOUNT, 0); }
		public TerminalNode NOEXPAND() { return getToken(PqlParser.NOEXPAND, 0); }
		public TerminalNode NORECOMPUTE() { return getToken(PqlParser.NORECOMPUTE, 0); }
		public TerminalNode NTILE() { return getToken(PqlParser.NTILE, 0); }
		public TerminalNode NUMBER() { return getToken(PqlParser.NUMBER, 0); }
		public TerminalNode OFFSET() { return getToken(PqlParser.OFFSET, 0); }
		public TerminalNode ONLY() { return getToken(PqlParser.ONLY, 0); }
		public TerminalNode OPTIMISTIC() { return getToken(PqlParser.OPTIMISTIC, 0); }
		public TerminalNode OPTIMIZE() { return getToken(PqlParser.OPTIMIZE, 0); }
		public TerminalNode OUT() { return getToken(PqlParser.OUT, 0); }
		public TerminalNode OUTPUT() { return getToken(PqlParser.OUTPUT, 0); }
		public TerminalNode OWNER() { return getToken(PqlParser.OWNER, 0); }
		public TerminalNode PARTITION() { return getToken(PqlParser.PARTITION, 0); }
		public TerminalNode PATH() { return getToken(PqlParser.PATH, 0); }
		public TerminalNode PRECEDING() { return getToken(PqlParser.PRECEDING, 0); }
		public TerminalNode PRIOR() { return getToken(PqlParser.PRIOR, 0); }
		public TerminalNode RANGE() { return getToken(PqlParser.RANGE, 0); }
		public TerminalNode RANK() { return getToken(PqlParser.RANK, 0); }
		public TerminalNode READONLY() { return getToken(PqlParser.READONLY, 0); }
		public TerminalNode READ_ONLY() { return getToken(PqlParser.READ_ONLY, 0); }
		public TerminalNode RECOMPILE() { return getToken(PqlParser.RECOMPILE, 0); }
		public TerminalNode RELATIVE() { return getToken(PqlParser.RELATIVE, 0); }
		public TerminalNode REMOTE() { return getToken(PqlParser.REMOTE, 0); }
		public TerminalNode REPEATABLE() { return getToken(PqlParser.REPEATABLE, 0); }
		public TerminalNode ROOT() { return getToken(PqlParser.ROOT, 0); }
		public TerminalNode ROW() { return getToken(PqlParser.ROW, 0); }
		public TerminalNode ROWGUID() { return getToken(PqlParser.ROWGUID, 0); }
		public TerminalNode ROWS() { return getToken(PqlParser.ROWS, 0); }
		public TerminalNode ROW_NUMBER() { return getToken(PqlParser.ROW_NUMBER, 0); }
		public TerminalNode SAMPLE() { return getToken(PqlParser.SAMPLE, 0); }
		public TerminalNode SCHEMABINDING() { return getToken(PqlParser.SCHEMABINDING, 0); }
		public TerminalNode SCROLL() { return getToken(PqlParser.SCROLL, 0); }
		public TerminalNode SCROLL_LOCKS() { return getToken(PqlParser.SCROLL_LOCKS, 0); }
		public TerminalNode SELF() { return getToken(PqlParser.SELF, 0); }
		public TerminalNode SERIALIZABLE() { return getToken(PqlParser.SERIALIZABLE, 0); }
		public TerminalNode SNAPSHOT() { return getToken(PqlParser.SNAPSHOT, 0); }
		public TerminalNode SPATIAL_WINDOW_MAX_CELLS() { return getToken(PqlParser.SPATIAL_WINDOW_MAX_CELLS, 0); }
		public TerminalNode STATIC() { return getToken(PqlParser.STATIC, 0); }
		public TerminalNode STATS_STREAM() { return getToken(PqlParser.STATS_STREAM, 0); }
		public TerminalNode STDEV() { return getToken(PqlParser.STDEV, 0); }
		public TerminalNode STDEVP() { return getToken(PqlParser.STDEVP, 0); }
		public TerminalNode SUM() { return getToken(PqlParser.SUM, 0); }
		public TerminalNode THROW() { return getToken(PqlParser.THROW, 0); }
		public TerminalNode TIES() { return getToken(PqlParser.TIES, 0); }
		public TerminalNode TIME() { return getToken(PqlParser.TIME, 0); }
		public TerminalNode TRY() { return getToken(PqlParser.TRY, 0); }
		public TerminalNode TYPE() { return getToken(PqlParser.TYPE, 0); }
		public TerminalNode TYPE_WARNING() { return getToken(PqlParser.TYPE_WARNING, 0); }
		public TerminalNode UNBOUNDED() { return getToken(PqlParser.UNBOUNDED, 0); }
		public TerminalNode UNCOMMITTED() { return getToken(PqlParser.UNCOMMITTED, 0); }
		public TerminalNode UNKNOWN() { return getToken(PqlParser.UNKNOWN, 0); }
		public TerminalNode USING() { return getToken(PqlParser.USING, 0); }
		public TerminalNode VAR() { return getToken(PqlParser.VAR, 0); }
		public TerminalNode VARP() { return getToken(PqlParser.VARP, 0); }
		public TerminalNode VP_METADATA() { return getToken(PqlParser.VP_METADATA, 0); }
		public TerminalNode WORK() { return getToken(PqlParser.WORK, 0); }
		public TerminalNode XML() { return getToken(PqlParser.XML, 0); }
		public TerminalNode XMLNAMESPACES() { return getToken(PqlParser.XMLNAMESPACES, 0); }
		public Simple_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterSimple_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitSimple_id(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitSimple_id(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Simple_idContext simple_id() throws RecognitionException {
		Simple_idContext _localctx = new Simple_idContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_simple_id);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(639);
			_la = _input.LA(1);
			if ( !(_la==FORCESEEK || ((((_la - 193)) & ~0x3f) == 0 && ((1L << (_la - 193)) & ((1L << (ABSOLUTE - 193)) | (1L << (APPLY - 193)) | (1L << (AUTO - 193)) | (1L << (AVG - 193)) | (1L << (BASE64 - 193)) | (1L << (CALLER - 193)) | (1L << (CAST - 193)) | (1L << (CATCH - 193)) | (1L << (CHECKSUM_AGG - 193)) | (1L << (COMMITTED - 193)) | (1L << (CONCAT - 193)) | (1L << (COOKIE - 193)) | (1L << (COUNT - 193)) | (1L << (COUNT_BIG - 193)) | (1L << (DELAY - 193)) | (1L << (DELETED - 193)) | (1L << (DENSE_RANK - 193)) | (1L << (DISABLE - 193)) | (1L << (DYNAMIC - 193)) | (1L << (ENCRYPTION - 193)) | (1L << (FAST - 193)) | (1L << (FAST_FORWARD - 193)) | (1L << (FIRST - 193)) | (1L << (FOLLOWING - 193)) | (1L << (FORWARD_ONLY - 193)) | (1L << (FULLSCAN - 193)) | (1L << (GLOBAL - 193)) | (1L << (GO - 193)) | (1L << (GROUPING - 193)) | (1L << (GROUPING_ID - 193)) | (1L << (HASH - 193)) | (1L << (INSENSITIVE - 193)) | (1L << (INSERTED - 193)) | (1L << (ISOLATION - 193)) | (1L << (KEEPFIXED - 193)) | (1L << (KEYSET - 193)) | (1L << (LAST - 193)) | (1L << (LEVEL - 193)) | (1L << (LOCAL - 193)) | (1L << (LOCK_ESCALATION - 193)) | (1L << (LOGIN - 193)) | (1L << (LOOP - 193)) | (1L << (MARK - 193)) | (1L << (MAX - 193)) | (1L << (MIN - 193)) | (1L << (MODIFY - 193)) | (1L << (NEXT - 193)) | (1L << (NAME - 193)) | (1L << (NOCOUNT - 193)) | (1L << (NOEXPAND - 193)) | (1L << (NORECOMPUTE - 193)) | (1L << (NTILE - 193)) | (1L << (NUMBER - 193)) | (1L << (OFFSET - 193)) | (1L << (ONLY - 193)) | (1L << (OPTIMISTIC - 193)) | (1L << (OPTIMIZE - 193)))) != 0) || ((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & ((1L << (OUT - 257)) | (1L << (OUTPUT - 257)) | (1L << (OWNER - 257)) | (1L << (PARTITION - 257)) | (1L << (PATH - 257)) | (1L << (PRECEDING - 257)) | (1L << (PRIOR - 257)) | (1L << (RANGE - 257)) | (1L << (RANK - 257)) | (1L << (READONLY - 257)) | (1L << (READ_ONLY - 257)) | (1L << (RECOMPILE - 257)) | (1L << (RELATIVE - 257)) | (1L << (REMOTE - 257)) | (1L << (REPEATABLE - 257)) | (1L << (ROOT - 257)) | (1L << (ROW - 257)) | (1L << (ROWGUID - 257)) | (1L << (ROWS - 257)) | (1L << (ROW_NUMBER - 257)) | (1L << (SAMPLE - 257)) | (1L << (SCHEMABINDING - 257)) | (1L << (SCROLL - 257)) | (1L << (SCROLL_LOCKS - 257)) | (1L << (SELF - 257)) | (1L << (SERIALIZABLE - 257)) | (1L << (SNAPSHOT - 257)) | (1L << (SPATIAL_WINDOW_MAX_CELLS - 257)) | (1L << (STATIC - 257)) | (1L << (STATS_STREAM - 257)) | (1L << (STDEV - 257)) | (1L << (STDEVP - 257)) | (1L << (SUM - 257)) | (1L << (THROW - 257)) | (1L << (TIES - 257)) | (1L << (TIME - 257)) | (1L << (TRY - 257)) | (1L << (TYPE - 257)) | (1L << (TYPE_WARNING - 257)) | (1L << (UNBOUNDED - 257)) | (1L << (UNCOMMITTED - 257)) | (1L << (UNKNOWN - 257)) | (1L << (USING - 257)) | (1L << (VAR - 257)) | (1L << (VARP - 257)) | (1L << (VP_METADATA - 257)) | (1L << (WORK - 257)) | (1L << (XML - 257)) | (1L << (XMLNAMESPACES - 257)) | (1L << (ID - 257)))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Comparison_operatorContext extends ParserRuleContext {
		public Comparison_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterComparison_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitComparison_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitComparison_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Comparison_operatorContext comparison_operator() throws RecognitionException {
		Comparison_operatorContext _localctx = new Comparison_operatorContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_comparison_operator);
		try {
			setState(658);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(641);
				match(T__0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(642);
				match(COLON);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(643);
				match(EQUAL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(644);
				match(GREATER);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(645);
				match(LESS);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(646);
				match(LESS);
				setState(647);
				match(EQUAL);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(648);
				match(GREATER);
				setState(649);
				match(EQUAL);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(650);
				match(LESS);
				setState(651);
				match(GREATER);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(652);
				match(EXCLAMATION);
				setState(653);
				match(EQUAL);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(654);
				match(EXCLAMATION);
				setState(655);
				match(GREATER);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(656);
				match(EXCLAMATION);
				setState(657);
				match(LESS);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Assignment_operatorContext extends ParserRuleContext {
		public Assignment_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).enterAssignment_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PqlListener ) ((PqlListener)listener).exitAssignment_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PqlVisitor ) return ((PqlVisitor<? extends T>)visitor).visitAssignment_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Assignment_operatorContext assignment_operator() throws RecognitionException {
		Assignment_operatorContext _localctx = new Assignment_operatorContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_assignment_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(660);
			_la = _input.LA(1);
			if ( !(((((_la - 322)) & ~0x3f) == 0 && ((1L << (_la - 322)) & ((1L << (PLUS_ASSIGN - 322)) | (1L << (MINUS_ASSIGN - 322)) | (1L << (MULT_ASSIGN - 322)) | (1L << (DIV_ASSIGN - 322)) | (1L << (MOD_ASSIGN - 322)) | (1L << (AND_ASSIGN - 322)) | (1L << (XOR_ASSIGN - 322)) | (1L << (OR_ASSIGN - 322)))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 9:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 2);
		case 2:
			return precpred(_ctx, 1);
		case 3:
			return precpred(_ctx, 9);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\u015e\u0299\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\3\2\7\2p\n\2\f\2\16\2s\13\2\3\2\3\2"+
		"\3\3\3\3\5\3y\n\3\3\4\3\4\3\5\3\5\3\5\5\5\u0080\n\5\3\6\3\6\5\6\u0084"+
		"\n\6\3\6\5\6\u0087\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\7\7\u0090\n\7\f\7\16"+
		"\7\u0093\13\7\3\7\3\7\5\7\u0097\n\7\3\7\3\7\3\7\3\7\7\7\u009d\n\7\f\7"+
		"\16\7\u00a0\13\7\5\7\u00a2\n\7\3\7\3\7\3\7\3\7\3\7\5\7\u00a9\n\7\3\7\5"+
		"\7\u00ac\n\7\3\b\3\b\3\t\3\t\3\t\3\t\5\t\u00b4\n\t\3\t\3\t\5\t\u00b8\n"+
		"\t\3\n\3\n\3\n\3\n\5\n\u00be\n\n\3\n\3\n\3\n\7\n\u00c3\n\n\f\n\16\n\u00c6"+
		"\13\n\3\n\5\n\u00c9\n\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u00dc\n\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\7\13\u00eb\n\13\f\13"+
		"\16\13\u00ee\13\13\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u00f6\n\f\3\r\3\r\3\16"+
		"\3\16\3\16\7\16\u00fd\n\16\f\16\16\16\u0100\13\16\3\17\3\17\3\17\7\17"+
		"\u0105\n\17\f\17\16\17\u0108\13\17\3\20\3\20\3\20\7\20\u010d\n\20\f\20"+
		"\16\20\u0110\13\20\3\21\5\21\u0113\n\21\3\21\3\21\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\5\22\u0129\n\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u0132\n\22\3"+
		"\22\3\22\3\22\3\22\5\22\u0138\n\22\3\22\3\22\3\22\3\22\5\22\u013e\n\22"+
		"\3\22\3\22\3\22\3\22\5\22\u0144\n\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\5\22\u014e\n\22\3\23\3\23\3\23\3\23\3\23\5\23\u0155\n\23\3\24\3"+
		"\24\3\24\3\24\5\24\u015b\n\24\3\24\3\24\3\24\3\24\7\24\u0161\n\24\f\24"+
		"\16\24\u0164\13\24\5\24\u0166\n\24\3\24\3\24\5\24\u016a\n\24\3\24\3\24"+
		"\3\24\3\24\3\24\7\24\u0171\n\24\f\24\16\24\u0174\13\24\5\24\u0176\n\24"+
		"\3\24\3\24\5\24\u017a\n\24\3\24\3\24\3\24\5\24\u017f\n\24\3\25\3\25\3"+
		"\25\3\25\3\25\7\25\u0186\n\25\f\25\16\25\u0189\13\25\3\25\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\5\25\u0194\n\25\5\25\u0196\n\25\3\26\3\26"+
		"\5\26\u019a\n\26\3\27\3\27\3\30\3\30\3\31\3\31\3\31\7\31\u01a3\n\31\f"+
		"\31\16\31\u01a6\13\31\3\32\3\32\3\32\5\32\u01ab\n\32\3\32\3\32\3\32\5"+
		"\32\u01b0\n\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u01b8\n\32\3\32\5\32"+
		"\u01bb\n\32\5\32\u01bd\n\32\3\33\3\33\3\33\3\33\3\33\5\33\u01c4\n\33\3"+
		"\34\3\34\7\34\u01c8\n\34\f\34\16\34\u01cb\13\34\3\35\3\35\5\35\u01cf\n"+
		"\35\3\36\5\36\u01d2\n\36\3\36\3\36\5\36\u01d6\n\36\5\36\u01d8\n\36\3\36"+
		"\5\36\u01db\n\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36"+
		"\3\36\3\36\3\36\5\36\u01eb\n\36\3\37\3\37\5\37\u01ef\n\37\3 \5 \u01f2"+
		"\n \3 \3 \3!\3!\5!\u01f8\n!\3!\3!\5!\u01fc\n!\5!\u01fe\n!\3\"\3\"\5\""+
		"\u0202\n\"\3\"\3\"\3#\5#\u0207\n#\3#\3#\3#\3#\7#\u020d\n#\f#\16#\u0210"+
		"\13#\3#\3#\3$\3$\3%\3%\3%\3%\7%\u021a\n%\f%\16%\u021d\13%\3%\3%\3&\3&"+
		"\5&\u0223\n&\3\'\3\'\3\'\7\'\u0228\n\'\f\'\16\'\u022b\13\'\3(\3(\3)\3"+
		")\3)\7)\u0232\n)\f)\16)\u0235\13)\3*\3*\3+\3+\3+\7+\u023c\n+\f+\16+\u023f"+
		"\13+\3,\3,\3,\5,\u0244\n,\3,\3,\3-\3-\3-\7-\u024b\n-\f-\16-\u024e\13-"+
		"\3.\3.\3.\5.\u0253\n.\3.\3.\5.\u0257\n.\3.\5.\u025a\n.\3.\5.\u025d\n."+
		"\3/\5/\u0260\n/\3/\3/\3\60\3\60\5\60\u0266\n\60\3\61\3\61\3\61\3\61\5"+
		"\61\u026c\n\61\3\61\3\61\5\61\u0270\n\61\3\61\3\61\5\61\u0274\n\61\3\62"+
		"\5\62\u0277\n\62\3\62\3\62\3\63\3\63\3\64\3\64\3\64\5\64\u0280\n\64\3"+
		"\65\3\65\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3"+
		"\66\3\66\3\66\3\66\3\66\5\66\u0295\n\66\3\67\3\67\3\67\2\3\248\2\4\6\b"+
		"\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVX"+
		"Z\\^`bdfhjl\2\23\5\2\u00dc\u00dc\u0118\u0118\u0130\u0130\3\2\u0159\u015a"+
		"\3\2\u0156\u0158\4\2\u0159\u015a\u015c\u015e\5\2\6\6\t\t\u00a5\u00a5\4"+
		"\2\u0113\u0113\u0115\u0115\4\2\u00df\u00df\u00f8\u00f8\4\2\13\13\65\65"+
		"\4\2\5\5\\\\\4\2VV\u0114\u0114\5\2OOee\u0095\u0095\6\2jj\u00e7\u00e7\u00f2"+
		"\u00f2\u0110\u0110\4\2iirr\3\2\u013e\u013f\4\2\u013a\u013a\u013e\u013e"+
		"\t\2JJ\u00c3\u00c7\u00c9\u00cb\u00cd\u00d2\u00d7\u00f5\u00f7\u0133\u013b"+
		"\u013b\3\2\u0144\u014b\u02d6\2q\3\2\2\2\4x\3\2\2\2\6z\3\2\2\2\b\177\3"+
		"\2\2\2\n\u0081\3\2\2\2\f\u0088\3\2\2\2\16\u00ad\3\2\2\2\20\u00af\3\2\2"+
		"\2\22\u00b9\3\2\2\2\24\u00db\3\2\2\2\26\u00f5\3\2\2\2\30\u00f7\3\2\2\2"+
		"\32\u00f9\3\2\2\2\34\u0101\3\2\2\2\36\u0109\3\2\2\2 \u0112\3\2\2\2\"\u014d"+
		"\3\2\2\2$\u0154\3\2\2\2&\u0156\3\2\2\2(\u0180\3\2\2\2*\u0197\3\2\2\2,"+
		"\u019b\3\2\2\2.\u019d\3\2\2\2\60\u019f\3\2\2\2\62\u01bc\3\2\2\2\64\u01c3"+
		"\3\2\2\2\66\u01c5\3\2\2\28\u01cc\3\2\2\2:\u01ea\3\2\2\2<\u01ec\3\2\2\2"+
		">\u01f1\3\2\2\2@\u01fd\3\2\2\2B\u0201\3\2\2\2D\u0206\3\2\2\2F\u0213\3"+
		"\2\2\2H\u0215\3\2\2\2J\u0222\3\2\2\2L\u0224\3\2\2\2N\u022c\3\2\2\2P\u022e"+
		"\3\2\2\2R\u0236\3\2\2\2T\u0238\3\2\2\2V\u0243\3\2\2\2X\u0247\3\2\2\2Z"+
		"\u025c\3\2\2\2\\\u025f\3\2\2\2^\u0265\3\2\2\2`\u0273\3\2\2\2b\u0276\3"+
		"\2\2\2d\u027a\3\2\2\2f\u027f\3\2\2\2h\u0281\3\2\2\2j\u0294\3\2\2\2l\u0296"+
		"\3\2\2\2np\5\4\3\2on\3\2\2\2ps\3\2\2\2qo\3\2\2\2qr\3\2\2\2rt\3\2\2\2s"+
		"q\3\2\2\2tu\7\2\2\3u\3\3\2\2\2vy\5\6\4\2wy\5\b\5\2xv\3\2\2\2xw\3\2\2\2"+
		"y\5\3\2\2\2z{\5\n\6\2{\7\3\2\2\2|\u0080\5\f\7\2}\u0080\5\20\t\2~\u0080"+
		"\5\22\n\2\177|\3\2\2\2\177}\3\2\2\2\177~\3\2\2\2\u0080\t\3\2\2\2\u0081"+
		"\u0083\5$\23\2\u0082\u0084\5(\25\2\u0083\u0082\3\2\2\2\u0083\u0084\3\2"+
		"\2\2\u0084\u0086\3\2\2\2\u0085\u0087\7\u0154\2\2\u0086\u0085\3\2\2\2\u0086"+
		"\u0087\3\2\2\2\u0087\13\3\2\2\2\u0088\u0089\7&\2\2\u0089\u008a\7\u00bb"+
		"\2\2\u008a\u0096\5T+\2\u008b\u008c\7\u0151\2\2\u008c\u0091\5Z.\2\u008d"+
		"\u008e\7\u0153\2\2\u008e\u0090\5Z.\2\u008f\u008d\3\2\2\2\u0090\u0093\3"+
		"\2\2\2\u0091\u008f\3\2\2\2\u0091\u0092\3\2\2\2\u0092\u0094\3\2\2\2\u0093"+
		"\u0091\3\2\2\2\u0094\u0095\7\u0152\2\2\u0095\u0097\3\2\2\2\u0096\u008b"+
		"\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u00a1\3\2\2\2\u0098\u0099\7\u00c0\2"+
		"\2\u0099\u009e\5\16\b\2\u009a\u009b\7\u0153\2\2\u009b\u009d\5\16\b\2\u009c"+
		"\u009a\3\2\2\2\u009d\u00a0\3\2\2\2\u009e\u009c\3\2\2\2\u009e\u009f\3\2"+
		"\2\2\u009f\u00a2\3\2\2\2\u00a0\u009e\3\2\2\2\u00a1\u0098\3\2\2\2\u00a1"+
		"\u00a2\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a4\7\n\2\2\u00a4\u00a8\5\n"+
		"\6\2\u00a5\u00a6\7\u00c0\2\2\u00a6\u00a7\7\30\2\2\u00a7\u00a9\7|\2\2\u00a8"+
		"\u00a5\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00ab\3\2\2\2\u00aa\u00ac\7\u0154"+
		"\2\2\u00ab\u00aa\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac\r\3\2\2\2\u00ad\u00ae"+
		"\t\2\2\2\u00ae\17\3\2\2\2\u00af\u00b0\7:\2\2\u00b0\u00b3\7\u0107\2\2\u00b1"+
		"\u00b2\7Y\2\2\u00b2\u00b4\7C\2\2\u00b3\u00b1\3\2\2\2\u00b3\u00b4\3\2\2"+
		"\2\u00b4\u00b5\3\2\2\2\u00b5\u00b7\5P)\2\u00b6\u00b8\7\u0154\2\2\u00b7"+
		"\u00b6\3\2\2\2\u00b7\u00b8\3\2\2\2\u00b8\21\3\2\2\2\u00b9\u00ba\7:\2\2"+
		"\u00ba\u00bd\7\u00bb\2\2\u00bb\u00bc\7Y\2\2\u00bc\u00be\7C\2\2\u00bd\u00bb"+
		"\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\u00bf\3\2\2\2\u00bf\u00c4\5T+\2\u00c0"+
		"\u00c1\7\u0153\2\2\u00c1\u00c3\5T+\2\u00c2\u00c0\3\2\2\2\u00c3\u00c6\3"+
		"\2\2\2\u00c4\u00c2\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00c8\3\2\2\2\u00c6"+
		"\u00c4\3\2\2\2\u00c7\u00c9\7\u0154\2\2\u00c8\u00c7\3\2\2\2\u00c8\u00c9"+
		"\3\2\2\2\u00c9\23\3\2\2\2\u00ca\u00cb\b\13\1\2\u00cb\u00cc\7\u015b\2\2"+
		"\u00cc\u00dc\5\24\13\7\u00cd\u00ce\t\3\2\2\u00ce\u00dc\5\24\13\5\u00cf"+
		"\u00dc\7\62\2\2\u00d0\u00dc\7p\2\2\u00d1\u00dc\5`\61\2\u00d2\u00dc\5V"+
		",\2\u00d3\u00d4\7\u0151\2\2\u00d4\u00d5\5\24\13\2\u00d5\u00d6\7\u0152"+
		"\2\2\u00d6\u00dc\3\2\2\2\u00d7\u00d8\7\u0151\2\2\u00d8\u00d9\5\30\r\2"+
		"\u00d9\u00da\7\u0152\2\2\u00da\u00dc\3\2\2\2\u00db\u00ca\3\2\2\2\u00db"+
		"\u00cd\3\2\2\2\u00db\u00cf\3\2\2\2\u00db\u00d0\3\2\2\2\u00db\u00d1\3\2"+
		"\2\2\u00db\u00d2\3\2\2\2\u00db\u00d3\3\2\2\2\u00db\u00d7\3\2\2\2\u00dc"+
		"\u00ec\3\2\2\2\u00dd\u00de\f\6\2\2\u00de\u00df\t\4\2\2\u00df\u00eb\5\24"+
		"\13\7\u00e0\u00e1\f\4\2\2\u00e1\u00e2\t\5\2\2\u00e2\u00eb\5\24\13\5\u00e3"+
		"\u00e4\f\3\2\2\u00e4\u00e5\5j\66\2\u00e5\u00e6\5\24\13\4\u00e6\u00eb\3"+
		"\2\2\2\u00e7\u00e8\f\13\2\2\u00e8\u00e9\7\35\2\2\u00e9\u00eb\5f\64\2\u00ea"+
		"\u00dd\3\2\2\2\u00ea\u00e0\3\2\2\2\u00ea\u00e3\3\2\2\2\u00ea\u00e7\3\2"+
		"\2\2\u00eb\u00ee\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed"+
		"\25\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ef\u00f6\7p\2\2\u00f0\u00f6\5`\61\2"+
		"\u00f1\u00f2\7\u0151\2\2\u00f2\u00f3\5\26\f\2\u00f3\u00f4\7\u0152\2\2"+
		"\u00f4\u00f6\3\2\2\2\u00f5\u00ef\3\2\2\2\u00f5\u00f0\3\2\2\2\u00f5\u00f1"+
		"\3\2\2\2\u00f6\27\3\2\2\2\u00f7\u00f8\5\n\6\2\u00f8\31\3\2\2\2\u00f9\u00fe"+
		"\5\34\17\2\u00fa\u00fb\7\u0153\2\2\u00fb\u00fd\5\34\17\2\u00fc\u00fa\3"+
		"\2\2\2\u00fd\u0100\3\2\2\2\u00fe\u00fc\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff"+
		"\33\3\2\2\2\u0100\u00fe\3\2\2\2\u0101\u0106\5\36\20\2\u0102\u0103\7}\2"+
		"\2\u0103\u0105\5\36\20\2\u0104\u0102\3\2\2\2\u0105\u0108\3\2\2\2\u0106"+
		"\u0104\3\2\2\2\u0106\u0107\3\2\2\2\u0107\35\3\2\2\2\u0108\u0106\3\2\2"+
		"\2\u0109\u010e\5 \21\2\u010a\u010b\7\b\2\2\u010b\u010d\5 \21\2\u010c\u010a"+
		"\3\2\2\2\u010d\u0110\3\2\2\2\u010e\u010c\3\2\2\2\u010e\u010f\3\2\2\2\u010f"+
		"\37\3\2\2\2\u0110\u010e\3\2\2\2\u0111\u0113\7o\2\2\u0112\u0111\3\2\2\2"+
		"\u0112\u0113\3\2\2\2\u0113\u0114\3\2\2\2\u0114\u0115\5\"\22\2\u0115!\3"+
		"\2\2\2\u0116\u0117\7C\2\2\u0117\u0118\7\u0151\2\2\u0118\u0119\5\30\r\2"+
		"\u0119\u011a\7\u0152\2\2\u011a\u014e\3\2\2\2\u011b\u011c\5\24\13\2\u011c"+
		"\u011d\5j\66\2\u011d\u011e\5\24\13\2\u011e\u014e\3\2\2\2\u011f\u0120\5"+
		"\24\13\2\u0120\u0121\5j\66\2\u0121\u0122\t\6\2\2\u0122\u0123\7\u0151\2"+
		"\2\u0123\u0124\5\30\r\2\u0124\u0125\7\u0152\2\2\u0125\u014e\3\2\2\2\u0126"+
		"\u0128\5\24\13\2\u0127\u0129\7o\2\2\u0128\u0127\3\2\2\2\u0128\u0129\3"+
		"\2\2\2\u0129\u012a\3\2\2\2\u012a\u012b\7\17\2\2\u012b\u012c\5\24\13\2"+
		"\u012c\u012d\7\b\2\2\u012d\u012e\5\24\13\2\u012e\u014e\3\2\2\2\u012f\u0131"+
		"\5\24\13\2\u0130\u0132\7o\2\2\u0131\u0130\3\2\2\2\u0131\u0132\3\2\2\2"+
		"\u0132\u0133\3\2\2\2\u0133\u0134\7Z\2\2\u0134\u0137\7\u0151\2\2\u0135"+
		"\u0138\5\30\r\2\u0136\u0138\5L\'\2\u0137\u0135\3\2\2\2\u0137\u0136\3\2"+
		"\2\2\u0138\u0139\3\2\2\2\u0139\u013a\7\u0152\2\2\u013a\u014e\3\2\2\2\u013b"+
		"\u013d\5\24\13\2\u013c\u013e\7o\2\2\u013d\u013c\3\2\2\2\u013d\u013e\3"+
		"\2\2\2\u013e\u013f\3\2\2\2\u013f\u0140\7f\2\2\u0140\u0143\5\24\13\2\u0141"+
		"\u0142\7?\2\2\u0142\u0144\5\24\13\2\u0143\u0141\3\2\2\2\u0143\u0144\3"+
		"\2\2\2\u0144\u014e\3\2\2\2\u0145\u0146\5\24\13\2\u0146\u0147\7a\2\2\u0147"+
		"\u0148\5\\/\2\u0148\u014e\3\2\2\2\u0149\u014a\7\u0151\2\2\u014a\u014b"+
		"\5\34\17\2\u014b\u014c\7\u0152\2\2\u014c\u014e\3\2\2\2\u014d\u0116\3\2"+
		"\2\2\u014d\u011b\3\2\2\2\u014d\u011f\3\2\2\2\u014d\u0126\3\2\2\2\u014d"+
		"\u012f\3\2\2\2\u014d\u013b\3\2\2\2\u014d\u0145\3\2\2\2\u014d\u0149\3\2"+
		"\2\2\u014e#\3\2\2\2\u014f\u0155\5&\24\2\u0150\u0151\7\u0151\2\2\u0151"+
		"\u0152\5$\23\2\u0152\u0153\7\u0152\2\2\u0153\u0155\3\2\2\2\u0154\u014f"+
		"\3\2\2\2\u0154\u0150\3\2\2\2\u0155%\3\2\2\2\u0156\u0157\7\u009d\2\2\u0157"+
		"\u015a\5\60\31\2\u0158\u0159\7`\2\2\u0159\u015b\5N(\2\u015a\u0158\3\2"+
		"\2\2\u015a\u015b\3\2\2\2\u015b\u0165\3\2\2\2\u015c\u015d\7N\2\2\u015d"+
		"\u0162\5\64\33\2\u015e\u015f\7\u0153\2\2\u015f\u0161\5\64\33\2\u0160\u015e"+
		"\3\2\2\2\u0161\u0164\3\2\2\2\u0162\u0160\3\2\2\2\u0162\u0163\3\2\2\2\u0163"+
		"\u0166\3\2\2\2\u0164\u0162\3\2\2\2\u0165\u015c\3\2\2\2\u0165\u0166\3\2"+
		"\2\2\u0166\u0169\3\2\2\2\u0167\u0168\7\u00be\2\2\u0168\u016a\5\34\17\2"+
		"\u0169\u0167\3\2\2\2\u0169\u016a\3\2\2\2\u016a\u0175\3\2\2\2\u016b\u016c"+
		"\7S\2\2\u016c\u016d\7\23\2\2\u016d\u0172\5,\27\2\u016e\u016f\7\u0153\2"+
		"\2\u016f\u0171\5,\27\2\u0170\u016e\3\2\2\2\u0171\u0174\3\2\2\2\u0172\u0170"+
		"\3\2\2\2\u0172\u0173\3\2\2\2\u0173\u0176\3\2\2\2\u0174\u0172\3\2\2\2\u0175"+
		"\u016b\3\2\2\2\u0175\u0176\3\2\2\2\u0176\u0179\3\2\2\2\u0177\u0178\7U"+
		"\2\2\u0178\u017a\5\34\17\2\u0179\u0177\3\2\2\2\u0179\u017a\3\2\2\2\u017a"+
		"\u017e\3\2\2\2\u017b\u017c\7l\2\2\u017c\u017d\7\23\2\2\u017d\u017f\5."+
		"\30\2\u017e\u017b\3\2\2\2\u017e\u017f\3\2\2\2\u017f\'\3\2\2\2\u0180\u0181"+
		"\7~\2\2\u0181\u0182\7\23\2\2\u0182\u0187\5*\26\2\u0183\u0184\7\u0153\2"+
		"\2\u0184\u0186\5*\26\2\u0185\u0183\3\2\2\2\u0186\u0189\3\2\2\2\u0187\u0185"+
		"\3\2\2\2\u0187\u0188\3\2\2\2\u0188\u0195\3\2\2\2\u0189\u0187\3\2\2\2\u018a"+
		"\u018b\7\u00ff\2\2\u018b\u018c\5\24\13\2\u018c\u0193\t\7\2\2\u018d\u018e"+
		"\7F\2\2\u018e\u018f\t\b\2\2\u018f\u0190\5\24\13\2\u0190\u0191\t\7\2\2"+
		"\u0191\u0192\7\u0100\2\2\u0192\u0194\3\2\2\2\u0193\u018d\3\2\2\2\u0193"+
		"\u0194\3\2\2\2\u0194\u0196\3\2\2\2\u0195\u018a\3\2\2\2\u0195\u0196\3\2"+
		"\2\2\u0196)\3\2\2\2\u0197\u0199\5\24\13\2\u0198\u019a\t\t\2\2\u0199\u0198"+
		"\3\2\2\2\u0199\u019a\3\2\2\2\u019a+\3\2\2\2\u019b\u019c\5\24\13\2\u019c"+
		"-\3\2\2\2\u019d\u019e\t\n\2\2\u019e/\3\2\2\2\u019f\u01a4\5\62\32\2\u01a0"+
		"\u01a1\7\u0153\2\2\u01a1\u01a3\5\62\32\2\u01a2\u01a0\3\2\2\2\u01a3\u01a6"+
		"\3\2\2\2\u01a4\u01a2\3\2\2\2\u01a4\u01a5\3\2\2\2\u01a5\61\3\2\2\2\u01a6"+
		"\u01a4\3\2\2\2\u01a7\u01a8\5N(\2\u01a8\u01a9\7\u014c\2\2\u01a9\u01ab\3"+
		"\2\2\2\u01aa\u01a7\3\2\2\2\u01aa\u01ab\3\2\2\2\u01ab\u01af\3\2\2\2\u01ac"+
		"\u01b0\7\u0156\2\2\u01ad\u01ae\7\u0150\2\2\u01ae\u01b0\t\13\2\2\u01af"+
		"\u01ac\3\2\2\2\u01af\u01ad\3\2\2\2\u01b0\u01bd\3\2\2\2\u01b1\u01b2\5J"+
		"&\2\u01b2\u01b3\7\u0140\2\2\u01b3\u01b4\5\24\13\2\u01b4\u01bd\3\2\2\2"+
		"\u01b5\u01ba\5\24\13\2\u01b6\u01b8\7\n\2\2\u01b7\u01b6\3\2\2\2\u01b7\u01b8"+
		"\3\2\2\2\u01b8\u01b9\3\2\2\2\u01b9\u01bb\5J&\2\u01ba\u01b7\3\2\2\2\u01ba"+
		"\u01bb\3\2\2\2\u01bb\u01bd\3\2\2\2\u01bc\u01aa\3\2\2\2\u01bc\u01b1\3\2"+
		"\2\2\u01bc\u01b5\3\2\2\2\u01bd\63\3\2\2\2\u01be\u01c4\5\66\34\2\u01bf"+
		"\u01c0\7\u0151\2\2\u01c0\u01c1\5\66\34\2\u01c1\u01c2\7\u0152\2\2\u01c2"+
		"\u01c4\3\2\2\2\u01c3\u01be\3\2\2\2\u01c3\u01bf\3\2\2\2\u01c4\65\3\2\2"+
		"\2\u01c5\u01c9\58\35\2\u01c6\u01c8\5:\36\2\u01c7\u01c6\3\2\2\2\u01c8\u01cb"+
		"\3\2\2\2\u01c9\u01c7\3\2\2\2\u01c9\u01ca\3\2\2\2\u01ca\67\3\2\2\2\u01cb"+
		"\u01c9\3\2\2\2\u01cc\u01ce\5<\37\2\u01cd\u01cf\5> \2\u01ce\u01cd\3\2\2"+
		"\2\u01ce\u01cf\3\2\2\2\u01cf9\3\2\2\2\u01d0\u01d2\7]\2\2\u01d1\u01d0\3"+
		"\2\2\2\u01d1\u01d2\3\2\2\2\u01d2\u01d8\3\2\2\2\u01d3\u01d5\t\f\2\2\u01d4"+
		"\u01d6\7\177\2\2\u01d5\u01d4\3\2\2\2\u01d5\u01d6\3\2\2\2\u01d6\u01d8\3"+
		"\2\2\2\u01d7\u01d1\3\2\2\2\u01d7\u01d3\3\2\2\2\u01d8\u01da\3\2\2\2\u01d9"+
		"\u01db\t\r\2\2\u01da\u01d9\3\2\2\2\u01da\u01db\3\2\2\2\u01db\u01dc\3\2"+
		"\2\2\u01dc\u01dd\7b\2\2\u01dd\u01de\5\64\33\2\u01de\u01df\7v\2\2\u01df"+
		"\u01e0\5\34\17\2\u01e0\u01eb\3\2\2\2\u01e1\u01e2\7\'\2\2\u01e2\u01e3\7"+
		"b\2\2\u01e3\u01eb\5\64\33\2\u01e4\u01e5\7\'\2\2\u01e5\u01e6\7\u00c4\2"+
		"\2\u01e6\u01eb\5\64\33\2\u01e7\u01e8\7\177\2\2\u01e8\u01e9\7\u00c4\2\2"+
		"\u01e9\u01eb\5\64\33\2\u01ea\u01d7\3\2\2\2\u01ea\u01e1\3\2\2\2\u01ea\u01e4"+
		"\3\2\2\2\u01ea\u01e7\3\2\2\2\u01eb;\3\2\2\2\u01ec\u01ee\5P)\2\u01ed\u01ef"+
		"\5D#\2\u01ee\u01ed\3\2\2\2\u01ee\u01ef\3\2\2\2\u01ef=\3\2\2\2\u01f0\u01f2"+
		"\7\n\2\2\u01f1\u01f0\3\2\2\2\u01f1\u01f2\3\2\2\2\u01f2\u01f3\3\2\2\2\u01f3"+
		"\u01f4\5@!\2\u01f4?\3\2\2\2\u01f5\u01f7\5f\64\2\u01f6\u01f8\5B\"\2\u01f7"+
		"\u01f6\3\2\2\2\u01f7\u01f8\3\2\2\2\u01f8\u01fe\3\2\2\2\u01f9\u01fb\5f"+
		"\64\2\u01fa\u01fc\5D#\2\u01fb\u01fa\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc"+
		"\u01fe\3\2\2\2\u01fd\u01f5\3\2\2\2\u01fd\u01f9\3\2\2\2\u01feA\3\2\2\2"+
		"\u01ff\u0200\7\u00aa\2\2\u0200\u0202\t\16\2\2\u0201\u01ff\3\2\2\2\u0201"+
		"\u0202\3\2\2\2\u0202\u0203\3\2\2\2\u0203\u0204\5J&\2\u0204C\3\2\2\2\u0205"+
		"\u0207\7\u00c0\2\2\u0206\u0205\3\2\2\2\u0206\u0207\3\2\2\2\u0207\u0208"+
		"\3\2\2\2\u0208\u0209\7\u0151\2\2\u0209\u020e\5F$\2\u020a\u020b\7\u0153"+
		"\2\2\u020b\u020d\5F$\2\u020c\u020a\3\2\2\2\u020d\u0210\3\2\2\2\u020e\u020c"+
		"\3\2\2\2\u020e\u020f\3\2\2\2\u020f\u0211\3\2\2\2\u0210\u020e\3\2\2\2\u0211"+
		"\u0212\7\u0152\2\2\u0212E\3\2\2\2\u0213\u0214\7\u013b\2\2\u0214G\3\2\2"+
		"\2\u0215\u0216\7\u0151\2\2\u0216\u021b\5J&\2\u0217\u0218\7\u0153\2\2\u0218"+
		"\u021a\5J&\2\u0219\u0217\3\2\2\2\u021a\u021d\3\2\2\2\u021b\u0219\3\2\2"+
		"\2\u021b\u021c\3\2\2\2\u021c\u021e\3\2\2\2\u021d\u021b\3\2\2\2\u021e\u021f"+
		"\7\u0152\2\2\u021fI\3\2\2\2\u0220\u0223\5f\64\2\u0221\u0223\7\u013c\2"+
		"\2\u0222\u0220\3\2\2\2\u0222\u0221\3\2\2\2\u0223K\3\2\2\2\u0224\u0229"+
		"\5\24\13\2\u0225\u0226\7\u0153\2\2\u0226\u0228\5\24\13\2\u0227\u0225\3"+
		"\2\2\2\u0228\u022b\3\2\2\2\u0229\u0227\3\2\2\2\u0229\u022a\3\2\2\2\u022a"+
		"M\3\2\2\2\u022b\u0229\3\2\2\2\u022c\u022d\7\u013b\2\2\u022dO\3\2\2\2\u022e"+
		"\u0233\5N(\2\u022f\u0230\7\u0157\2\2\u0230\u0232\5N(\2\u0231\u022f\3\2"+
		"\2\2\u0232\u0235\3\2\2\2\u0233\u0231\3\2\2\2\u0233\u0234\3\2\2\2\u0234"+
		"Q\3\2\2\2\u0235\u0233\3\2\2\2\u0236\u0237\7\u013b\2\2\u0237S\3\2\2\2\u0238"+
		"\u023d\5R*\2\u0239\u023a\7\u0157\2\2\u023a\u023c\5R*\2\u023b\u0239\3\2"+
		"\2\2\u023c\u023f\3\2\2\2\u023d\u023b\3\2\2\2\u023d\u023e\3\2\2\2\u023e"+
		"U\3\2\2\2\u023f\u023d\3\2\2\2\u0240\u0241\5P)\2\u0241\u0242\7\u014c\2"+
		"\2\u0242\u0244\3\2\2\2\u0243\u0240\3\2\2\2\u0243\u0244\3\2\2\2\u0244\u0245"+
		"\3\2\2\2\u0245\u0246\5Z.\2\u0246W\3\2\2\2\u0247\u024c\5Z.\2\u0248\u0249"+
		"\7\u0153\2\2\u0249\u024b\5Z.\2\u024a\u0248\3\2\2\2\u024b\u024e\3\2\2\2"+
		"\u024c\u024a\3\2\2\2\u024c\u024d\3\2\2\2\u024dY\3\2\2\2\u024e\u024c\3"+
		"\2\2\2\u024f\u0250\5f\64\2\u0250\u0251\7\u014c\2\2\u0251\u0253\3\2\2\2"+
		"\u0252\u024f\3\2\2\2\u0252\u0253\3\2\2\2\u0253\u0254\3\2\2\2\u0254\u0256"+
		"\5f\64\2\u0255\u0257\5J&\2\u0256\u0255\3\2\2\2\u0256\u0257\3\2\2\2\u0257"+
		"\u025d\3\2\2\2\u0258\u025a\5@!\2\u0259\u0258\3\2\2\2\u0259\u025a\3\2\2"+
		"\2\u025a\u025b\3\2\2\2\u025b\u025d\5f\64\2\u025c\u0252\3\2\2\2\u025c\u0259"+
		"\3\2\2\2\u025d[\3\2\2\2\u025e\u0260\7o\2\2\u025f\u025e\3\2\2\2\u025f\u0260"+
		"\3\2\2\2\u0260\u0261\3\2\2\2\u0261\u0262\7p\2\2\u0262]\3\2\2\2\u0263\u0266"+
		"\7p\2\2\u0264\u0266\5`\61\2\u0265\u0263\3\2\2\2\u0265\u0264\3\2\2\2\u0266"+
		"_\3\2\2\2\u0267\u0274\7\u013c\2\2\u0268\u0274\7\u013d\2\2\u0269\u0274"+
		"\5b\62\2\u026a\u026c\5d\63\2\u026b\u026a\3\2\2\2\u026b\u026c\3\2\2\2\u026c"+
		"\u026d\3\2\2\2\u026d\u0274\t\17\2\2\u026e\u0270\5d\63\2\u026f\u026e\3"+
		"\2\2\2\u026f\u0270\3\2\2\2\u0270\u0271\3\2\2\2\u0271\u0272\7\u0150\2\2"+
		"\u0272\u0274\t\20\2\2\u0273\u0267\3\2\2\2\u0273\u0268\3\2\2\2\u0273\u0269"+
		"\3\2\2\2\u0273\u026b\3\2\2\2\u0273\u026f\3\2\2\2\u0274a\3\2\2\2\u0275"+
		"\u0277\5d\63\2\u0276\u0275\3\2\2\2\u0276\u0277\3\2\2\2\u0277\u0278\3\2"+
		"\2\2\u0278\u0279\7\u013a\2\2\u0279c\3\2\2\2\u027a\u027b\t\3\2\2\u027b"+
		"e\3\2\2\2\u027c\u0280\5h\65\2\u027d\u0280\7\u0138\2\2\u027e\u0280\7\u0139"+
		"\2\2\u027f\u027c\3\2\2\2\u027f\u027d\3\2\2\2\u027f\u027e\3\2\2\2\u0280"+
		"g\3\2\2\2\u0281\u0282\t\21\2\2\u0282i\3\2\2\2\u0283\u0295\7\3\2\2\u0284"+
		"\u0295\7\u0155\2\2\u0285\u0295\7\u0140\2\2\u0286\u0295\7\u0141\2\2\u0287"+
		"\u0295\7\u0142\2\2\u0288\u0289\7\u0142\2\2\u0289\u0295\7\u0140\2\2\u028a"+
		"\u028b\7\u0141\2\2\u028b\u0295\7\u0140\2\2\u028c\u028d\7\u0142\2\2\u028d"+
		"\u0295\7\u0141\2\2\u028e\u028f\7\u0143\2\2\u028f\u0295\7\u0140\2\2\u0290"+
		"\u0291\7\u0143\2\2\u0291\u0295\7\u0141\2\2\u0292\u0293\7\u0143\2\2\u0293"+
		"\u0295\7\u0142\2\2\u0294\u0283\3\2\2\2\u0294\u0284\3\2\2\2\u0294\u0285"+
		"\3\2\2\2\u0294\u0286\3\2\2\2\u0294\u0287\3\2\2\2\u0294\u0288\3\2\2\2\u0294"+
		"\u028a\3\2\2\2\u0294\u028c\3\2\2\2\u0294\u028e\3\2\2\2\u0294\u0290\3\2"+
		"\2\2\u0294\u0292\3\2\2\2\u0295k\3\2\2\2\u0296\u0297\t\22\2\2\u0297m\3"+
		"\2\2\2Vqx\177\u0083\u0086\u0091\u0096\u009e\u00a1\u00a8\u00ab\u00b3\u00b7"+
		"\u00bd\u00c4\u00c8\u00db\u00ea\u00ec\u00f5\u00fe\u0106\u010e\u0112\u0128"+
		"\u0131\u0137\u013d\u0143\u014d\u0154\u015a\u0162\u0165\u0169\u0172\u0175"+
		"\u0179\u017e\u0187\u0193\u0195\u0199\u01a4\u01aa\u01af\u01b7\u01ba\u01bc"+
		"\u01c3\u01c9\u01ce\u01d1\u01d5\u01d7\u01da\u01ea\u01ee\u01f1\u01f7\u01fb"+
		"\u01fd\u0201\u0206\u020e\u021b\u0222\u0229\u0233\u023d\u0243\u024c\u0252"+
		"\u0256\u0259\u025c\u025f\u0265\u026b\u026f\u0273\u0276\u027f\u0294";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}