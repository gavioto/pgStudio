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

public class Constraints {

	private final Connection conn;

	private final static String CONSTRAINT_LIST = "SELECT DISTINCT conname, contype, condeferrable, condeferred, ci.relname as indexname, "
			+ "       ct.relname as tablename, ri.rolname as indexowner, cn.confdeltype, cn.confupdtype, "
			+ "       pg_get_constraintdef(cn.oid, true) as condef, cn.oid "
			+ "  FROM pg_class ct, pg_constraint cn "
			+ "    LEFT OUTER JOIN pg_depend d ON (cn.oid = d.refobjid) "
			+ "    LEFT OUTER JOIN pg_class ci ON (d.objid = ci.oid) "
			+ "    LEFT OUTER JOIN pg_roles ri ON (ci.relowner = ri.oid) "
			+ " WHERE cn.conrelid = ct.oid " + "   AND ct.oid = ?  ";

	public Constraints(Connection conn) {
		this.conn = conn;
	}

	public String getList(int item) {
		JSONArray result = new JSONArray();

		try {
			PreparedStatement stmt = conn.prepareStatement(CONSTRAINT_LIST);
			stmt.setInt(1, item);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));				
				jsonMessage.put("name", rs.getString(1));
				jsonMessage.put("type", rs.getString(2));
				jsonMessage.put("deferrable",
						Boolean.toString(rs.getBoolean(3)));
				jsonMessage.put("deferred", Boolean.toString(rs.getBoolean(4)));
				jsonMessage.put("index_name", rs.getString(5));
				jsonMessage.put("index_owner", rs.getString(7));
				jsonMessage.put("delete_action", rs.getString(8));
				jsonMessage.put("update_action", rs.getString(9));
				jsonMessage.put("definition", rs.getString(10));

				result.add(jsonMessage);
			}

		} catch (SQLException e) {
			return "";
		}

		return result.toString();
	}

	public String drop(int item, String constraintsName) throws SQLException {
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "ALTER TABLE " + name + " DROP CONSTRAINT "
				+ constraintsName;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String rename(int item, String oldName, String newName)
			throws SQLException {
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "ALTER TABLE " + name + " RENAME CONSTRAINT "
				+ oldName + " TO " + newName;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String createUniqueConstraint(int item, String constraintName,
			boolean isPrimaryKey, ArrayList<String> columnList)
			throws SQLException {

		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		StringBuffer command = new StringBuffer("ALTER TABLE");

		command.append(" " + name);

		if (isPrimaryKey) {
			command.append(" ADD PRIMARY KEY ");
		} else {
			command.append(" ADD CONSTRAINT ");
			command.append(constraintName);
			command.append(" UNIQUE");
		}

		command.append(" (");

		for (String column : columnList) {
			command.append(column + ",");
		}
		command.deleteCharAt(command.length() - 1);

		command.append(")");

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command.toString());
	}

	public String createCheckConstraint(int item, String constraintName,
			String definition) throws SQLException {
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		StringBuffer command = new StringBuffer("ALTER TABLE");

		command.append(" " + name);

		command.append(" ADD CONSTRAINT ");
		command.append(constraintName);
		command.append(" CHECK (");
		command.append(definition);
		command.append(")");

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command.toString());
	}

	public String createForeignKeyConstraint(int item, String constraintName,
			ArrayList<String> columnList, String referenceTable,
			ArrayList<String> referenceList) throws SQLException {

		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		StringBuffer command = new StringBuffer("ALTER TABLE");

		command.append(" " + name);

		command.append(" ADD CONSTRAINT ");
		command.append(constraintName);
		command.append(" FOREIGN KEY");

		command.append(" (");

		for (String column : columnList) {
			command.append(column + ",");
		}
		command.deleteCharAt(command.length() - 1);

		command.append(") REFERENCES ");
		command.append(referenceTable + " (");

		for (String ref : referenceList) {
			command.append(ref + ",");
		}
		command.deleteCharAt(command.length() - 1);
		command.append(")");

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command.toString());
	}

}
