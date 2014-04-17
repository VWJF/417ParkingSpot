<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Query.FilterPredicate" %>
<%@ page import="com.google.appengine.api.datastore.Query.FilterOperator" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> 
    <meta charset="utf-8">       
    <link type="text/css" rel="stylesheet" href="/stylesheets/style.css" />
    <link type="text/css" rel="stylesheet" href="/stylesheets/my_account.css" />
    <script type="text/javascript"
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAUiC6DA0eL6xBGKpnbmvvgW8JazAZNIAM&sensor=true">
    </script>   
    <script src='http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js' type='text/javascript'></script>
   
    
    
  </head>
  <body>
  		 <!-- CHECKED LOGGED IN -->
		 <%
		 	//CONSTANTS
		 	//=====================
		 	String ParkingSpotName = "ParkingSpotApp";
		 	String userName = null;		 
		 	
		 	
		 	//GET USER NAME
		 	//=====================
			UserService userService = UserServiceFactory.getUserService();
        	User user = userService.getCurrentUser();
        	
      		// Successful log in then
       		if (user != null) {
       			pageContext.setAttribute("userName", user);       			
       			userName = user.toString();
       		} 
        	else {
        		response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
       		 }
      		   
      		
      		
      		//QUERY DATA FROM DATASTORE
      		//=====================
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Key BookingKey = KeyFactory.createKey("Booking", ParkingSpotName);
            
            // Run an ancestor query to ensure we see the most up-to-date
            // view of the Greetings belonging to the selected Guestbook.
                    
            Query query = new Query("ParkingSpotQuery", BookingKey);
            query.addSort("startDate", Query.SortDirection.DESCENDING);
            query.setFilter(new FilterPredicate("username", FilterOperator.EQUAL, userName));
            
            List<Entity> bookings = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
              
            
            
            //PROCESS QUERY DATA 
            //=====================
            List<Map> bookingsList = new ArrayList<Map>();
                        
            if (bookings.isEmpty()) {
            	System.out.println(userName + " has no bookings");
            	//responseHTMLString = "<div>'" + reqUsername + "' has no bookings for you.</div>";
            } else {
            	 for (Entity booking : bookings) {
            		 Map<String, String> bookingMap = new HashMap<String, String>();
            		 
            		 bookingMap.put("bookingId", booking.getProperty("bookingId").toString());
            		 bookingMap.put("parkingSpot", booking.getProperty("parkingSpot").toString());
            		 bookingMap.put("startDate", booking.getProperty("startDate").toString());
            		 bookingMap.put("endDate", booking.getProperty("endDate").toString());
            		 bookingMap.put("status", booking.getProperty("status").toString());
            		             		 
            		 bookingsList.add(bookingMap);
            		             		 
            	 }
            }
            
            
            //GENERATE HTML OUTPUT
            //=====================
            String outputString = "";
          	
            pageContext.setAttribute("outputString", outputString);
            
            outputString += "<ul>";            
            for(Map booking : bookingsList)
            {
            	outputString += "<li>";            	
            	outputString += "<ul>";            	
            	for(Object key : booking.keySet())
            	{
            		outputString += "<li>";
            		outputString += key.toString() + ": " + booking.get(key); 
            		outputString += "</li>";
            	}
            	outputString += "</u>";
            	outputString += "</li>";
            }            
            outputString += "</u>";
            
            //test
            System.out.println("outputString: " + outputString);
          %>
          
          
			
	<t:page_template>


		<jsp:attribute name="main_content">
		
			Welcome user <span id="userName" style="color:red">  </span>
			<p>Welcome user ${fn:escapeXml(userName)} </p>
									
			${outputString}
			
			
 		</jsp:attribute>
 		
	</t:page_template>
	
	<input type="hidden" id="user" value="${fn:escapeXml(user)}"/>
  
  </body>
</html>