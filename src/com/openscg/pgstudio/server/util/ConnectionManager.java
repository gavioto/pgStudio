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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;

import com.openscg.pgstudio.shared.DatabaseConnectionException;
import com.openscg.pgstudio.shared.DatabaseConnectionException.DATABASE_ERROR_TYPE;

public class ConnectionManager {
	private static Hashtable<String, ConnectionInfo> connTable = new Hashtable<String, ConnectionInfo>();
	
	private int databaseVersion;
	
	public Connection getConnection(String token, String clientIP, String userAgent) throws DatabaseConnectionException {
		ConnectionInfo info = connTable.get(token);
		
		Connection conn = info.getConnection(token, clientIP, userAgent);
		
		/* See if it got closed on us and retry creating */
		if (conn != null)
		{
			try {
				if (conn.isClosed())
				{
					conn = null;
				}
			} catch (SQLException e) {
				// ignore
				conn = null;
			}
		}
		
		if (conn == null)
		{
			try {
				conn = createConnection(info.getDatabaseURL(), info.getDatabasePort(), 
						info.getDatabaseName(), info.getUser(), info.getPassword());
			} catch (DatabaseConnectionException e) {
				throw new DatabaseConnectionException("Disconnected and could not create a connection. Try again.");
			}
			
			info.setConnection(conn);
		}
		return info.getConnection(token, clientIP, userAgent);
	}

	public void closeConnection(String token, String clientIP, String userAgent) throws DatabaseConnectionException {

		ConnectionInfo info = connTable.get(token);
		try {
			info.closeDBConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * 
	 */
	private Connection createConnection(String databaseURL, int databasePort, String databaseName, 
			                        String user, String password) throws DatabaseConnectionException 
	{
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new DatabaseConnectionException(e.getMessage());
		} 

		Connection conn = null;
		String url = "jdbc:postgresql://" + databaseURL + ":" + 
		             Integer.toString(databasePort) + "/" +
		             databaseName;
		
		String sslSuffix = "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		try {
			// Try to connect using SSL first
			conn = DriverManager.getConnection(url + sslSuffix, user, password);
		} catch (SQLException e) {
			try {
				// If SSL fails, try a clear connection
				conn = DriverManager.getConnection(url, user, password);
			} catch (SQLException e1) {
				throw new DatabaseConnectionException(e1.getMessage(),
						DATABASE_ERROR_TYPE.INVALID_CONNECTION_STRING);
			}
		}
		
		if (conn == null) {
			throw new DatabaseConnectionException("Unable to connect to Database server", DATABASE_ERROR_TYPE.INVALID_CONNECTION_STRING);
		}
		
		return conn;
	}

	/*
	 * 
	 */
	public String addConnection(String databaseURL, int databasePort, String databaseName, 
			                        String user, String password, String clientIP, 
			                        String userAgent) throws DatabaseConnectionException {
		
		//TODO: Validate all incoming parameters
		
		Connection conn = createConnection(databaseURL, databasePort, databaseName, user, password);
		
		// Setup the connection
		try {
			PreparedStatement stmt = conn.prepareStatement("SET application_name TO 'pgStudio'");
			stmt.execute();
		} catch (SQLException e) {
			throw new DatabaseConnectionException(e.getMessage());
		}
		
		ConnectionInfo info;
		try {
			info = new ConnectionInfo(conn, clientIP, userAgent, databaseURL, databasePort,
					databaseName, user, password);
			
			DBVersionCheck check = new DBVersionCheck(conn);
			this.databaseVersion = check.getVersion();
			info.setDatabaseVersion(databaseVersion);
		} catch (Exception e) {
			throw new DatabaseConnectionException(e.getMessage());
		}

		String token = info.getToken();
		
		connTable.put(token, info);
		
		return token;				
	}

	public int getDatabaseVersion() {
		return databaseVersion;
	}
}
