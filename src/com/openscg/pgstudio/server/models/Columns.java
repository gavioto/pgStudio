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
import java.sql.SQLWarning;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.server.util.DBVersionCheck;
import com.openscg.pgstudio.server.util.QueryExecutor;
import com.openscg.pgstudio.server.util.DBVersionCheck.PG_FLAVORS;

public class Columns {

	private final Connection conn;

	private final PG_FLAVORS pgFlavor;

	private final static String COLUMN_LIST = "SELECT a.attname, pg_catalog.format_type(a.atttypid, a.atttypmod) as datatype, "
			+ "       a.attnotnull, a.attnum, "
			+ "       (SELECT substring(pg_catalog.pg_get_expr(d.adbin, d.adrelid) for 128) as default_value"
			+ "          FROM pg_catalog.pg_attrdef d"
			+ "         WHERE d.adrelid = a.attrelid "
			+ "           AND d.adnum = a.attnum AND a.atthasdef) as devault_value, "
			+ "       EXISTS (SELECT 1 "
			+ "                 FROM (SELECT unnest(conkey) attnum "
			+ "                         FROM pg_constraint "
			+ "                        WHERE conrelid = a.attrelid AND contype = 'p') c"
			+ "                WHERE c.attnum = a.attnum) as is_pk, "
			+ "      false as is_dk,"
			+ "      d.description "
			+ "  FROM pg_attribute a "
			+ "       LEFT OUTER JOIN pg_description d"
			+ "         ON (a.attrelid = d.objoid AND d.objsubid = a.attnum) "
			+ " WHERE a.attrelid = ? "
			+ "   AND a.attnum > 0 "
			+ "   AND NOT a.attisdropped " + " ORDER BY a.attnum ";

	private final static String COLUMN_LIST_XC = "SELECT a.attname, pg_catalog.format_type(a.atttypid, a.atttypmod) as datatype, "
			+ "       a.attnotnull, a.attnum, "
			+ "       (SELECT substring(pg_catalog.pg_get_expr(d.adbin, d.adrelid) for 128) as default_value"
			+ "          FROM pg_catalog.pg_attrdef d"
			+ "         WHERE d.adrelid = a.attrelid "
			+ "           AND d.adnum = a.attnum AND a.atthasdef) as devault_value, "
			+ "       EXISTS (SELECT 1 "
			+ "                 FROM (SELECT unnest(conkey) attnum "
			+ "                         FROM pg_constraint "
			+ "                        WHERE conrelid = a.attrelid AND contype = 'p') c"
			+ "                WHERE c.attnum = a.attnum) as is_pk, "
			+ "       EXISTS (SELECT 1"
			+ "                 FROM pgxc_class xc"
			+ "                WHERE xc.pcrelid = a.attrelid"
			+ "                  AND xc.pcattnum = a.attnum) as is_dk, "
			+ "      d.description "
			+ "  FROM pg_attribute a "
			+ "       LEFT OUTER JOIN pg_description d"
			+ "         ON (a.attrelid = d.objoid AND d.objsubid = a.attnum) "
			+ " WHERE a.attrelid = ?  "
			+ "   AND a.attnum > 0 "
			+ "   AND NOT a.attisdropped " + " ORDER BY a.attnum ";

	public Columns(Connection conn) {
		this.conn = conn;
		DBVersionCheck ver = new DBVersionCheck(conn);
		this.pgFlavor = ver.getPgFlavor();
	}

	public String getList(int item) {
		JSONArray result = new JSONArray();

		try {
			PreparedStatement stmt = null;
			if (this.pgFlavor == PG_FLAVORS.OTHER_XC
					|| this.pgFlavor == PG_FLAVORS.STORMDB) {
				stmt = conn.prepareStatement(COLUMN_LIST_XC);
			}

			if (this.pgFlavor == PG_FLAVORS.POSTGRESQL) {
				stmt = conn.prepareStatement(COLUMN_LIST);
			}

			stmt.setInt(1, item);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("name", rs.getString("attname"));
				jsonMessage.put("data_type", rs.getString("datatype"));
				jsonMessage.put("id", Integer.toString(rs.getInt("attnum")));

				if (rs.getBoolean("attnotnull")) {
					jsonMessage.put("nullable", "true");
				} else {
					jsonMessage.put("nullable", "false");
				}

				jsonMessage.put("default_value", rs.getString("devault_value"));

				jsonMessage.put("primary_key",
						Boolean.toString(rs.getBoolean("is_pk")));
				jsonMessage.put("distribution_key",
						Boolean.toString(rs.getBoolean("is_dk")));
				jsonMessage.put("comment", rs.getString("description"));

				result.add(jsonMessage);
			}

		} catch (SQLException e) {
			return "";
		}

		return result.toString();
	}

	public String drop(int item, String objectName) throws SQLException {
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "ALTER TABLE " + name + " DROP COLUMN " + objectName
				+ " RESTRICT";

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String rename(int item, String oldName, String newName)
			throws SQLException {
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String command = "ALTER TABLE " + name + " RENAME COLUMN " + oldName
				+ " TO " + newName;

		QueryExecutor qe = new QueryExecutor(conn);
		return qe.executeUtilityCommand(command);
	}

	public String create(int item, String columnName, String datatype,
			String comment, boolean not_null, String defaultval)
			throws SQLException {

		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		StringBuffer command = new StringBuffer("ALTER TABLE " + name
				+ " ADD COLUMN " + columnName + " " + datatype);

		if (not_null)
			command.append(" NOT NULL");
		if (!defaultval.equalsIgnoreCase(""))
			command.append(" DEFAULT " + defaultval);

		QueryExecutor qe = new QueryExecutor(conn);
		String result = qe.executeUtilityCommand(command.toString());

		if (comment != "") {
			String commentCommand = "COMMENT ON COLUMN " + name + "."
					+ columnName + " IS " + "$$" + comment + "$$";

			String r2 = qe.executeUtilityCommand(commentCommand);
		}

		return result;
	}

}