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

import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.server.util.QueryExecutor;

public class Functions {
	
	private final Connection conn;
	
	private final static String FUNC_LIST = 
		"SELECT proname, p.oid,  pg_get_function_identity_arguments(p.oid) as ident" +
		"  FROM pg_proc p " +
		" WHERE p.pronamespace = ? " +
		" ORDER BY proname ";

	private final static String RANGE_DIFF_FUNC =
			"SELECT proname " +
			"  FROM pg_proc " +
			" WHERE prorettype = (SELECT oid FROM pg_type WHERE typname = 'float8') " +
			"   AND array_length(string_to_array(proargtypes::text, ' '), 1) = 2 " +
			"   AND (string_to_array(proargtypes::text, ' '))[1] = (SELECT oid FROM pg_type WHERE typname = ?)::text " +
			"   AND (string_to_array(proargtypes::text, ' '))[2] = (SELECT oid FROM pg_type WHERE typname = ?)::text " +
			" ORDER BY proname ";
	
	private final static String TRIGGER_FUNC_LIST = 
			"SELECT proname, p.oid " +
			"  FROM pg_proc p " +
			" WHERE p.pronamespace = ? " +
			"   AND prorettype = (SELECT oid FROM pg_type WHERE typname = 'trigger') " +
			" ORDER BY proname ";
	
	public Functions(Connection conn) {
		this.conn = conn;
	}
	
	public String getList(int schema) {
		JSONArray result = new JSONArray();
		
		try {
			PreparedStatement stmt = conn.prepareStatement(FUNC_LIST);
			stmt.setInt(1, schema);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString("proname"));
				jsonMessage.put("ident", rs.getString("ident"));				
				
				result.add(jsonMessage);
			}
			
		} catch (SQLException e) {
			return "";
		}
		
		return result.toString();
	}

	public String getTriggerFunctionList(int schema) {
		JSONArray result = new JSONArray();
		
		try {
			PreparedStatement stmt = conn.prepareStatement(TRIGGER_FUNC_LIST);
			stmt.setInt(1, schema);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString("proname"));
				
				result.add(jsonMessage);
			}
			
		} catch (SQLException e) {
			return "";
		}
		
		return result.toString();
	}

	public String getRangeDiffList(String schema, String subType) {
		JSONArray result = new JSONArray();
		
		try {
			PreparedStatement stmt = conn.prepareStatement(RANGE_DIFF_FUNC);
			stmt.setString(1, subType);
			stmt.setString(2, subType);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("name", rs.getString("proname"));
				
				result.add(jsonMessage);
			}
			
		} catch (SQLException e) {
			return "";
		}
		
		return result.toString();
	}

	public String dropFunction(int item, boolean cascade) throws SQLException{
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.FUNCTION);

		StringBuffer command = new StringBuffer("DROP FUNCTION " + name);

		if (cascade)
			command.append(" CASCADE");

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command.toString());
	}

	public String create(int schema,
			String functionName, String returns, String language,
			ArrayList<String> paramList, String definition) throws SQLException {

		StringBuffer prefix;
		StringBuffer parameter;
		StringBuffer suffix;
		String query;

		Schemas s = new Schemas(conn);
		String schemaName = s.getName(schema);

		prefix = new StringBuffer("CREATE FUNCTION " +schemaName + "." + functionName);

		parameter = new StringBuffer("");
		if (paramList.size() > 0) {
			/*
			 * The ArrayList paramList contains the details of each parameter as
			 * it would appear in the final CREATE FUNCTION query
			 */
			for (int i = 0; i < paramList.size(); i++) {
				if (i != 0)
					parameter.append(" , ");
				parameter.append(paramList.get(i));
			}
		}
		parameter = new StringBuffer(" ( " + parameter + " ) ");

		prefix.append(parameter);
		prefix.append(" RETURNS " + returns);
		prefix.append(" AS $$\n");

		suffix = new StringBuffer("\n$$ ");
		suffix.append(" LANGUAGE " + language);

		query = (prefix.toString() + definition + suffix.toString());

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(query);
	}
}
