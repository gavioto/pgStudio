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

public class ItemData {
	
	private final Connection conn;

	public ItemData(Connection conn) {
		this.conn = conn;
	}
	
	public String getData(int item, ITEM_TYPE type, int count) throws SQLException {
		if (type != ITEM_TYPE.TABLE && type != ITEM_TYPE.VIEW && type != ITEM_TYPE.FOREIGN_TABLE) {
			return "";
		}

		Database db = new Database(conn);
		String name = db.getItemFullName(item, type);

		JSONArray ret = new JSONArray();		
		JSONObject result = new JSONObject();		
			
		String sql = "SELECT * FROM " + name + " LIMIT " + count; 
		
			try {
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
								
				JSONArray resultset = new JSONArray();
				while (rs.next()) {
					JSONObject jsonMessage = new JSONObject();
	
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						String col = "c" + i;
						String val = "";
						
						if (rs.getObject(i) != null) {
							switch (rs.getMetaData().getColumnType(i)) {
								case -5:   /* BIGINT */
								case 4:    /* INT */
								case 5:    /* SMALLINT */
									val = Long.toString(rs.getLong(i));
									break;
								case -7:   /* BINARY */
								case 16:   /* BOOLEAN */
									if (rs.getBoolean(i)) {
										val = "true";					
									} else {
										val = "false";										
									}
									break;
								case 12 :  /* VARCHAR */
									val = rs.getString(i);
									break;
								case 91 : /* DATE */
									val = rs.getDate(i).toString();
									break;
								case 92 : /* TIME */
									val = rs.getTime(i).toString();
									break;
								case 93 : /* TIMESTAMP */
									val = rs.getTimestamp(i).toString();
									break;
								default :
									val = rs.getObject(i).toString();
							}
						}
						
						jsonMessage.put(col, val);	
					}
									
					resultset.add(jsonMessage);
				}
				result.put("resultset", resultset);			
		} catch (SQLException e) {
			return "";
		} catch (Exception e) {
			return "";
		}
		
		ret.add(result);
		return ret.toString();

	}

	public String getExplainResult(String query){
		String JsonQuery = "EXPLAIN (format json) " + query; 
				
		String explain = "";

		try {
			PreparedStatement stmt = conn.prepareStatement(JsonQuery);
			ResultSet rs = stmt.executeQuery();

			while (rs.next())
				explain += rs.getString(1);

		} catch (SQLException e) {
			return "";
		}

		return explain;
	}
}
