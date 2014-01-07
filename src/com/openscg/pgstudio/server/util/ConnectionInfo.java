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
import java.sql.SQLException;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;

import com.openscg.pgstudio.shared.DatabaseConnectionException;
import com.openscg.pgstudio.shared.DatabaseConnectionException.DATABASE_ERROR_TYPE;

public class ConnectionInfo {
	
	public static long TIMEOUT = 1800000; // 30 Minutes
	
	private static String delimiter = "---";
	
	private static final String HEADER_X_FORWARDED_FOR =
	        "X-FORWARDED-FOR";

	private final String token;
	private final String clientIP;
	private final String userAgent;
	private final Date connTime;
	private Connection conn;
	
	private int databaseVersion;
	
	/* We also save original connection parameters so that we can reconnect */
	private final String databaseURL;
	private final int databasePort;
	private final String databaseName;
    private final String user;
    private final String password;
	private Date lastAccessedTime;
	
	private SecretKey secretkey;
	
	public ConnectionInfo(Connection conn, String clientIP, String userAgent,
			String databaseURL, int databasePort, String databaseName, 
            String user, String password) throws Exception {
		
		this.clientIP = clientIP;
		this.userAgent = userAgent;
		this.connTime = new Date();
		this.lastAccessedTime = new Date();
		this.conn = conn;
		this.databaseURL = databaseURL;
		this.databasePort = databasePort;
		this.databaseName = databaseName;
		this.user = user;
		this.password = password;

		this.token = generateToken();
	}

	private String generateToken() throws Exception {
		String token = "";
		
		KeyGenerator keygenerator = KeyGenerator.getInstance("Blowfish");
	    secretkey = keygenerator.generateKey();

	    Cipher cipher = Cipher.getInstance("Blowfish");
	    cipher.init(Cipher.ENCRYPT_MODE, secretkey);
	    
	    String tkStr = clientIP + delimiter + userAgent + delimiter + Long.toString(connTime.getTime());
	    byte[] encrypted = cipher.doFinal(tkStr.getBytes());
	    
	    char[] t = Hex.encodeHex(encrypted);
	    
	    token = new String(t);
		
		return token;
	}
	
	public Connection getConnection(String token, String clientIP, String userAgent) throws DatabaseConnectionException {
		// First check if the session has timed out
		Date now = new Date();
		if ((now.getTime() - lastAccessedTime.getTime()) > TIMEOUT) {
			try {
				this.conn.close();
			} catch (SQLException e) {
				throw new DatabaseConnectionException(e.getMessage());
			}
			//throw new DatabaseConnectionException("The session has timed out", DATABASE_ERROR_TYPE.TIMED_OUT);
		}
		
		// Decrypt the token so the client can be validated before returning the connection
		char[] t = token.toCharArray();
		
		String[] items;
		try {
			byte[] raw = Hex.decodeHex(t);

			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, secretkey);

			byte[] decrypted = cipher.doFinal(raw);

			String tkStr = new String(decrypted);
			items = tkStr.split(delimiter);
		} catch (Exception e) {
			throw new DatabaseConnectionException(e.getMessage());
		}
				
		

		if (!items[0].equals(clientIP)) {
			throw new DatabaseConnectionException("The client does not match the original connection", DATABASE_ERROR_TYPE.INVALID_TOKEN);
		}

		if (!items[0].equals(clientIP)) {
			throw new DatabaseConnectionException("The client does not match the original connection", DATABASE_ERROR_TYPE.INVALID_TOKEN);
		}

		if (!items[1].equals(userAgent)) {
			throw new DatabaseConnectionException("The client does not match the original connection", DATABASE_ERROR_TYPE.INVALID_TOKEN);
		}

		if (!items[2].equals(Long.toString(connTime.getTime()))) {
			// This should never happen since the connection time is a salt
			throw new DatabaseConnectionException("The client does not match the original connection", DATABASE_ERROR_TYPE.INVALID_TOKEN);
		}

		this.lastAccessedTime = new Date();
		return this.conn;
	}

	public void closeDBConnection() throws SQLException	{
		this.conn.close();
	}

	public String getToken() {
		return token;
	}
	
	protected String getDatabaseURL() {
		 return databaseURL;
	}
	
	protected int getDatabasePort() {
		return databasePort;
	}
	
	protected String getDatabaseName() {
		return databaseName;
	}
	
	protected String getUser() {
		return user;
	}
	
	protected String getPassword() {
		return password;
	}

	protected void setConnection(Connection conn)
	{
		this.conn = conn;
	}

	public int getDatabaseVersion() {
		return databaseVersion;
	}

	public void setDatabaseVersion(int databaseVersion) {
		this.databaseVersion = databaseVersion;
	}
	
	public static String remoteAddr(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		String forwarded;
		if ((forwarded = request.getHeader(HEADER_X_FORWARDED_FOR)) != null) {
			remoteAddr = forwarded;
			int idx = remoteAddr.indexOf(',');
			if (idx > -1) {
				remoteAddr = remoteAddr.substring(0, idx);
			}
		}
		return remoteAddr;
	}

}
