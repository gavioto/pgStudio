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
package com.openscg.pgstudio.server;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.openscg.pgstudio.server.util.ConnectionInfo;
import com.openscg.pgstudio.server.util.ConnectionManager;
import com.openscg.pgstudio.shared.DatabaseConnectionException;

public class LoginHandler extends HttpServlet {

	private static final long serialVersionUID = -3598253864439571563L;

public void doPost(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {
    res.setContentType("text/html");

    String dbURL = req.getParameter("dbURL");
    int dbPort = Integer.parseInt(req.getParameter("dbPort"));
    String dbName = req.getParameter("dbName");
    String username = req.getParameter("username");
    String passwd = req.getParameter("password");

	String clientIP = ConnectionInfo.remoteAddr(req);
    String userAgent = req.getHeader("User-Agent");
    String queryString = req.getQueryString();
    
    String token = "";
    
    ConnectionManager connMgr = new ConnectionManager();

	HttpSession session = req.getSession(true);
    try {
		token = connMgr.addConnection(dbURL, dbPort, dbName, username, passwd, clientIP, userAgent);
	} catch (DatabaseConnectionException e) {
		session.setAttribute("err_msg", e.getMessage());
		session.setAttribute("dbName", dbName);
		session.setAttribute("dbURL", dbURL);
		session.setAttribute("username", username);
	}
    
	if (!token.equals("")) {
		session.setAttribute("dbToken", token);
		session.setAttribute("dbVersion", Integer.toString(connMgr.getDatabaseVersion()));
	}

    // Try redirecting the client to the page he first tried to access
	try {
		String target = this.getServletContext().getInitParameter("home_url");

		if (queryString != null)
			target = target + "?"+ queryString;

		res.sendRedirect(target);
		return;
	} catch (Exception ignored) {
	}

  }
}