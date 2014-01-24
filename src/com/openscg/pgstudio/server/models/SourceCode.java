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
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;

public class SourceCode {
	
	private final Connection conn;

	private final static String COLUMN_SOURCE = 
			"SELECT a.attname, pg_catalog.format_type(a.atttypid, a.atttypmod) as datatype, " +
			"       attnotnull, pg_get_constraintdef(ct.oid, true) as constr" +
			"  FROM pg_class c, pg_attribute a " +
			"    FULL OUTER JOIN pg_constraint ct ON " +
			"            (a.attrelid = ct.conrelid " +
			"             AND array_length(ct.conkey, 1) = 1 " +
			"             AND a.attnum = ct.conkey[1])  " +
			"  WHERE c.oid = a.attrelid " +
			"    AND c.relkind = ? " +
			"    AND c.oid = ? " +
			"    AND a.attnum > 0 " +
			"    AND NOT a.attisdropped " +
			"  ORDER BY a.attnum ";
	
	private final static String COMPOSITE_SOURCE = 
		"SELECT a.attname, pg_catalog.format_type(a.atttypid, a.atttypmod) as datatype " +
		"  FROM pg_type t, pg_class c, pg_attribute a " +
		" WHERE t.typrelid = c.oid " +
		"   AND c.oid = a.attrelid " +
		"   AND t.typtype = 'c' " +
		"   AND c.relkind = 'c' " +
		"   AND c.oid = ? " +
		"   AND a.attnum > 0 " +
		"   AND NOT a.attisdropped " +
		" ORDER BY a.attnum ";

	private final static String DOMAIN_SOURCE =
		"SELECT pg_catalog.format_type(t.typbasetype, t.typtypmod) as base_type, " +
		"       pg_catalog.array_to_string(ARRAY(" +
		"         SELECT pg_catalog.pg_get_constraintdef(r.oid, true) FROM pg_catalog.pg_constraint r WHERE t.oid = r.contypid" +
		"       ), ' ') as check ," +
		"       typdefault, typnotnull " +
		"  FROM pg_type t " +
		" WHERE t.typtype = 'd' " +
		"   AND t.oid = ? ";

	private final static String ENUM_SOURCE =
		"SELECT e.enumlabel " +
		"  FROM pg_type t, pg_enum e " +
		" WHERE t.oid = e.enumtypid " +
		"   AND t.typtype = 'e' " +
		"   AND t.oid = ? " +
		" ORDER BY e.enumsortorder ";

	private final static String FUNC_SOURCE = 
		"SELECT p.prosrc, l.lanname," +
		"       pg_catalog.pg_get_function_result(p.oid) as result_type," +
		"       pg_catalog.pg_get_function_arguments(p.oid) as arguments" +
		"  FROM pg_proc p, pg_language l" +
		" WHERE p.prolang = l.oid" +
		"   AND p.oid = ? ";

	private final static String RANGE_SOURCE = 
			"SELECT (SELECT typname FROM pg_type WHERE oid = rngsubtype) as subtype, " +
			"       (SELECT opcname FROM pg_opclass WHERE oid = rngsubopc) as opc, " +
			"       (SELECT collname FROM pg_collation WHERE oid = rngcollation) as collation, " +
			"       (SELECT proname FROM pg_proc WHERE oid = rngcanonical) as canonical, " +
			"       (SELECT proname FROM pg_proc WHERE oid = rngsubdiff) as subdiff " +
			"  FROM pg_type t, pg_range r " +
			" WHERE t.oid = r.rngtypid " +
			"   AND t.typtype = 'r' " +
			"   AND t.oid = ? ";
	
	private final static String SEQUENCE_SOURCE =
		"SELECT last_value, min_value, max_value, cache_value, " +
		"       is_cycled, increment_by, is_called " +
		"  FROM ";
	
	private final static String TYPE_KIND = 
			"SELECT relkind " +
			"  FROM pg_class " +
			" WHERE oid = ? " +
			" UNION " +
			"SELECT typtype " +
			"  FROM pg_type " +
			" WHERE oid = ? ";

	private final static String VIEW_SOURCE = 
		"SELECT pg_get_viewdef(c.oid, true) as definition " +
		"  FROM pg_class c " +
		" WHERE c.relkind = 'v' " +
		"   AND c.oid = ? ";

	private final static String GET_FOREIGN_SERVER = 
			"SELECT s.srvname " +
			"  FROM pg_foreign_table t, pg_foreign_server s " +
			" WHERE t.ftserver = s.oid " +
			"   AND t.ftrelid = ? ";
	
	private final static String GET_OPTIONS = 
			"SELECT unnest(ftoptions) AS option " +
			"  FROM pg_foreign_table " +
			" WHERE ftrelid = ? ";
	
	public SourceCode(Connection conn) {
		this.conn = conn;
	}
	
