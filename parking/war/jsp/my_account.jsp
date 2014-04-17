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
		 	String BookingQueryKind = "BookingsQuery";
		 	String ParkingSpotQueryKind = "ParkingSpotQuery";
		 	String BookingKeyKind = "Booking";
		 	String ParkingSpotKeyKind = "parkingspot";
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
            List<Entity> bookings = doQuery(BookingKeyKind, ParkingSpotName, BookingQueryKind, "startDate", "username", userName);             
                     
            List<Entity> parkingSpots = doQuery(ParkingSpotKeyKind, ParkingSpotName, ParkingSpotQueryKind, "longitude", "owner", userName);
            
            System.out.println("bookings: " + bookings.size() + ", parkingspots: " + parkingSpots.size());
            //PROCESS QUERY DATA 
            //=====================
            
            	//BOKINGS
      			//=====================
            List<Map<String, String>> bookingsList = new ArrayList<Map<String, String>>();
                        
            if (bookings.isEmpty()) {
            	System.out.println(userName + " has no bookings");
            	
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
            
				//PARKING SPOTS
            	//=====================
            List<Map<String, String>> parkingSpotsList = new ArrayList<Map<String, String>>();
                        
            if (parkingSpots.isEmpty()) {
            	System.out.println(userName + " has no parkingSpots");
            	
            } else {
            	 for (Entity parkingSpot : parkingSpots) {
            		 Map<String, String> parkingSpotMap = new HashMap<String, String>();
            		 
            		 parkingSpotMap.put("owner", parkingSpot.getProperty("owner").toString());
            		 parkingSpotMap.put("address", parkingSpot.getProperty("address").toString());
            		 parkingSpotMap.put("longitude", parkingSpot.getProperty("longitude").toString());
            		 parkingSpotMap.put("latitude", parkingSpot.getProperty("latitude").toString());
            		 parkingSpotMap.put("hourly_rate", parkingSpot.getProperty("hourly_rate").toString());
            		            		 
            		 parkingSpotsList.add(parkingSpotMap);            		             		 
            	 }
            }
            
            
            //GENERATE HTML OUTPUT
            //=====================
            String outputString = "";
            
            outputString += "<h3> Bookings </h3> ";
            if(generateHTMLOutput(bookingsList) != null)            	
            	outputString += generateHTMLOutput(bookingsList);            
            else
            	outputString += "<p>" + userName + " has no bookings </p> ";
            
            outputString += " <br> ";
            
            outputString += "<h3> Parking Spots </h3> ";
            if(generateHTMLOutput(parkingSpotsList) != null)
            	outputString += generateHTMLOutput(parkingSpotsList);
            else
            	outputString += "<p>" + userName + " has no parking Spots</p> ";
            
            pageContext.setAttribute("outputString", outputString);	
            	
            //test
            System.out.println("outputString: " + outputString);
          
          	
          	
          %>
                             
          
          <%!
          //A method that handles the query based on the parameters and
          //returns the results of the queray as a list of attributes
          List<Entity> doQuery(String keyKind, String ParkingSpotName, String QueryKind, String sortByAttribute, String filterProperty, String filterValue){
        	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	  
        	  Key Key = KeyFactory.createKey(keyKind, ParkingSpotName);
        	  // Run an ancestor query to ensure we see the most up-to-date
        	  // view of the Greetings belonging to the selected Guestbook.
        	  
        	  Query query = new Query(QueryKind, Key);
        	  query.addSort(sortByAttribute, Query.SortDirection.DESCENDING);
        	  query.setFilter(new FilterPredicate(filterProperty, FilterOperator.EQUAL, filterValue));
        	  
        	  List<Entity> attributesList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
        	  
        	  return attributesList;
          }
          %>          
          
          <%!
          //A method that takes a list of attributes and returns an 
          //html unsorted list string of these attributes
          String generateHTMLOutput(List<Map<String, String>> attributesList){
        	  String outputString = "";
        	  
        	  if(attributesList.isEmpty()){
        		  outputString = null;
        		  
        	  } else {
        		  outputString += "<ul>";
        		  for(Map<String, String> attribute : attributesList)
        		  {
        			  outputString += "<li>";
        			  outputString += "<ul>";
        			  for(Object key : attribute.keySet())
        			  {
        				  outputString += "<li>";
        				  outputString += key.toString() + ": " + attribute.get(key);
        				  outputString += "</li>";
        			  }
        			  outputString += "</u>";
        			  outputString += "</li>";
        		  }
        		  outputString += "</u>";
        	  }
        	  
        	  return outputString;
          }          
          %>
          
			
	<t:page_template>


		<jsp:attribute name="main_content">
					
			<p>Welcome user ${fn:escapeXml(userName)} </p>
			
									
			${outputString}	
			
			
 		</jsp:attribute>
 		
	</t:page_template>	
	
  
  </body>
</html>