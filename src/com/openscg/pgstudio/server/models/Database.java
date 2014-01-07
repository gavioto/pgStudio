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
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;

public class Database {
	
	private final Connection conn;
	
	private final static String DATA_TYPE_LIST = "SELECT tn, CASE WHEN typmodin = 0 "
			+ "                 THEN false "
			+ "                 ELSE true "
			+ "               END AS has_length,"
			+ "       (SELECT count(*) "
			+ "          FROM pg_attribute a, pg_class c, pg_namespace n"
			+ "         WHERE a.atttypid = ts.oid"
			+ "           AND a.attnum > 0"
			+ "           AND a.attrelid = c.oid"
			+ "           AND c.relnamespace = n.oid"
			+ "           AND n.nspname NOT IN ('pg_catalog', 'information_schema')) AS usage_count "
			+ "  FROM (SELECT typname AS tn, typrelid, typelem, typtype, "
			+ "               typcategory, typbasetype, typmodin, oid"
			+ "          FROM pg_type "
			+ "        UNION"
			+ "        SELECT format_type(oid, null) AS tn,typrelid, typelem, "
			+ "               typtype, typcategory, typbasetype, typmodin, oid"
			+ "          FROM pg_type) AS ts "
			+ " WHERE typrelid = 0 "
			+ "   AND typelem = 0 "
			+ "   AND typtype != 'p' "
			+ "   AND typcategory != 'X' "
			+ "   AND typbasetype = 0 "
			+ "UNION SELECT 'serial', false, 0 "
			+ "UNION SELECT 'bigserial', false, 0 " + " ORDER BY 1";

	private final static String LANGUAGE_LIST = 
			"SELECT lanname, oid " +
			"  FROM pg_language " +
			" WHERE lanname NOT IN ('internal', 'c') " +
			" ORDER BY lanname ";
	
	private final static String ROLE_LIST = 
			"SELECT rolname, oid " +
			"  FROM pg_roles " +
			" ORDER BY rolname ";
	
	private final static String SERVER_LIST = 
			"SELECT srvname, oid " +
			"  FROM pg_foreign_server";
	

	private final static String CLASS_NAME = 
			"SELECT n.nspname, c.relname " +
			"  FROM pg_class c, pg_namespace n " +
			" WHERE c.relnamespace = n.oid " +
			"   AND c.oid = ? ";
	
	private final static String FUNCTION_NAME = 
			"SELECT n.nspname, p.proname, " +
			"       pg_get_function_identity_arguments(p.oid) as args " +
			"  FROM pg_proc p, pg_namespace n " +
			" WHERE p.pronamespace = n.oid " +
			"   AND p.oid = ? ";

	private final static String TYPE_NAME = 
			"SELECT n.nspname, t.typname " +
			"  FROM pg_type t, pg_namespace n " +
			" WHERE t.typnamespace = n.oid " +
			"   AND (t.oid = ? OR t.typrelid = ?) ";

	public Database(Connection conn) {
		this.conn = conn;
	}

