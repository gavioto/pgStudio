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

public class Triggers {
	
	private final Connection conn;
	
	private final static String TRIGGER_LIST = 
			"SELECT c.relname, t.tgname, t.tgdeferrable, t.tginitdeferred, " +
		"       pg_get_triggerdef(t.oid, true), t.oid " +
		"  FROM pg_trigger t, pg_class c " +
		" WHERE t.tgrelid = c.oid " +
		"   AND t.tgisinternal = false " +
		"   AND c.oid = ? " +
		" ORDER BY 1, 2 ";

	
	public Triggers(Connection conn) {
		this.conn = conn;
	}
	
	public String getList(int item) {
		JSONArray result = new JSONArray();
		
		try {
			PreparedStatement stmt = conn.prepareStatement(TRIGGER_LIST);
			stmt.setInt(1, item);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("id", Integer.toString(rs.getInt("oid")));
				jsonMessage.put("name", rs.getString(2));
								
				jsonMessage.put("deferrable", Boolean.toString(rs.getBoolean(3)));
				jsonMessage.put("init_deferrable", Boolean.toString(rs.getBoolean(4)));

				jsonMessage.put("definition", rs.getString(5));
				
				result.add(jsonMessage);
			}
			
		} catch (SQLException e) {
			return "";
		}
		
		return result.toString();
	}

	public String drop(int item, String triggerName) throws SQLException{
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "DROP TRIGGER " + triggerName + " ON " + name;
		
		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String rename(int item, String oldName, String newName) throws SQLException{
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "ALTER TRIGGER " + oldName + " ON " + name + " RENAME TO " + newName;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String createTrigger(int item, ITEM_TYPE type, String triggerName,
			String event, String triggerType, String forEach,
			String function) throws SQLException {

		Database db = new Database(conn);
		String name = db.getItemFullName(item, type);
		
		StringBuffer command = new StringBuffer("CREATE TRIGGER " + triggerName);
		command.append(" " + triggerType);
		command.append(" " + event);
		command.append(" ON " + name);
		command.append(" FOR EACH " + forEach);
		command.append(" EXECUTE PROCEDURE " + function + "()");

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command.toString());
	}

}
