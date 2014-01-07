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
package com.openscg.pgstudio.server.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class QueryExecutor {

	private final Connection conn;

	public QueryExecutor(Connection conn) {
		this.conn = conn;
	}

	public String Execute(String query, String queryType) {
		try {
			if (queryType.equals("SELECT")) {
				// get out because it should not come here
				return "";
			}

			PreparedStatement stmt = conn.prepareStatement(query);
			
			if (queryType.equals("COMMAND")) {
				stmt.execute();
				
				stmt.close();
				// TODO: actually return back better messages like "Table Created"
				return "Command Executed";
			}
	
			int r = stmt.executeUpdate();
			stmt.close();
			
			if (queryType.equals("INSERT")) {
				return "INSERT 0 " + r;
			}
			
			if (queryType.equals("UPDATE")) {
				return "UPDATE " + r; 
			}
			
			if (queryType.equals("DELETE")) {
				return "DELETE " + r; 
			}
			
			return "";

		} catch (SQLException e) {
			return e.getMessage();
		} catch (Exception e) {
			return "Exception";
		}
	}

	public String executeUtilityCommand(String command) {
		return executeUtilityCommand(command, null);
	}
	
	public String executeUtilityCommand(String command, String defaultReturnMessage) {

		String status = "";
		String info = "";
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(command);
			int queryResult = stmt.executeUpdate();
			
			if (queryResult == 0) {
				status = "SUCCESS";
				if (defaultReturnMessage != null) {
					info = defaultReturnMessage;
				}
			}
			else
				status = "ERROR";

			SQLWarning warning = stmt.getWarnings();
			while (warning != null) {
				info = info + warning.getMessage() + "\n";
				warning = warning.getNextWarning();
			}

		} catch (SQLException e) {
			status = "ERROR";
			info = e.getMessage();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO: Log this error
				}			
		}

		JSONArray result = new JSONArray();
		JSONObject jsonMessage = new JSONObject();
		jsonMessage.put("status", status);
		jsonMessage.put("message", info);
		result.add(jsonMessage);

		return result.toString();
	}


}
