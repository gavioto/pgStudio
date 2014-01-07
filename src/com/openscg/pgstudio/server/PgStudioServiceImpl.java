/*
 * PostgreSQL Studio
 * 
 * Copyright (c) 2013 - 2014, Open Source Consulting Group, Inc.
 * Copyright (c) 2012 - 2013, StormDB, Inc.
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without a written agreement is
 * hereby granted, provided that the above copyright notice and this paragraph and
 * the following two paragraphs appear in all copies.
 * 
 * IN NO EVENT SHALL OPEN SOURCE CONSULTING GROUP BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST
 * PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * OPEN SOURCE CONSULTING GROUP HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * OPEN SOURCE CONSULTING GROUP SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND
 * OPEN SOURCE CONSULTING GROUP HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 */
package com.openscg.pgstudio.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.openscg.pgstudio.client.PgStudio.DATABASE_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.INDEX_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.TYPE_FORM;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.server.models.Columns;
import com.openscg.pgstudio.server.models.Constraints;
import com.openscg.pgstudio.server.models.Database;
import com.openscg.pgstudio.server.models.ForeignTables;
import com.openscg.pgstudio.server.models.Functions;
import com.openscg.pgstudio.server.models.Indexes;
import com.openscg.pgstudio.server.models.ItemData;
import com.openscg.pgstudio.server.models.ItemMetaData;
import com.openscg.pgstudio.server.models.Privileges;
import com.openscg.pgstudio.server.models.QueryMetaData;
import com.openscg.pgstudio.server.models.Rules;
import com.openscg.pgstudio.server.models.Schemas;
import com.openscg.pgstudio.server.models.Sequences;
import com.openscg.pgstudio.server.models.SourceCode;
import com.openscg.pgstudio.server.models.Stats;
import com.openscg.pgstudio.server.models.Tables;
import com.openscg.pgstudio.server.models.Triggers;
import com.openscg.pgstudio.server.models.Types;
import com.openscg.pgstudio.server.models.Views;
import com.openscg.pgstudio.server.util.ConnectionInfo;
import com.openscg.pgstudio.server.util.ConnectionManager;
import com.openscg.pgstudio.server.util.QueryExecutor;
import com.openscg.pgstudio.server.util.QuotingLogic;
import com.openscg.pgstudio.shared.DatabaseConnectionException;
import com.openscg.pgstudio.shared.PostgreSQLException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PgStudioServiceImpl extends RemoteServiceServlet implements
		PgStudioService {

	QuotingLogic q= new QuotingLogic();

	@Override
	public void doLogout(String connectionToken, String source) throws DatabaseConnectionException	{
	/*The argument source keeps a track of the origin of the doLogout call.
	 * 	doLogout strictly works with three strings , viz:
	 * WINDOW_CLOSE : pass this when logging out due to a window/tab close action
	 * USER_INITIATED : pass this when logging out from the Disconnect button
	 * SESSION_TIMEOUT : pass this when logging out due to timeout.
	 */
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		ConnectionManager connMgr = new ConnectionManager();

		connMgr.closeConnection(connectionToken, clientIP, userAgent);

		if (request.getSession(false) != null)	{
			if(source.equals("WINDOW_CLOSE"))	{
				request.getSession(false).invalidate();
	    	}
	    	else
	    	if(source.equals("USER_INITIATED") || source.equals("SESSION_TIMEOUT"))	{
	    		request.getSession(false).setAttribute("dbToken", null);
	    		request.getSession(false).setAttribute("dbName", null);
	    		request.getSession(false).setAttribute("dbURL", null);
	    		request.getSession(false).setAttribute("username", null);
	    	}
		}
	}

	@Override
	public void invalidateSession()	{
		HttpServletRequest request = this.getThreadLocalRequest();  
		request.getSession(false).invalidate();
	}

	@Override
	public String getConnectionInfoMessage(String connectionToken)
			throws IllegalArgumentException, DatabaseConnectionException {
		
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  
		
		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		Connection conn = connMgr.getConnection(connectionToken,clientIP, userAgent);

		String db = "";
		String user = "";
		try {
			db = conn.getCatalog();
			user = conn.getMetaData().getUserName();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String ret = db + " as " + user;
		return ret;
	}

	@Override
	public String getList(String connectionToken, DATABASE_OBJECT_TYPE type)
			throws IllegalArgumentException, DatabaseConnectionException {
		
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		Database db;
		
		switch (type) {
		case DATA_TYPE:
			db = new Database(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return db.getDataTypes();
		case FOREIGN_SERVER:
			db = new Database(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return db.getForeignServerList();
		case SCHEMA:
			Schemas schemas = new Schemas(connMgr.getConnection(connectionToken,clientIP, userAgent));			
			return schemas.getList();
		case LANGUAGE:
			db = new Database(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return db.getLanguageList();
		case ROLE:
			db = new Database(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return db.getRoleList();
		default:
			return "";
		}
		
	}

	@Override
	public String getList(String connectionToken, int schema, ITEM_TYPE type)
			throws IllegalArgumentException, DatabaseConnectionException {

		ConnectionManager connMgr = new ConnectionManager();		
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		switch (type) {
		case TABLE:
			Tables tables = new Tables(connMgr.getConnection(connectionToken,clientIP, userAgent));	
			return tables.getList(schema);
		case MATERIALIZED_VIEW:
		case VIEW:
			Views views = new Views(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return views.getList(schema);
		case FOREIGN_TABLE:
			ForeignTables fTables = new ForeignTables(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return fTables.getList(schema);
		case FUNCTION:
			Functions funcs = new Functions(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return  funcs.getList(schema);
		case SEQUENCE:
			Sequences seqs = new Sequences(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return seqs.getList(schema);
		case TYPE:
			Types types = new Types(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return  types.getList(schema);
		default:
			return "";
		}			
	}

	@Override
	public String getRangeDiffFunctionList(String connectionToken, String schema, String subType)
			throws IllegalArgumentException, DatabaseConnectionException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		Functions funcs;

		funcs = new Functions(connMgr.getConnection(connectionToken,clientIP, userAgent));

		String funcList = funcs.getRangeDiffList(schema, subType);
		
		return funcList;
	}

	@Override
	public String getTriggerFunctionList(String connectionToken, int schema)
			throws IllegalArgumentException, DatabaseConnectionException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		Functions funcs;

		funcs = new Functions(connMgr.getConnection(connectionToken,clientIP, userAgent));

		String funcList = funcs.getTriggerFunctionList(schema);
		
		return funcList;
	}

	
	@Override
	public String getItemObjectList(String connectionToken,
			int item, ITEM_TYPE type, ITEM_OBJECT_TYPE object)
			throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		try {
		switch (object) {
		case TRIGGER:
			Triggers triggers = new Triggers(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return  triggers.getList(item);
		case COLUMN:
			Columns columns = new Columns(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return  columns.getList(item);
		case CONSTRAINT:
			Constraints constraints = new Constraints(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return  constraints.getList(item);
		case GRANT:
			Privileges priv = new Privileges(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return priv.getPrivileges(item, type);
		case INDEX:
			Indexes indexes = new Indexes(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return indexes.getList(item);
		case RULE:
			Rules rules = new Rules(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return rules.getList(item);
		case SOURCE:
			SourceCode sc = new SourceCode(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return sc.getSourceCode(item, type);
		case STATS:
			Stats stats = new Stats(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return stats.getList(item);
		default:
			return "";
		}
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String getItemMetaData(String connectionToken, 
			int item, ITEM_TYPE type)
			throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException 
	{

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		ItemMetaData id = new ItemMetaData(connMgr.getConnection(connectionToken,clientIP, userAgent));

		try {
			return id.getMetaData(item, type);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String getItemData(String connectionToken, int item, ITEM_TYPE type, int count)
			throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException 
	{

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		ItemData id;

		id = new ItemData(connMgr.getConnection(connectionToken,clientIP, userAgent));
		
		try {
			return id.getData(item, type, count);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String getQueryMetaData(String connectionToken, String query)
			throws IllegalArgumentException, DatabaseConnectionException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		QueryMetaData id;

		id = new QueryMetaData(connMgr.getConnection(connectionToken,clientIP, userAgent));
		
		return id.getMetaData(query);
	}

	@Override
	public String executeQuery(String connectionToken, String query, String queryType)
			throws IllegalArgumentException, DatabaseConnectionException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		QueryExecutor id;

		id = new QueryExecutor(connMgr.getConnection(connectionToken,clientIP, userAgent));

		return id.Execute(query, queryType);
	}

	@Override
	public String dropItem(String connectionToken, int item, ITEM_TYPE type, boolean cascade) 
			throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		try {
		switch (type) {
		case FOREIGN_TABLE:
			ForeignTables fTables = new ForeignTables(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return fTables.drop(item, cascade);
		case TABLE:
			Tables tables = new Tables(connMgr.getConnection(connectionToken,clientIP, userAgent));		
			return tables.drop(item, cascade);
		case VIEW:
		case MATERIALIZED_VIEW:
			boolean isMaterialized = false;
			
			if (type == ITEM_TYPE.MATERIALIZED_VIEW)
				isMaterialized = true;
			
			Views views= new Views(connMgr.getConnection(connectionToken,clientIP, userAgent));			
			return views.dropView(item, cascade, isMaterialized);
		case FUNCTION:
			Functions functions = new Functions(connMgr.getConnection(connectionToken, clientIP, userAgent));
			return functions.dropFunction(item, cascade);
		case SEQUENCE:
			 Sequences sequences = new Sequences(connMgr.getConnection(connectionToken, clientIP, userAgent));
			return sequences.drop(item, cascade);
		case TYPE:
			Types types = new Types(connMgr.getConnection(connectionToken, clientIP, userAgent));
			return types.dropType(item, cascade);
		default:
			return "";
		}
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());			
		}
	}

	@Override
	public String dropItemObject(String connectionToken, int item,
			ITEM_TYPE type, String objectName, ITEM_OBJECT_TYPE objType)
			throws DatabaseConnectionException, PostgreSQLException {
		
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		try {
		switch (objType) {
		case COLUMN:
			Columns columns = new Columns(connMgr.getConnection(
					connectionToken, clientIP, userAgent));
			return columns.drop(item, q.addQuote(objectName));

		case CONSTRAINT:
			Constraints constraints = new Constraints(connMgr.getConnection(
					connectionToken, clientIP, userAgent));
			return constraints.drop(item, q.addQuote(objectName));
		case INDEX:
			Indexes indexes = new Indexes(connMgr.getConnection(
					connectionToken, clientIP, userAgent));
			return indexes.drop(item, q.addQuote(objectName));
		case RULE:
			Rules rules = new Rules(connMgr.getConnection(connectionToken,
					clientIP, userAgent));
			return rules.drop(item, q.addQuote(objectName));
		case TRIGGER:
			Triggers triggers = new Triggers(connMgr.getConnection(
					connectionToken, clientIP, userAgent));
			return triggers.drop(item, q.addQuote(objectName));

		default:
			return "";
		}
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String renameItemObject(String connectionToken, int item,
			ITEM_TYPE type, String objectName, ITEM_OBJECT_TYPE objType,
			String newObjectName) throws DatabaseConnectionException, PostgreSQLException 
	{
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		try {
		switch (objType) {
		case COLUMN:
			Columns columns = new Columns(connMgr.getConnection(
					connectionToken, clientIP, userAgent));
			return columns.rename(item, q.addQuote(objectName),
					q.addQuote(newObjectName));
		case CONSTRAINT:
			Constraints constraints = new Constraints(connMgr.getConnection(
					connectionToken, clientIP, userAgent));
			return constraints.rename(item, q.addQuote(objectName),
					q.addQuote(newObjectName));
		case INDEX:
			Indexes indexes = new Indexes(connMgr.getConnection(
					connectionToken, clientIP, userAgent));
			return indexes.rename(item, q.addQuote(objectName),
					q.addQuote(newObjectName));
		case RULE:
			// A RULE can not be renamed so just return a blank string if if get
			// here for some reason
			return "";
		case TRIGGER:
			Triggers triggers = new Triggers(connMgr.getConnection(
					connectionToken, clientIP, userAgent));
			return triggers.rename(item, q.addQuote(objectName),
					q.addQuote(newObjectName));

		default:
			return "";
		}
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String analyze(String connectionToken, int item, ITEM_TYPE type, boolean vacuum, boolean vacuumFull) 
			throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Tables tables;

		tables = new Tables(connMgr.getConnection(connectionToken,clientIP, userAgent));	
		
		try {
			return tables.analyze(item, type, vacuum, vacuumFull);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String renameItem(String connectionToken, int item, ITEM_TYPE type, String newName) 
			throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		try {
		switch(type) {
		case FOREIGN_TABLE:
			ForeignTables fTables = new ForeignTables(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return fTables.rename(item, type, q.addQuote(newName));
		case TABLE:
			Tables tables = new Tables(connMgr.getConnection(connectionToken,clientIP, userAgent));
			return tables.rename(item, type, q.addQuote(newName));
		case VIEW:
		case MATERIALIZED_VIEW:
			Views views = new Views(connMgr.getConnection(connectionToken,clientIP, userAgent));	
			return views.rename(item, type, newName);
		default:
			return "";			
		}
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String truncate(String connectionToken, int item, ITEM_TYPE type) throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Tables tables = new Tables(connMgr.getConnection(connectionToken,clientIP, userAgent));
	
		try {
			return tables.truncate(item, type);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String createTable(String connectionToken, int schema,
			String tableName, boolean unlogged, boolean temporary, String fill,
			ArrayList<String> col_list, HashMap<Integer, String> commentLog, ArrayList<String> col_index)
			throws DatabaseConnectionException, PostgreSQLException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Tables tables = new Tables(connMgr.getConnection(connectionToken,clientIP, userAgent));

		try {
			return tables.create(schema, q.addQuote(tableName), unlogged, temporary, fill, col_list, commentLog, col_index);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String createView(String connectionToken, String schema,
			String viewName, String definition, String comment, boolean isMaterialized) throws DatabaseConnectionException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Views views= new Views(connMgr.getConnection(connectionToken,clientIP, userAgent));

		return views.createView(schema, q.addQuote(viewName), definition, comment, isMaterialized);

	}

	@Override
	public String createColumn(String connectionToken, int item, 
			String columnName, String datatype, String comment, boolean not_null, String defaultval) throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Columns columns;

		columns = new Columns(connMgr.getConnection(connectionToken, clientIP, userAgent));

		try {
			return columns.create(item, q.addQuote(columnName), datatype,comment, not_null, defaultval);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String createIndex(String connectionToken, int item, 
			String indexName, INDEX_TYPE indexType, 
			boolean isUnique, boolean isConcurrently, ArrayList<String> columnList) throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Indexes indexes = new Indexes(connMgr.getConnection(connectionToken, clientIP, userAgent));

		try {
			return indexes.create(item, indexName, indexType, isUnique, isConcurrently, columnList);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String createSequence(String connectionToken, int schema,
			String sequenceName, boolean temporary, int increment,
			int minValue, int maxValue, int start, int cache, boolean cycle)
			throws DatabaseConnectionException, PostgreSQLException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Sequences sequences = new Sequences(connMgr.getConnection(connectionToken,
				clientIP, userAgent));

		try {
			return sequences.create(schema, sequenceName,
					temporary, increment, minValue, maxValue, start, cache, cycle);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String renameSchema(String connectionToken, String oldSchema, String schema) throws DatabaseConnectionException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Schemas schemas;

		schemas = new Schemas(connMgr.getConnection(connectionToken,clientIP, userAgent));
		
		return schemas.renameSchema(schema, oldSchema);
	}

	@Override
	public String dropSchema(String connectionToken, String schemaName, boolean cascade) throws DatabaseConnectionException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Schemas schemas;

		schemas = new Schemas(connMgr.getConnection(connectionToken,clientIP, userAgent));
		
		return schemas.dropSchema(schemaName, cascade);
	}

	@Override
	public String createSchema(String connectionToken, String schemaName) throws DatabaseConnectionException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Schemas schemas;

		schemas = new Schemas(connMgr.getConnection(connectionToken,clientIP, userAgent));
		
		return schemas.createSchema(schemaName);
	}

	@Override
	public String getExplainResult(String connectionToken, String query) throws IllegalArgumentException, DatabaseConnectionException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");
		
		ItemData id;

		id = new ItemData(connMgr.getConnection(connectionToken,clientIP, userAgent));
		
		return id.getExplainResult(query);
	}

	@Override
	public String createUniqueConstraint(String connectionToken, int item, 
			String constraintName, boolean isPrimaryKey,
			ArrayList<String> columnList) throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Constraints constraints;

		constraints = new Constraints(connMgr.getConnection(connectionToken,
				clientIP, userAgent));

		try {
			return constraints.createUniqueConstraint(item, constraintName, isPrimaryKey, columnList);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}

	}

	@Override
	public String createCheckConstraint(String connectionToken, int item, String constraintName, String definition)
			throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Constraints constraints;

		constraints = new Constraints(connMgr.getConnection(connectionToken,
				clientIP, userAgent));

		try {
			return constraints.createCheckConstraint(item, constraintName, definition);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}
	
	@Override
	public String createForeignKeyConstraint(String connectionToken,
			int item, String constraintName,
			ArrayList<String> columnList, String referenceTable,
			ArrayList<String> referenceList) throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Constraints constraints;

		constraints = new Constraints(connMgr.getConnection(connectionToken,
				clientIP, userAgent));

		try {
			return constraints.createForeignKeyConstraint(item, constraintName, columnList, referenceTable,
					referenceList);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}
	
	@Override
	public String createFunction(String connectionToken, int schema,
			String functionName, String returns, String language,
			ArrayList<String> paramList, String definition)
			throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Functions funcs;

		funcs = new Functions(connMgr.getConnection(connectionToken, clientIP,
				userAgent));

		try {
			return funcs.create(schema, functionName, returns, language, paramList, definition);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}
	
	@Override
	public String createType(String connectionToken, String schema,
			String typeName, TYPE_FORM form, String baseType, String definition,
			ArrayList<String> attributeList) throws DatabaseConnectionException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Types types;

		types = new Types(connMgr.getConnection(connectionToken, clientIP,
				userAgent));

		return types.createType(connectionToken, schema, typeName, form,
				baseType, definition, attributeList);
	}
	
	@Override
	public String createForeignTable(String connectionToken, String schema,
			String tableName, String server,
			ArrayList<String> columns, HashMap<Integer, String> comments, ArrayList<String> options)
			throws IllegalArgumentException, DatabaseConnectionException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		ForeignTables tables;

		tables = new ForeignTables(connMgr.getConnection(connectionToken,clientIP, userAgent));
	
		return tables.createForeignTable(connectionToken, schema, tableName, server, columns, comments, options);
	}

	@Override
	public String createRule(String connectionToken, int item, ITEM_TYPE type,
			String ruleName, String event, String ruleType, String definition)
			throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Rules rules;

		rules = new Rules(connMgr.getConnection(connectionToken, clientIP,
				userAgent));

		try {
			return rules.createRule(item, type, ruleName, event, ruleType,
					definition);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String createTrigger(String connectionToken, int item, ITEM_TYPE type,
			String triggerName,
			String event, String triggerType, String forEach,
			String function) throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Triggers triggers = new Triggers(connMgr.getConnection(connectionToken,
				clientIP, userAgent));

		try {
			return triggers.createTrigger(item, type, triggerName, event,
					triggerType, forEach, function);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}
	
	@Override
	public String revoke(String connectionToken, int item, ITEM_TYPE type,
			String privilege, String grantee, boolean cascade)
			throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Privileges priv = new Privileges(connMgr.getConnection(connectionToken,
				clientIP, userAgent));
		try {
			return priv.revoke(item, type, privilege, grantee, cascade);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}
	}

	@Override
	public String grant(String connectionToken, int item, ITEM_TYPE type,
			ArrayList<String> privileges, String grantee)
			throws DatabaseConnectionException, PostgreSQLException {
		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Privileges priv = new Privileges(connMgr.getConnection(connectionToken,
				clientIP, userAgent));
		try {
			return priv.grant(item, type, privileges, grantee);
		} catch (SQLException e) {
			throw new PostgreSQLException(e.getMessage());
		}

	}
	
/***********************************************************************************************/
	
	@Override
	public String refreshMaterializedView(String connectionToken,
			String schema, String viewName) throws DatabaseConnectionException {

		ConnectionManager connMgr = new ConnectionManager();
		HttpServletRequest request = this.getThreadLocalRequest();  

		String clientIP = ConnectionInfo.remoteAddr(request);
		String userAgent = request.getHeader("User-Agent");

		Views views = new Views(connMgr.getConnection(connectionToken,
				clientIP, userAgent));

		return views.refreshMaterializedView(schema, q.addQuote(viewName));
	}

}