	public String getDataTypes() {
		JSONArray result = new JSONArray();

		try {
			PreparedStatement stmt = conn.prepareStatement(DATA_TYPE_LIST);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", "0");
				jsonMessage.put("name", rs.getString(1));
				jsonMessage.put("has_length",
						Boolean.toString(rs.getBoolean(2)));
				jsonMessage.put("usage_count", Integer.toString(rs.getInt(3)));

				result.add(jsonMessage);
			}
		} catch (SQLException e) {
			return "";
		}
		return result.toString();
	}


	public String getLanguageList() {
		JSONArray result = new JSONArray();
		
		try {
			PreparedStatement stmt = conn.prepareStatement(LANGUAGE_LIST);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString("lanname"));
				
				result.add(jsonMessage);
			}
		} catch (SQLException e) {
			return "";
		}
		
		return result.toString();
	}

	public String getForeignServerList() {
		JSONArray result = new JSONArray();

		try {
			PreparedStatement stmt = null;
			stmt = conn.prepareStatement(SERVER_LIST);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString("srvname"));
				result.add(jsonMessage);
			}

		} catch (SQLException e) {
			return "";
		}
		return result.toString();		
	}

	public String getRoleList() {
		JSONArray result = new JSONArray();
		
		try {
			PreparedStatement stmt = conn.prepareStatement(ROLE_LIST);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString("rolname"));
				
				result.add(jsonMessage);
			}
		} catch (SQLException e) {
			return "";
		}
		
		return result.toString();
	}

	public String getItemFullName(int item, ITEM_TYPE type) throws SQLException {
		switch (type) {
		case FOREIGN_TABLE:
		case MATERIALIZED_VIEW:
		case SEQUENCE:
		case TABLE:
		case VIEW:
			return getFullClassName(item);
		case FUNCTION:
			return getFullFunctionName(item);
		case TYPE:
			return getFullTypeName(item);
		}
		
		throw new SQLException("Unknown Item Type");
	}
	
	private String getFullClassName(int oid) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(CLASS_NAME);
		stmt.setInt(1, oid);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String schema = rs.getString("nspname");
			String name = rs.getString("relname");

			if (!StringUtils.isAlphanumeric(schema) || !StringUtils.isAllLowerCase(schema))
				schema = "\"" + schema + "\"";

			if (!StringUtils.isAlphanumeric(name) || !StringUtils.isAllLowerCase(name))
				name = "\"" + name + "\"";

			return schema + "." + name;
		}

		throw new SQLException("Invalid Resultset");
	}

	private String getFullFunctionName(int oid) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(FUNCTION_NAME);
		stmt.setInt(1, oid);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String schema = rs.getString("nspname");
			String name = rs.getString("proname");
			String args = rs.getString("args");

			if (!StringUtils.isAlphanumeric(schema) || !StringUtils.isAllLowerCase(schema))
				schema = "\"" + schema + "\"";

			if (!StringUtils.isAlphanumeric(name) || !StringUtils.isAllLowerCase(name))
				name = "\"" + name + "\"";

			return schema + "." + name + "(" + args + ")";
		}

		throw new SQLException("Invalid Resultset");
	}

	private String getFullTypeName(int oid) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(TYPE_NAME);
		stmt.setInt(1, oid);
		stmt.setInt(2, oid);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String schema = rs.getString("nspname");
			String name = rs.getString("typname");

			if (!StringUtils.isAlphanumeric(schema) || !StringUtils.isAllLowerCase(schema))
				schema = "\"" + schema + "\"";

			if (!StringUtils.isAlphanumeric(name) || !StringUtils.isAllLowerCase(name))
				name = "\"" + name + "\"";

			return schema + "." + name;
		}

		throw new SQLException("Invalid Resultset");
	}

	public String getItemSchema(int item, ITEM_TYPE type) throws SQLException {
		switch (type) {
		case FOREIGN_TABLE:
		case MATERIALIZED_VIEW:
		case SEQUENCE:
		case TABLE:
		case VIEW:
			return getClassSchema(item);
		case FUNCTION:
			return getFunctionSchema(item);
		case TYPE:
			return getTypeSchema(item);
		}
		
		throw new SQLException("Unknown Item Type");
	}
	
	private String getClassSchema(int oid) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(CLASS_NAME);
		stmt.setInt(1, oid);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String schema = rs.getString("nspname");

			if (StringUtils.isAlphanumeric(schema) && StringUtils.isAllLowerCase(schema))
				return schema;
				
			return "\"" + schema + "\"";
		}
		
		throw new SQLException("Invalid Resultset");
	}

	private String getFunctionSchema(int oid) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(FUNCTION_NAME);
		stmt.setInt(1, oid);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String schema = rs.getString("nspname");

			if (!StringUtils.isAlphanumeric(schema) || !StringUtils.isAllLowerCase(schema))
				return schema;
			
			return "\"" + schema + "\"";
		}

		throw new SQLException("Invalid Resultset");
	}

	private String getTypeSchema(int oid) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(TYPE_NAME);
		stmt.setInt(1, oid);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String schema = rs.getString("nspname");

			if (!StringUtils.isAlphanumeric(schema) || !StringUtils.isAllLowerCase(schema))
				return schema;
			
			return "\"" + schema + "\"";
		}

		throw new SQLException("Invalid Resultset");
	}

}
