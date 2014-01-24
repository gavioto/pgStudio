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
import java.sql.Statement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.openscg.pgstudio.server.util.QueryExecutor;

public class Schemas {
	
	private final Connection conn;
	
	private static String SCHEMA_LIST = 
		"SELECT nspname, oid " +
		"  FROM pg_namespace " +
		" WHERE nspname not in ('pg_toast', 'pg_temp_1', 'pg_toast_temp_1') " +
		" ORDER BY nspname ";
	
	private static String SCHEMA_NAME = 
			"SELECT nspname " +
			"  FROM pg_namespace " +
			" WHERE oid = ? ";
	
	public Schemas(Connection conn) {
		this.conn = conn;
	}
	
	public String getList() {
		JSONArray result = new JSONArray();
		
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SCHEMA_LIST);
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString("nspname"));
				
				result.add(jsonMessage);
			}
			
		} catch (SQLException e) {
			return "";
		}				
		
		return result.toString();
	}
	
	public String renameSchema(String schema, String oldSchema){
		String command = "ALTER SCHEMA " + oldSchema + " RENAME TO " + schema;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String createSchema(String schema){
		String command = "CREATE SCHEMA " + schema;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String dropSchema(String schema, boolean cascade){
		String command = "DROP SCHEMA " + schema;

		if (cascade) 
			command = command + " CASCADE";
		
		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}
	
	public String getName(int oid) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(SCHEMA_NAME);
		stmt.setInt(1, oid);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String schema = rs.getString("nspname");
			return "\"" + schema + "\"";
		}

		throw new SQLException("Invalid Resultset");
	}

}
