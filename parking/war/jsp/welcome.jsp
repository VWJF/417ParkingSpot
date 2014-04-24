<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> 
    <meta charset="utf-8">       
    <link type="text/css" rel="stylesheet" href="/stylesheets/style.css" />
    <script type="text/javascript"
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAUiC6DA0eL6xBGKpnbmvvgW8JazAZNIAM&sensor=true">
    </script>   
    <script src='http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js' type='text/javascript'></script>  
    
  </head>
  <body>

	<!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
	<noscript>
		<div
			style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
			Your web browser must have JavaScript enabled in order for this
			application to display correctly.</div>
	</noscript>

	<!--  Loads the template of the page through the use of tags  -->
    <!--  See WEB-INF/tags -->
    <!--  Things wrapped in jsp:attribute is what I pass to that template -->
   
	<t:page_template>
		<jsp:attribute name="main_content">
			<h2 >Welcome <span>to our site</span></h2>
		
			<p class="txt-2"> 
				OurParkingSpot Inc. is a startup that enables users to take advantage of the current trend of crowdsourcing system to offer a collaborative renting parking spot.
				OurParkingSpot.com will be an online service that offers a platform for guests and hosts. Where Guests can find available parking spot to rent near them and Hosts can rent out parking spots.
			</p>

		
 		</jsp:attribute>
	</t:page_template>
	
	
</body>
</html>