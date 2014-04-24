<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> 
    <meta charset="utf-8">       
    <link type="text/css" rel="stylesheet" href="/stylesheets/style.css" />
    <link type="text/css" rel="stylesheet" href="/stylesheets/register_spot.css" />
    <link type="text/css" rel="stylesheet" href="/stylesheets/parking_map.css" />
    <script type="text/javascript"
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAUiC6DA0eL6xBGKpnbmvvgW8JazAZNIAM&sensor=true">
    </script>   
    <script src='http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js' type='text/javascript'></script>  
    <script src='javascripts/parking_map.js' type='text/javascript'></script>  
    <script src='javascripts/register_spot.js' type='text/javascript'></script>  
	    
  </head>
  <body>

	<!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
	<noscript>
		<div
			style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
			Your web browser must have JavaScript enabled in order for this
			application to display correctly.</div>
	</noscript>


	<!-- CHECKED LOGGED IN -->
		 <% 
			UserService userService = UserServiceFactory.getUserService();
        	User user = userService.getCurrentUser();

      		// Successful log in then
       		if (user != null) {
       	      pageContext.setAttribute("user", user);
       		 } 
        	else {
        		response.sendRedirect(userService.createLoginURL(request.getRequestURI()));

       		 }
          %>		

	<t:page_template>
		<jsp:attribute name="main_content">
		    <label for='address'>Address of your parking Spot</label><br>
			<input type='text' id='address' name='address'>
			<button type='button' id='submit_address'>Search</button>
			
			<br><br>
			<div id='map_canvas'></div>
		
 		</jsp:attribute>
	</t:page_template>
  
  </body>
</html>