	public String getSourceCode(int item, ITEM_TYPE type) throws SQLException {
		switch (type) {
		case FOREIGN_TABLE:
			return getForeignTableCode(item);
		case FUNCTION:
			return getFunctionCode(item);
		case SEQUENCE:
			return getSequenceCode(item);
		case TABLE:
			return getTableCode(item);
		case TYPE:
			return getTypeCode(item);
		case VIEW:
			return getViewCode(item);
		default:
			break;
		}

		return "";
	}

	private String getCompositeCode(int item) throws SQLException {
		String result = "";
		
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

			PreparedStatement stmt = conn.prepareStatement(COMPOSITE_SOURCE);
			stmt.setInt(1, item);
			
			ResultSet rs = stmt.executeQuery();
			
			String atts = "";
			while (rs.next()) {
				atts = atts + "\t" + rs.getString(1) + "\t" + rs.getString(2) + ",\n";
			}
			
			if (!atts.equals("")) {
				// trim off the last ,\n
				atts =  atts.substring(0, atts.length() - 2);  
				
				result = "CREATE TYPE " + name + " AS ( \n";
				result = result + atts + "\n)";
			}
		
		return result;
	}

	private String getDomainCode(int item) throws SQLException {
		String result = "";
		
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TYPE);

			PreparedStatement stmt = conn.prepareStatement(DOMAIN_SOURCE);
			stmt.setInt(1, item);
			
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				result = "CREATE DOMAIN " + name + " AS " + rs.getString("base_type") + "\n";
				
				if (rs.getString("typdefault") != null)
					result = result + "\tDEFAULT " + rs.getString("typdefault") + "\n";					

				if (rs.getBoolean("typnotnull"))
					result = result + "\tNOT NULL\n";					
				
