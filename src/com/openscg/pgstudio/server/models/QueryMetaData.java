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

public class QueryMetaData {
	
	private final Connection conn;

	public QueryMetaData(Connection conn) {
		this.conn = conn;
	}
	
	public String getMetaData(String query) {
		JSONArray ret = new JSONArray();		
		JSONObject result = new JSONObject();		
			
		String queryType = getQueryType(query);
		
		result.put("query_type", queryType);

		if (queryType.equals("SELECT")) {
			
			String sql = query;
			
			if(!query.toLowerCase().contains("limit")){
				sql = sql + " LIMIT 5";
			}

			try {
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();

				JSONArray metadata = new JSONArray();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					JSONObject jsonMessage = new JSONObject();

					jsonMessage.put("name", rs.getMetaData().getColumnName(i));
					jsonMessage.put("data_type", rs.getMetaData().getColumnType(i));

					metadata.add(jsonMessage);
				}			
				result.put("metadata", metadata);				
			} catch (SQLException e) {
				JSONArray error = new JSONArray();
				JSONObject errorMessage = new JSONObject();

				errorMessage.put("error", e.getMessage());
				errorMessage.put("error_code", e.getErrorCode());

				error.add(errorMessage);

				result.put("error", error);
			} catch (Exception e) {
				JSONArray error = new JSONArray();
				JSONObject errorMessage = new JSONObject();

				errorMessage.put("error", e.getMessage());
				errorMessage.put("error_code", -1);

				error.add(errorMessage);

				result.put("error", error);
			}
		}

		ret.add(result);
		return ret.toString();

	}

	private String getQueryType(String query) {
		String q = query.trim().toUpperCase();
		
		if (q.startsWith("SELECT")) {
			return "SELECT";
		}
		
		/*if (q.startsWith("INSERT")) {
			return "INSERT";
		}
		
		if (q.startsWith("UPDATE")) {
			return "UPDATE";
		}

		if (q.startsWith("DELETE")) {
			return "DELETE";
		}*/

		return "COMMAND";
	}
	
}
