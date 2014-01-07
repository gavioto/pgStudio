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
package com.openscg.pgstudio.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.openscg.pgstudio.client.PgStudio.DATABASE_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.INDEX_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.PgStudio.TYPE_FORM;
import com.openscg.pgstudio.shared.DatabaseConnectionException;
import com.openscg.pgstudio.shared.PostgreSQLException;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("studio")
public interface PgStudioService extends RemoteService {
	String getConnectionInfoMessage(String connectionToken) throws IllegalArgumentException, DatabaseConnectionException;

	String getList(String connectionToken, DATABASE_OBJECT_TYPE type) throws IllegalArgumentException, DatabaseConnectionException;

	String getList(String connectionToken, int schema, ITEM_TYPE type) throws IllegalArgumentException, DatabaseConnectionException;

	String getRangeDiffFunctionList(String connectionToken, String schema, String subType)  throws IllegalArgumentException, DatabaseConnectionException;
	
	String getTriggerFunctionList(String connectionToken, int schema)  throws IllegalArgumentException, DatabaseConnectionException;
	
	String getItemObjectList(String connectionToken, int item, ITEM_TYPE type, ITEM_OBJECT_TYPE object) throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException;

	String getItemMetaData(String connectionToken, int item, ITEM_TYPE type) throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException;

	String getItemData(String connectionToken, int item, ITEM_TYPE type, int count) throws IllegalArgumentException, DatabaseConnectionException, PostgreSQLException;

	String getQueryMetaData(String connectionToken, String query) throws IllegalArgumentException, DatabaseConnectionException;

	String executeQuery(String connectionToken, String query, String queryType) throws IllegalArgumentException, DatabaseConnectionException;
	
	String dropItem(String connectionToken, int item, ITEM_TYPE type, boolean cascade) throws DatabaseConnectionException, PostgreSQLException;
	
	String analyze(String connectionToken, int item, ITEM_TYPE type, boolean vacuum, boolean vacuumFull) throws DatabaseConnectionException, PostgreSQLException;
	
	String renameItem(String connectionToken, int item, ITEM_TYPE type, String newName) throws DatabaseConnectionException, PostgreSQLException;

	String truncate(String connectionToken, int item, ITEM_TYPE type) throws DatabaseConnectionException, PostgreSQLException;

	String createColumn(String connectionToken, int item, String columnName, String datatype, String comment, boolean not_null, String defaultval) throws DatabaseConnectionException, PostgreSQLException;

	String createIndex(String connectionToken, int item, String indexName, INDEX_TYPE indexType, boolean isUnique, boolean isConcurrently, ArrayList<String> columnList) throws DatabaseConnectionException, PostgreSQLException;

	String dropItemObject(String connectionToken, int item, ITEM_TYPE type, String objectName, ITEM_OBJECT_TYPE objType) throws DatabaseConnectionException, PostgreSQLException;

	String renameItemObject(String connectionToken, int item, ITEM_TYPE type, String objectName, ITEM_OBJECT_TYPE objType, String newObjectName) throws DatabaseConnectionException, PostgreSQLException;

	String renameSchema(String connectionToken, String oldSchema, String schema) throws DatabaseConnectionException;
	
	String dropSchema(String connectionToken, String schemaName, boolean cascade) throws DatabaseConnectionException;

	String createSchema(String connectionToken, String schemaName) throws DatabaseConnectionException;

	String getExplainResult(String connectionToken, String query) throws IllegalArgumentException, DatabaseConnectionException;

	String createView(String connectionToken, String schema, String viewName, String definition, String comment, boolean isMaterialized) throws DatabaseConnectionException;

	String createTable(String connectionToken, int schema, String tableName, boolean unlogged, boolean temporary, String fill,	ArrayList<String> col_list, HashMap<Integer, String> commentLog, ArrayList<String> col_index) throws DatabaseConnectionException, PostgreSQLException;

	String createUniqueConstraint(String connectionToken, int item, String constraintName, boolean isPrimaryKey, ArrayList<String> columnList) throws DatabaseConnectionException, PostgreSQLException;

	String createCheckConstraint(String connectionToken, int item, String constraintName, String definition) throws DatabaseConnectionException, PostgreSQLException;
	
	String createForeignKeyConstraint(String connectionToken, int item, String constraintName, ArrayList<String> columnList, String referenceTable, ArrayList<String> referenceList) throws DatabaseConnectionException, PostgreSQLException;
	
	String createSequence(String connectionToken, int schema, String sequenceName, boolean temporary, int increment, int minValue, int maxValue, int start, int cache, boolean cycle) throws DatabaseConnectionException, PostgreSQLException;
	
	String createFunction(String connectionToken, int schema, String functionName, String returns, String language, ArrayList<String> paramList, String definition) throws DatabaseConnectionException, PostgreSQLException;
	
	String createType(String connectionToken, String schema, String typeName, TYPE_FORM form, String baseType, String definition, ArrayList<String> attributeList) throws DatabaseConnectionException;

	String createForeignTable(String connectionToken, String schema, String tableName, String server, ArrayList<String> columns, HashMap<Integer, String> comments, ArrayList<String> options) throws DatabaseConnectionException;
	
	String refreshMaterializedView(String connectionToken, String schema, String viewName) throws DatabaseConnectionException;

	String createRule(String connectionToken, int item, ITEM_TYPE type, String ruleName, String event, String ruleType, String definition) throws DatabaseConnectionException, PostgreSQLException;
	
	String createTrigger(String connectionToken, int item, ITEM_TYPE type, String triggerName, String event, String triggerType, String forEach, String function) throws DatabaseConnectionException, PostgreSQLException;
	
	String revoke(String connectionToken, int item, ITEM_TYPE type, String privilege, String grantee, boolean cascade) throws DatabaseConnectionException, PostgreSQLException;
	
	String grant(String connectionToken, int item, ITEM_TYPE type, ArrayList<String> privileges, String grantee) throws DatabaseConnectionException, PostgreSQLException;

	void doLogout(String connectionToken, String source) throws IllegalArgumentException, DatabaseConnectionException;

	void invalidateSession();

}