				result = result + "\t" + rs.getString("check");
			}
		
		return result;
	}

	private String getEnumCode(int item) throws SQLException {
		String result = "";
		
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TYPE);

			PreparedStatement stmt = conn.prepareStatement(ENUM_SOURCE);
			stmt.setInt(1, item);
			
			ResultSet rs = stmt.executeQuery();
			
			String enums = "";
			while (rs.next()) {
				enums = enums + "'" + rs.getString(1) + "',";
			}
			
			if (!enums.equals("")) {
				// trim off the last ,
				enums =  enums.substring(0, enums.length() - 1);  
				
				result = "CREATE TYPE " + name + " AS ENUM \n";
				result = result + "\t(" + enums + ")";
			}
						
		return result;
	}

	private String getForeignTableCode(int item) throws SQLException {
		String result = "";
		PreparedStatement stmt = null;
		
		try {
			Database db = new Database(conn);
			String name = db.getItemFullName(item, ITEM_TYPE.FOREIGN_TABLE);

			stmt = conn.prepareStatement(COLUMN_SOURCE);
			stmt.setString(1, "f");
			stmt.setInt(2, item);

			ResultSet rs = stmt.executeQuery();

			result = "CREATE FOREIGN TABLE " + name + " (\n";

			String columns = "";
			while (rs.next()) {
				columns = columns + "\t " + rs.getString(1);
				columns = columns + "\t " + rs.getString(2);

				if (rs.getBoolean(3)) {
					columns = columns + "\t NOT NULL";
				}

				String constraintDef = rs.getString(4);

				if (constraintDef != null) {
					if (constraintDef.toUpperCase().startsWith("PRIMARY KEY")) {
						columns = columns + "\t " + "PRIMARY KEY";
					} else if (constraintDef.toUpperCase().startsWith("UNIQUE")) {
						columns = columns + "\t " + "UNIQUE";
					} else if (constraintDef.toUpperCase().startsWith(
							"FOREIGN KEY")) {
						columns = columns
								+ "\t "
								+ constraintDef.substring(constraintDef
										.toUpperCase().indexOf("REFERENCES"));
					} else {
						columns = columns + "\t " + constraintDef;
					}
				}

				columns = columns + ",\n";
			}

			result = result + columns.substring(0, columns.lastIndexOf(","))
					+ "\n)";

			stmt.close();
			stmt = conn.prepareStatement(GET_FOREIGN_SERVER);
			stmt.setInt(1, item);

			rs = stmt.executeQuery();

			if (rs.next()) {
				result = result + "\nSERVER " + rs.getString(1);
			}
			
			stmt.close();
			stmt = conn.prepareStatement(GET_OPTIONS);
			stmt.setInt(1, item);

			rs = stmt.executeQuery();

			String options = "";
			while (rs.next()) {
				if (!options.equals(""))
					options = options + ", ";
					
				String option = rs.getString("option");
				options = options + option.split("=")[0] + " '" + option.split("=")[1] + "'";
			}

			if (!options.equals("")) {
				result = result + "\nOPTIONS (" + options + ")";
			}
			
		} finally {
			if (stmt != null)
				stmt.close();
		}
		return result;
	}

	private String getFunctionCode(int item) throws SQLException {
		String result = "";
		
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.FUNCTION);

			PreparedStatement stmt = conn.prepareStatement(FUNC_SOURCE);
			stmt.setInt(1, item);
			
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				String src = rs.getString(1);
				String lang = rs.getString(2);
				String ret = rs.getString(3);
				
				result = "CREATE FUNCTION " + name;
				result = result + " RETURNS " + ret;
				result = result + " AS $$ ";
				result = result + src;
				result = result + "$$ LANGUAGE " + lang + ";";
			}
		
		return result;
	}

	private String getRangeCode(int item) throws SQLException {
		String result = "";
		
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TYPE);

			PreparedStatement stmt = conn.prepareStatement(RANGE_SOURCE);
			stmt.setInt(1, item);
			
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				result = "CREATE TYPE " + name + " AS RANGE (\n";

				result = result + "\tSUBTYPE =" + rs.getString("subtype") + "\n";

				if (rs.getString("opc") != null)
					result = result + "\t, SUBTYPE_OPCLASS = " + rs.getString("opc") + "\n";					

				if (rs.getString("collation") != null)
					result = result + "\t, COLLATION = " + rs.getString("collation") + "\n";					

				if (rs.getString("canonical") != null)
					result = result + "\t, CANONICAL = " + rs.getString("canonical") + "\n";					

				if (rs.getString("subdiff") != null)
					result = result + "\t, SUBTYPE_DIFF = " + rs.getString("subdiff") + "\n";					

				result = result + ")";
			}
		
		return result;
	}

	private String getSequenceCode(int item) throws SQLException {
		String result = "";
		
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		String fromClause =  name;
		String sql = SEQUENCE_SOURCE + fromClause;
		
			PreparedStatement stmt = conn.prepareStatement(sql);
			
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				result = "CREATE SEQUENCE " + name + " AS\n";
				result = result + "\t INCREMENT " + Long.toString(rs.getLong(6)) + "\n";
				result = result + "\t MINVALUE " + Long.toString(rs.getLong(2)) + "\n";
				result = result + "\t MAXVALUE " + Long.toString(rs.getLong(3)) + "\n";
				result = result + "\t START " + Long.toString(rs.getLong(1)) + "\n";
				result = result + "\t CACHE " + Long.toString(rs.getLong(4)) + "\n";
				result = result + "\t ";
				
				if (!rs.getBoolean(5)) {
					result = result + "NO ";
				}
				result = result + "CYCLE\n";
			}
			
		return result;
	}

	private String getTableCode(int item) throws SQLException {
		String result = "";

		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.TABLE);

		PreparedStatement stmt = conn.prepareStatement(COLUMN_SOURCE);
		stmt.setString(1, "r");
		stmt.setInt(2, item);

		ResultSet rs = stmt.executeQuery();

		result = "CREATE TABLE " + name + " (\n";

		String columns = "";
		while (rs.next()) {
			columns = columns + "\t " + rs.getString(1);
			columns = columns + "\t " + rs.getString(2);

			if (rs.getBoolean(3)) {
				columns = columns + "\t NOT NULL";
			}

			String constraintDef = rs.getString(4);

			if (constraintDef != null) {
				if (constraintDef.toUpperCase().startsWith("PRIMARY KEY")) {
					columns = columns + "\t " + "PRIMARY KEY";
				} else if (constraintDef.toUpperCase().startsWith("UNIQUE")) {
					columns = columns + "\t " + "UNIQUE";
				} else if (constraintDef.toUpperCase()
						.startsWith("FOREIGN KEY")) {
					columns = columns
							+ "\t "
							+ constraintDef.substring(constraintDef
									.toUpperCase().indexOf("REFERENCES"));
				} else {
					columns = columns + "\t " + constraintDef;
				}
			}

			columns = columns + ",\n";
		}

		result = result + columns.substring(0, columns.lastIndexOf(","))
				+ "\n)";

		return result;
	}

	private String getTypeCode(int item) throws SQLException {
		String result = "";
		
			PreparedStatement stmt = conn.prepareStatement(TYPE_KIND);
			stmt.setInt(1, item);
			stmt.setInt(2, item);
			
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				String typeKind = rs.getString(1);
				
				if (typeKind.equalsIgnoreCase("c")) {
					result = getCompositeCode(item);
				} else if (typeKind.equalsIgnoreCase("d")) {
					result = getDomainCode(item);
				} else if (typeKind.equalsIgnoreCase("e")) {
					result = getEnumCode(item);
				} else if (typeKind.equalsIgnoreCase("r")) {
					result = getRangeCode(item);
				}

			}			
			
		
		return result;
	}

	private String getViewCode(int item) throws SQLException {
		String result = "";
		
		Database db = new Database(conn);
		String name = db.getItemFullName(item, ITEM_TYPE.VIEW);

			PreparedStatement stmt = conn.prepareStatement(VIEW_SOURCE);
			stmt.setInt(1, item);
			
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				result = "CREATE VIEW " + name + " AS\n";
				result = result + rs.getString(1);
			}
		
		return result;
	}

}
