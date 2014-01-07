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
package com.openscg.pgstudio.server.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.openscg.pgstudio.client.PgStudio.INDEX_TYPE;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.server.util.QueryExecutor;

public class Indexes {

	private final Connection conn;
	
	private final static String INDEX_LIST = 
		"SELECT ci.relname as indexname, ri.rolname as indexowner, am.amname," +
		"       i.indisprimary, i.indisunique, i.indisexclusion, (i.indpred IS NOT NULL) as ispartial, " +
		"       pg_get_indexdef(i.indexrelid, 0, true) as def, ci.oid" +
		"  FROM pg_index i, pg_class ct, pg_roles ri, pg_class ci, pg_am am " +
		" WHERE i.indexrelid = ci.oid " +
		"   AND i.indrelid = ct.oid " +
		"   AND ci.relowner = ri.oid " +
		"   AND ci.relam = am.oid" +
		"   AND ct.oid = ? ";

	
	public Indexes(Connection conn) {
		this.conn = conn;
	}

	public String getList(int item) {
		JSONArray result = new JSONArray();
		
		try {
			PreparedStatement stmt = conn.prepareStatement(INDEX_LIST);
			stmt.setInt(1, item);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));				
				jsonMessage.put("name", rs.getString("indexname"));
				jsonMessage.put("owner", rs.getString("indexowner"));
				jsonMessage.put("access_method", rs.getString("amname"));
				
				jsonMessage.put("primary_key", Boolean.toString(rs.getBoolean("indisprimary")));
				jsonMessage.put("unique", Boolean.toString(rs.getBoolean("indisunique")));
				jsonMessage.put("exclusion", Boolean.toString(rs.getBoolean("indisexclusion")));
				jsonMessage.put("partial", Boolean.toString(rs.getBoolean("ispartial")));

				jsonMessage.put("definition", rs.getString("def"));

				result.add(jsonMessage);
			}
			
		} catch (SQLException e) {
			return "";
		}
		
		return result.toString();
	}
	
	public String create(int item, String indexName,
			INDEX_TYPE indexType, boolean isUnique, boolean isConcurrently,
			ArrayList<String> columnList) throws SQLException {

		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		StringBuffer cmd = new StringBuffer("CREATE");

		if (isUnique)
			cmd.append(" UNIQUE");

		cmd.append(" INDEX");

		if (isConcurrently)
			cmd.append(" CONCURRENTLY");

		cmd.append(" " + indexName + " ON " + name);

		cmd.append(" USING " + indexType.toString());

		cmd.append(" (");

		for (String column : columnList) {
			cmd.append(column + ",");
		}
		cmd.deleteCharAt(cmd.length() - 1);

		cmd.append(")");

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(cmd.toString());
	}

	public String drop(int item, String indexName) throws SQLException {
		Database db = new Database(conn);
		String schema = db.getItemSchema(item, ITEM_TYPE.TABLE);
		
		String command = "DROP INDEX " + schema + "." + indexName;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}
	
	public String rename(int item, String oldName, String newName) throws SQLException{
		Database db = new Database(conn);
		String schema = db.getItemSchema(item, ITEM_TYPE.TABLE);

		String command = "ALTER INDEX " + schema + "." + oldName + " RENAME TO " + newName;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

}