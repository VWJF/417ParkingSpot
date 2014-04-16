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
    <link type="text/css" rel="stylesheet" href="/stylesheets/parking_map.css" />
    
    <script type="text/javascript"
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAUiC6DA0eL6xBGKpnbmvvgW8JazAZNIAM&sensor=true">
    </script>   
    <script src='http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js' type='text/javascript'></script>  
    <script src='/javascripts/parking_map.js' type='text/javascript'></script>  
    <script src='/javascripts/find_spots.js' type='text/javascript'></script>  
    
  </head>
  <body>

   
	<t:page_template>
		<jsp:attribute name="main_content">
			<form id ='search_form' action='post'>
			
				<!--  search type menu -->
				<label for='search_type'> Search by</label>
				<select id= 'search_type' name='search_type'>
 					 <option value="current_location">Current Location</option>
  					 <option value="by_address">By Address</option>
				</select>
				<input name='address_search_bar' id='address_search_bar' type='text' placeholder='Address' disabled/>
				
				<p> I want to find parking between: </p>
				<!--  Choose date -->
				
				<label for = "start_date">Start Date</label>
				<input type="date" id="start_date" required>
				<label for='start_time'> Hour</label>
				<select id ='start_time' name='start_time'>
				 	 <option value="00">00:00  </option>
 					 <option value="01">01:00  </option>
  					 <option value="02">02:00 </option>
  					 <option value="03">03:00 </option>
  					 <option value="04">04:00 </option>
  					 <option value="05">05:00 </option>
  					 <option value="06">06:00 </option>
  					 <option value="07">07:00 </option>
  					 <option value="08">08:00 </option>
  					 <option value="09">09:00 </option>
  					 <option value="10">10:00 </option>
  					 <option value="11">11:00 </option>
  					 <option value="12">12:00 </option>
  					 <option value="13">13:00 </option>
  					 <option value="14">14:00 </option>
  					 <option value="15">15:00 </option>
  					 <option value="16">16:00 </option>
  					 <option value="17">17:00 </option>
  					 <option value="18">18:00 </option>
  					 <option value="19">19:00 </option>
  					 <option value="20">20:00 </option>
  					 <option value="21">21:00 </option>
  					 <option value="22">22:00 </option>
  					 <option value="23">23:00 </option>
				</select>
				<br><br>
				<label for = "end_date">End Date&nbsp;</label>
				<input type="date" id="end_date" required>
				<label for='end_time'> Hour</label>
				<select id='end_time' name ="end_time">
 					 <option value="00">00:00  </option>
  					 <option value="02">02:00 </option>
  					 <option value="03">03:00 </option>
  					 <option value="04">04:00 </option>
  					 <option value="05">05:00 </option>
  					 <option value="06">06:00 </option>
  					 <option value="07">07:00 </option>
  					 <option value="08">08:00 </option>
  					 <option value="09">09:00 </option>
  					 <option value="10">10:00 </option>
  					 <option value="11">11:00 </option>
  					 <option value="12">12:00 </option>
  					 <option value="13">13:00 </option>
  					 <option value="14">14:00 </option>
  					 <option value="15">15:00 </option>
  					 <option value="16">16:00 </option>
  					 <option value="17">17:00 </option>
  					 <option value="18">18:00 </option>
  					 <option value="19">19:00 </option>
  					 <option value="20">20:00 </option>
  					 <option value="21">21:00 </option>
  					 <option value="22">22:00 </option>
  					 <option value="23">23:00 </option>
  					 <option value="24">24:00 </option>
				</select>
				<br><br>
				<input type="submit" value="Submit">
			</form>
			<br><br><br>
			<div id = 'map_canvas'>
			
			
			</div>	
		
		
 		</jsp:attribute>
	</t:page_template>
  
  </body>
</html>