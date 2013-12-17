<!doctype html>
<!-- The DOCTYPE declaration above will set the    -->
<!-- browser's rendering engine into               -->
<!-- "Standards Mode". Replacing this declaration  -->
<!-- with a "Quirks Mode" doctype may lead to some -->
<!-- differences in layout.                        -->

<html>
  <head>
  <!--[if IE 8]> <meta http-equiv="X-UA-Compatible" content="IE=8"> <![endif]-->
  <!--[if IE 9]> <meta http-equiv="X-UA-Compatible" content="IE=9"> <![endif]-->
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">

    <!--                                                               -->
    <!-- Consider inlining CSS to reduce the number of requested files -->
    <!--                                                               -->
    <link type="text/css" rel="stylesheet" href="PgStudio.css">

    <title>PostgreSQL Studio</title>
    
    <link rel="icon" type="image/x-icon" href="images/favicon.ico" />
      
    <% 
        String dbToken = null;
        if(session != null) {
            dbToken = (String) session.getAttribute("dbToken");
        }
    
        if(dbToken==null) { 
        	dbToken="";
    %>
    
    </head>
        
 <body class="login">
  <div class="content grid">
        <div id="headerlarge">
        <h1></h1>
          <p></p>
      </div>
  
   
  <div class="studiocontainer thinbase ">
    <div class="studiocontainer-header">
      <h3>Connect</h3>
    </div>
    <div class="studiocontainer-body">
            
<% 
        String err_msg = (String) session.getAttribute("err_msg");
    
        if(err_msg!=null) { 
%>
        <div class="msg-error">Error: <%= err_msg %></div>
<%
        }
%>
      <div class="connect">
        <form name="sdb_11_11_28" class="connectform" method="post" action="login">
            <fieldset>
                <div class="row clearfix">
                    <label class="connectlabel " for="id_db_host">Database Host</label>
                    <div class="connectlabel span1">
                        <input id="dbURL" tabindex="1" maxlength="100" type="text" class="required" name="dbURL" value="<%=((request.getSession().getAttribute("dbURL") == null || "".equals(request.getSession().getAttribute("dbURL"))) ? "" : request.getSession().getAttribute("dbURL")) %>">
                    </div>
                </div>
            
                <div class="row clearfix ">
                    <label class="connectlabel" for="id_db_port">Database Port</label>
                    <div class="span1">
                        <input id="dbPort" tabindex="2" maxlength="5" type="text" class="required" name="dbPort" value="<%=((request.getSession().getAttribute("dbPort") == null || "".equals(request.getSession().getAttribute("dbPort"))) ? "5432" : request.getSession().getAttribute("dbPort")) %>">
                    </div>
                </div>

                <div class="row clearfix ">
                    <label class="connectlabel" for="id_db_name">Database Name</label>
                    <div class="span1">
                        <input id="dbName" tabindex="3" maxlength="64" type="text" class="required" name="dbName" value="<%=((request.getSession().getAttribute("dbName") == null || "".equals(request.getSession().getAttribute("dbName"))) ? "" : request.getSession().getAttribute("dbName")) %>">
                    </div>
                </div>

                <div class="row clearfix ">
                    <label class="connectlabel" for="id_user_name">Username</label>
                    <div class="span1">
                        <input id="username" tabindex="4" maxlength="30" type="text" class="required" name="username" value="<%=((request.getSession().getAttribute("username") == null || "".equals(request.getSession().getAttribute("username"))) ? "" : request.getSession().getAttribute("username")) %>">
                    </div>
                </div>

                <div class="row clearfix ">
                    <label class="connectlabel" for="id_password">Password</label>
                    <div class="span1">
                        <input tabindex="5" maxlength="30" type="password" name="password" id="password" class="required">
                    </div>
                </div>
              </fieldset>
            
              <div class="pull-right">
                  <input class="btn primary wide" type="submit" tabindex="6" value="Login" />
              </div>
        </form>
      </div>
    </div>
  </div>
	<% } else { %>
	
	<script> 
      var dbToken = "<%= (String) session.getAttribute("dbToken") %>"; 
      var dbVersion = "<%= (String) session.getAttribute("dbVersion") %>"; 
    </script> 
	<!--                                           -->
    <!-- This script loads your compiled module.   -->
    <!-- If you add any GWT meta tags, they must   -->
    <!-- be added before this line.                -->
    <!--                                           -->
    <script type="text/javascript" language="javascript" src="com.openscg.pgstudio.PgStudio/com.openscg.pgstudio.PgStudio.nocache.js"></script>
  </head>

  <body class="studioBody">

    <!-- OPTIONAL: include this if you want history support -->
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
    <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
      </div>
    </noscript>

  <div class="studiocontainer widebase ">
    <div class="studiocontainer-header">
      <h3></h3>
    </div>
    <div class="studiocontainer-body">


    <div id="loadingWrapper" style="height: 90px;">
      <div id="loading">
        <div class="loadingIndicator" style="padding-top: 10px;">
          <img src="images/logo.png" style="margin-right:8px;float:left;vertical-align:top;"/><br/>
          <img src="images/spinner.gif" style="margin-right:8px;float:left;vertical-align:top;"/>
          <span id="loadingMsg">Loading styles and images...</span><br/>
        </div>
      </div>
    </div>

    <div id="mainPanelContainer"></div>

    </div>
    </div>
    <span id="version">PostgreSQL Studio v1.2</span>

	<% } %>
	
    </div>		
  </body>
</html>
