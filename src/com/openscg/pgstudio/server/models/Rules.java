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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.server.util.QueryExecutor;

public class Rules {
	
	private final Connection conn;
	
	private final static String RULE_LIST = 
			"SELECT c.relname, r.rulename, r.ev_type, r.ev_enabled, " +
			"       r.is_instead, pg_get_ruledef(r.oid, true) as def, r.oid " +
			"  FROM pg_rewrite r, pg_class c " +
			" WHERE r.ev_class = c.oid " +
			"   AND r.rulename <> '_RETURN' " +
			"   AND c.oid = ? " +
			" ORDER BY 1, 2 ";

	
	public Rules(Connection conn) {
		this.conn = conn;
	}
	
	public String getList(int item) throws SQLException {
		JSONArray result = new JSONArray();
		
			PreparedStatement stmt = conn.prepareStatement(RULE_LIST);
			stmt.setInt(1, item);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString("rulename"));
				
				String type = "";
				if (rs.getString("ev_type").equals("1")) {
				    type = "SELECT";
				} else if (rs.getString("ev_type").equals("2")) {
				    type = "UPDATE";
				} else if (rs.getString("ev_type").equals("3")) {
				    type = "INSERT";
				} else if (rs.getString("ev_type").equals("4")) {
				    type = "DELETE";
				}
				jsonMessage.put("type", type);
				
				if (rs.getString("ev_enabled").equals("D"))
					jsonMessage.put("enabled", "false");
				else
					jsonMessage.put("enabled", "true");					
				
				jsonMessage.put("instead", Boolean.toString(rs.getBoolean("is_instead")));
				jsonMessage.put("definition", rs.getString("def"));
				
				result.add(jsonMessage);
			}
			
		return result.toString();
	}

	public String drop(int item, String ruleName) throws SQLException {
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "DROP RULE " + ruleName + " ON " + name;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String createRule(int item, ITEM_TYPE type, String ruleName,
			String event, String ruleType, String definition)
			throws SQLException {

		Database db = new Database(conn);
		String name = db.getItemFullName(item, type);

		StringBuffer command = new StringBuffer("CREATE RULE " + ruleName
				+ " AS ");
		command.append(" ON " + event);
		command.append(" TO " + name);
		command.append(" DO " + ruleType);
		command.append(" " + definition);

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command.toString());
	}

}
