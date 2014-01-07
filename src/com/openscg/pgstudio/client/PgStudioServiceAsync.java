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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.openscg.pgstudio.client.PgStudio.DATABASE_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.INDEX_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_OBJECT_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.PgStudio.TYPE_FORM;

/**
 * The async counterpart of <code>PgStudioService</code>.
 */
public interface PgStudioServiceAsync {

	void getConnectionInfoMessage(String connectionToken, AsyncCallback<String> callback)
	        throws IllegalArgumentException;
	
	void getList(String connectionToken, DATABASE_OBJECT_TYPE type, AsyncCallback<String> callback)
	        throws IllegalArgumentException;

	void getList(String connectionToken, int schema, ITEM_TYPE type, AsyncCallback<String> callback)
    		throws IllegalArgumentException;

	void getRangeDiffFunctionList(String connectionToken, String schema, String subType, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void getTriggerFunctionList(String connectionToken, int schema, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	
	void getItemObjectList(String connectionToken, int item, ITEM_TYPE type, ITEM_OBJECT_TYPE object, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void getItemMetaData(String connectionToken, int item, ITEM_TYPE type, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void getItemData(String connectionToken, int item, ITEM_TYPE type, int count, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void getQueryMetaData(String connectionToken, String query, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void executeQuery(String connectionToken, String query, String queryType, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void dropItem(String connectionToken, int item, ITEM_TYPE type, boolean cascade, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	
	void analyze(String connectionToken, int item, ITEM_TYPE type, boolean vacuum, boolean vacuumFull, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	
	void renameItem(String connectionToken, int item, ITEM_TYPE type, String newName, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	
	void truncate(String connectionToken, int item, ITEM_TYPE type, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	
	void createIndex(String connectionToken, int item, String indexName, INDEX_TYPE indexType, boolean isUnique, boolean isConcurrently, ArrayList<String> columnList, AsyncCallback<String> callback) 
			throws IllegalArgumentException;

	void dropItemObject(String connectionToken, int item, ITEM_TYPE type, String objectName, ITEM_OBJECT_TYPE objType, AsyncCallback<String> callback) 
			throws IllegalArgumentException;

	void renameItemObject(String connectionToken, int item, ITEM_TYPE type, String objectName, ITEM_OBJECT_TYPE objType, String newObjectName, AsyncCallback<String> callback) 
			throws IllegalArgumentException;

	void renameSchema(String connectionToken, String oldSchema, String schema, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	
	void dropSchema(String connectionToken, String schemaName, boolean cascade, AsyncCallback<String> callback) 
			throws IllegalArgumentException;

	void createSchema(String connectionToken, String schemaName, AsyncCallback<String> callback) 
			throws IllegalArgumentException;

	void getExplainResult(String connectionToken, String query, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void createView(String connectionToken, String schema, String viewName, String definition, String comment, boolean isMaterialized, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void createColumn(String connectionToken, int item, String columnName, String datatype, String comment, boolean not_null, String defaultval, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void createTable(String connectionToken, int schema, String tableName, boolean unlogged, boolean temporary, String fill, ArrayList<String> col_list,  HashMap<Integer,String> commentLog, ArrayList<String> col_index, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void createUniqueConstraint(String connectionToken, int item, String constraintName, boolean isPrimaryKey, ArrayList<String> columnList, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	
	void createCheckConstraint(String connectionToken, int item, String constraintName, String definition, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	
	void createForeignKeyConstraint(String connectionToken, int item, String constraintName, ArrayList<String> columnList, String referenceTable, ArrayList<String> referenceList, AsyncCallback<String> callback)
			throws IllegalArgumentException;
			
	void createSequence(String connectionToken, int schema, String sequenceName, boolean temporary, int increment, int minValue, int maxValue, int start, int cache, boolean cycle, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	
	void createFunction(String connectionToken, int schema, String functionName, String returns, String language, ArrayList<String> paramList, String definition, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	
	void createType(String connectionToken, String schema, String typeName, TYPE_FORM form, String baseType, String definition, ArrayList<String> attributeList, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void  createForeignTable(String connectionToken, String schema, String tableName, String server, ArrayList<String> columns, HashMap<Integer, String> comments, ArrayList<String> options, AsyncCallback<String> callback) 
			throws IllegalArgumentException;

	void refreshMaterializedView(String connectionToken, String schema, String viewName, AsyncCallback<String> callback) 
			throws IllegalArgumentException;

	void createRule(String connectionToken, int item, ITEM_TYPE type, String ruleName, String event, String ruleType, String definition, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	
	void createTrigger(String connectionToken, int item, ITEM_TYPE type, String triggerName, String event, String triggerType, String forEach, String function, AsyncCallback<String> callback) 
			throws IllegalArgumentException;
	
	void revoke(String connectionToken, int item, ITEM_TYPE type, String privilege, String grantee, boolean cascade, AsyncCallback<String> callback) 
					throws IllegalArgumentException;
			
	void grant(String connectionToken, int item, ITEM_TYPE type, ArrayList<String> privileges, String grantee, AsyncCallback<String> callback) 
			throws IllegalArgumentException;

	void doLogout(String connectionToken, String source, AsyncCallback<Void> asyncCallback) throws IllegalArgumentException;

	void invalidateSession(AsyncCallback<Void> callback);

}
