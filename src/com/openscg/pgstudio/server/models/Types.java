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
import com.openscg.pgstudio.client.PgStudio.TYPE_FORM;
import com.openscg.pgstudio.server.util.QueryExecutor;

public class Types {

	private final Connection conn;

	private final static String TYPE_LIST = "SELECT t.typname, t.typtype, c.oid "
			+ "  FROM pg_type t, pg_class c "
			+ " WHERE t.typnamespace = ? " + "   AND t.typrelid = c.oid "
			+ "   AND t.typtype = 'c' " + "   AND c.relkind = 'c' "
			+ " UNION "
			+ "SELECT d.typname, d.typtype, d.oid "
			+ "  FROM pg_type d "
			+ " WHERE d.typnamespace = ? "
			+ "   AND d.typtype in ('d', 'e', 'r') " 
			+ " ORDER BY typname  ";

	public Types(Connection conn) {
		this.conn = conn;
	}

	public String getList(int schema) {
		JSONArray result = new JSONArray();

		try {
			PreparedStatement stmt = conn.prepareStatement(TYPE_LIST);
			stmt.setInt(1, schema);
			stmt.setInt(2, schema);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));

				jsonMessage.put("name", rs.getString("typname"));
				jsonMessage.put("type_kind", rs.getString("typtype"));

				result.add(jsonMessage);
			}

		} catch (SQLException e) {
			return "";
		}

		return result.toString();
	}

	public String dropType(int item, boolean cascade) throws SQLException {
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TYPE);
		
		StringBuffer command = new StringBuffer("DROP TYPE " + name);
		
		if (cascade)
			command.append(" CASCADE");

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command.toString());
	}

	public String createType(String connectionToken, String schema,
			String typeName, TYPE_FORM form, String baseType,
			String definition, ArrayList<String> attributeList) {

		switch (form) {
		case COMPOSITE:
			return createComposite(connectionToken, schema, typeName,
					attributeList);
		case ENUM:
			return createEnum(connectionToken, schema, typeName, attributeList);
		case DOMAIN:
			return createDomain(connectionToken, schema, typeName, baseType,
					definition);
		case RANGE:
			return createRange(connectionToken, schema, typeName, baseType,
					definition);
		default:
			break;
		}

		return "Command Not Executed";
	}

	private String createComposite(String connectionToken, String schema,
			String typeName, ArrayList<String> attributeList) {

		StringBuffer prefix;
		StringBuffer attr;
		String query;

		prefix = new StringBuffer("CREATE TYPE " + schema + "." + typeName
				+ " AS ");

		attr = new StringBuffer("");
		if (attributeList.size() > 0) {
			/*
			 * The ArrayList paramList contains the details of each parameter as
			 * it would appear in the final CREATE FUNCTION query
			 */
			for (int i = 0; i < attributeList.size(); i++) {
				if (i != 0)
					attr.append(" , ");
				attr.append(attributeList.get(i));
			}
		}
		attr = new StringBuffer(" ( " + attr + " ) ");

		query = (prefix.toString() + attr.toString());

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(query);
	}

	private String createDomain(String connectionToken, String schema,
			String typeName, String baseType, String definition) {

		StringBuffer prefix;
		String query;

		prefix = new StringBuffer("CREATE DOMAIN " + schema + "." + typeName
				+ " AS ");
		prefix.append(baseType);

		query = (prefix.toString() + " " + definition);

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(query);
	}

	private String createEnum(String connectionToken, String schema,
			String typeName, ArrayList<String> attributeList) {

		StringBuffer prefix;
		StringBuffer attr;
		String query;

		prefix = new StringBuffer("CREATE TYPE " + schema + "." + typeName
				+ " AS ENUM ");

		attr = new StringBuffer("");
		if (attributeList.size() > 0) {
			/*
			 * The ArrayList paramList contains the details of each parameter as
			 * it would appear in the final CREATE FUNCTION query
			 */
			for (int i = 0; i < attributeList.size(); i++) {
				if (i != 0)
					attr.append(" , ");
				attr.append("$$" + attributeList.get(i) + "$$");
			}
		}
		attr = new StringBuffer(" ( " + attr + " ) ");

		query = (prefix.toString() + attr.toString());

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(query);
	}

	private String createRange(String connectionToken, String schema,
			String typeName, String baseType, String definition) {

		StringBuffer query;

		query = new StringBuffer("CREATE TYPE " + schema + "." + typeName
				+ " AS RANGE (");
		query.append(" SUBTYPE = " + baseType);

		if (!definition.trim().equals(""))
			query.append(" , " + definition);

		query.append(")");

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(query.toString());
	}

}