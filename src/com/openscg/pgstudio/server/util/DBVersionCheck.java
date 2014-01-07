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
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBVersionCheck {

	private final Connection conn;

	public enum PG_FLAVORS {
		STORMDB, OTHER_XC, POSTGRESQL, UNKNOWN
    }

	private PG_FLAVORS pgFlavor;

	public DBVersionCheck(Connection conn)	{
		this.conn = conn;
	}

	public boolean isStormDB() throws SQLException	{

		if(conn.getMetaData().getDatabaseProductVersion().contains("StormDB"))	{
			return true;
		}
		else
			return false;

	}

	public boolean isXC()	{

		boolean ret = false;

		try {
			if(!isStormDB())	{
				String test = "SELECT 1 from pg_catalog.pg_class where relname = 'pgxc_class';";
				PreparedStatement stmt = conn.prepareStatement(test);
				ResultSet rs = stmt.executeQuery();

				//DB is PostgreXC
				if(rs.next())	{
					ret = true;
				}

				//DB is Vanilla Postgres
				else	{
					ret = false;
				}

			}

			//DB is StormDB which is also XC
			else
				ret = true;

		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public PG_FLAVORS getPgFlavor()	{
		try {
			if(isStormDB())
				this.pgFlavor = PG_FLAVORS.STORMDB;
			else	{
				if(isXC())
					this.pgFlavor = PG_FLAVORS.OTHER_XC;
				else
					this.pgFlavor = PG_FLAVORS.POSTGRESQL;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pgFlavor;
	}
	
	public int getVersion() {
		int version = 0;

		String query = "SELECT version()";
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String fullVerStr = rs.getString(1);
				
				String[] parts = fullVerStr.split(" ");
				String verStr = parts[1];
				
				verStr = verStr.replaceAll("\\.", "");
				
				if (Character.isDigit(verStr.charAt(0))
						&& Character.isDigit(verStr.charAt(1))
						&& !Character.isDigit(verStr.charAt(2))) {
					// We are connecting to a pre-release version of some sort.
					// Just assume is the .0 release
					verStr = verStr.substring(0, 2) + "0";
				}

				try {
				version = Integer.parseInt(verStr);
				} catch (Exception e) {
					// This is an unknown version string format so just return 0
					version = 0;
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return version;
	}

}