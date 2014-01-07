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
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.server.util.QueryExecutor;

public class ForeignTables {

	private final Connection conn;

	private final static String TABLE_LIST =
			"SELECT c.relname as tablename, fw.fdwname, d.description, c.oid " +
			"  FROM pg_foreign_table ft, " +
			"       pg_foreign_server fs, pg_foreign_data_wrapper fw, " +
			"       pg_class c " +
			"       LEFT OUTER JOIN pg_description d " +
			"         ON (c.oid = d.objoid AND d.objsubid = 0) " +
			" WHERE c.relnamespace = ? " +
			"   AND c.oid = ft.ftrelid " +
			"   AND ft.ftserver = fs.oid " +
			"   AND fs.srvfdw = fw.oid " +
			"   AND c.relkind = 'f' " +
			" ORDER BY tablename ";

	public ForeignTables(Connection conn) {
		this.conn = conn;
	}
	
	public String getList(int schema) {
		JSONArray result = new JSONArray();

		try {
			PreparedStatement stmt = null;
			stmt = conn.prepareStatement(TABLE_LIST);

			stmt.setInt(1, schema);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString("tablename"));
				jsonMessage.put("table_type", rs.getString("fdwname"));
				jsonMessage.put("comment", rs.getString("description"));
				result.add(jsonMessage);
			}

		} catch (SQLException e) {
			return "";
		}
		return result.toString();
	}
	
	public String drop(int item, boolean cascade) throws SQLException{
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "DROP FOREIGN TABLE " + name;

		if (cascade)
			command = command + " CASCADE";

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}
	
	public String analyzeTable(String schema, String tableName){
		String command = "ANALYZE " + schema + "." + tableName;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}
	
	public String rename(int item, ITEM_TYPE type, String newName) throws SQLException{
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "ALTER FOREIGN TABLE " + name + " RENAME TO " + newName;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}
	
	public String createForeignTable(String connectionToken, String schema,
			String tableName, String server, ArrayList<String> columns,
			HashMap<Integer, String> comments, ArrayList<String> options) {

			StringBuffer prefix;
			StringBuffer column;
			StringBuffer suffix;

			String query;

			prefix = new StringBuffer("CREATE FOREIGN TABLE " + schema + "."
					+ tableName);

			column = new StringBuffer("");
			if (columns.size() > 0) {
				/*
				 * The ArrayList col_list contains the details of each column as
				 * it would appear in the Final CREATE TABLE query
				 */
				for (int i = 0; i < columns.size(); i++) {
					if (i != 0)
						column.append(" , ");
					column.append(columns.get(i));
				}
			}
			column = new StringBuffer(" ( " + column + " ) ");

			suffix = new StringBuffer(" SERVER " + server);

			if (options != null) {
				if (options.size() > 0) {
					suffix.append(" OPTIONS (");

					for (int i = 0; i < options.size(); i++) {
						if (i != 0)
							suffix.append(" , ");
						suffix.append(options.get(i));
					}

					suffix.append(")");
				}
			}

			query = (prefix.toString() + column.toString() + suffix.toString());
			
			QueryExecutor qe = new QueryExecutor(conn);
			return qe.executeUtilityCommand(query);
	}
}