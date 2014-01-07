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

public class Stats {

	private final Connection conn;

	private final static String ITEM_KIND = "SELECT c.relkind"
			+ "  FROM pg_class c " + " WHERE c.oid = ? ";

	private final static String INDEX_STATS = "SELECT pg_size_pretty(pg_total_relation_size(relid)) as \"Total Index Size\", "
			+ "       idx_scan as \"Index Scans\", "
			+ "       idx_tup_read \"Rows Read\" "
			+ "  FROM pg_stat_user_indexes" + " WHERE indexrelid = ? ";

	private final static String SEQUENCE_STATS = "SELECT last_value as \"Last Value\", min_value as \"Minimum Value\", "
			+ "       max_value as \"Maximum Value\", cache_value as \"Cache Value\", "
			+ "       increment_by as \"Increment By\", is_cycled as \"Is Cycled\", "
			+ "       is_called as \"Is Called\" " + "  FROM ";

	private final static String TABLE_STATS = "SELECT pg_size_pretty(pg_total_relation_size(relid)) as \"Total Table Size\", "
			+ "       seq_scan as \"Full Table Scans\", "
			+ "       idx_scan as \"Index Scans\", "
			+ "       n_tup_ins \"Number of Rows Inserted\", "
			+ "       n_tup_upd + n_tup_hot_upd as \"Number of Rows Updated\", "
			+ "       n_tup_del as \"Number of Rows Deleted\", "
			+ "       n_live_tup as \"Number of Rows\" "
			+ "  FROM pg_stat_user_tables" + " WHERE relid = ? ";

	public Stats(Connection conn) {
		this.conn = conn;
	}

	public String getList(int item) throws SQLException {
		JSONArray result = new JSONArray();

		PreparedStatement stmt = conn.prepareStatement(ITEM_KIND);
		stmt.setInt(1, item);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String kind = rs.getString(1);

			ResultSet rsStats = null;

			if (kind.equals("r")) {
				PreparedStatement stmtStats = conn
						.prepareStatement(TABLE_STATS);
				stmtStats.setInt(1, item);
				rsStats = stmtStats.executeQuery();
			} else if (kind.equals("S")) {
				Database db = new Database(conn);
				String name = db.getItemFullName(item, ITEM_TYPE.SEQUENCE);

				String sql = SEQUENCE_STATS + name;
				PreparedStatement stmtStats = conn.prepareStatement(sql);
				rsStats = stmtStats.executeQuery();
			} else if (kind.equals("i")) {
				PreparedStatement stmtStats = conn
						.prepareStatement(INDEX_STATS);
				stmtStats.setInt(1, item);
				rsStats = stmtStats.executeQuery();
			}

			if (rsStats != null) {
				while (rsStats.next()) {
					for (int i = 1; i <= rsStats.getMetaData().getColumnCount(); i++) {
						JSONObject jsonMessage = new JSONObject();
						jsonMessage.put("name", rsStats.getMetaData()
								.getColumnName(i));

						int t = rsStats.getMetaData().getColumnType(i);
						int x = t;
						switch (rsStats.getMetaData().getColumnType(i)) {
						case -5: /* BIGINT */
						case 4: /* INT */
						case 5: /* SMALLINT */
							jsonMessage.put("value",
									Long.toString(rsStats.getLong(i)));
							break;
						case -7: /* BINARY */
						case 16: /* BOOLEAN */
							if (rsStats.getBoolean(i)) {
								jsonMessage.put("value", "true");
							} else {
								jsonMessage.put("value", "false");
							}
							break;
						case 12: /* VARCHAR */
							jsonMessage.put("value", rsStats.getString(i));
							break;
						}
						result.add(jsonMessage);
					}
				}
			}

		}

		return result.toString();
	}
}
