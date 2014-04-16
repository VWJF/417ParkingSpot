package com.eece417.ParkingSpot.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MakeBooking extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Date current_time = new Date(0);
	/**
	 * Booking(booking_id, username, ParkingSpot, start_Date-Time, end_Date-Time)
	 * ParkingSpot(parking_spot_id, latitude, longitude, price, owner)
	 * 
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
        
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        // We have one entity group per ParkingSpotApp with all ParkingSpot residing
        // in the same entity group as the ParkingSpotApp to which they belong.
        // This lets us run a transactional ancestor query to retrieve all
        // ParkingSpot for a given ParkingSpotApp.  However, the write rate to each
        // ParkingSpotApp should be limited to ~1/second.
        
        String ParkingSpotName = req.getParameter("ParkingSpotApp");

        String username = req.getParameter("username");
        Key BookingKey = KeyFactory.createKey("Booking", ParkingSpotName);
        
        Date start_time = (Date) req.getAttribute("start_time");
        Date end_time = (Date) req.getAttribute("end_time");
        this.current_time = new Date();  //Date current_time = (Date) req.getAttribute("current_time");


        //Get a ParkingSpot Entity from Datastore that matches (latitude, longitude)
        String latPosition =  req.getParameter("lat");
        String lngPosition =  req.getParameter("lng");
        
        Entity parkingSpot = getParkingSpots(latPosition, lngPosition, ParkingSpotName);
        
        String BookingStructure = "Booking(booking_id, username, ParkingSpot, start_Date-Time, end_Date-Time)"; 
        System.out.println(BookingStructure +"\n"
        				+ user + ": Booking(" + username + ", ParkingSpot:(" + latPosition + ", " + lngPosition+"), "
        				+start_time+", "+end_time+", "+current_time +")\n");
       
        //Create a new Booking entity.
        Entity booking = new Entity("Booking", BookingKey);
        booking.setProperty("user", user);
        booking.setProperty("username", username);
        booking.setProperty("parkingspot", parkingSpot);
        booking.setProperty("start_time", start_time);
        booking.setProperty("end_time", end_time);

        booking.setProperty("lat", latPosition);
        booking.setProperty("lng", lngPosition);
        
        // Check for conflicts with other Bookings, add the Booking to the datastore if it is conflict free.
        // TODO: Check based on start_time, end_time, current_time
        if( conflictFreeBooking(booking, ParkingSpotName) ){
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	datastore.put(booking);
        }
        
        resp.sendRedirect("/find_spots.jsp?parkingSpotName=" + ParkingSpotName);
    }

		/**
		 * 
		 * Given the identifiers of a ParkingSpot, retrieve from the Datastore the ParkingSpots that match.
		 * If multiple ParkingSpots are found, returns the first found with the lowest "price".
		 * 
		 * ParkingSpot(parking_spot_id, latitude, longitude, price, owner)
		 * @param String latPosition
		 * @param String lngPosition
		 * @param String ParkingSpotApp
		 * @return 
		 * @throws IOException
		 */
		private Entity getParkingSpots(String latPosition, String lngPosition, String ParkingSpotName)
				throws IOException {
			
	        //String ParkingSpotApp = req.getParameter("ParkingSpotApp");
	        //if (ParkingSpotApp == null) {
	        //	ParkingSpotApp = "default";
	        //}        
	        //String latPosition = req.getParameter("lat");
	        //String lngPosition = req.getParameter("lng");
	        
	        String queryParameters = "[lat->"+latPosition+"] [lng->"+lngPosition+"]";
	        
	        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	        Key ParkingSpotAppKey = KeyFactory.createKey("ParkingSpot", ParkingSpotName);
	        
	        // Run an ancestor query to ensure we see the most up-to-date
	        // view of the Greetings belonging to the selected Guestbook.
	                
	        Query query = new Query("ParkingSpot", ParkingSpotAppKey);
	        query.addSort("price", Query.SortDirection.DESCENDING);
	        //Add filter for a specific (lat,lng)
	        query.setFilter(FilterOperator.EQUAL.of("lat",latPosition)).setFilter(FilterOperator.EQUAL.of("lng",lngPosition));
	        
	        List<Entity> ParkingSpots = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
	        
	        Entity firstSpot = null;
	        String allParkingSpots = "";
	        
	        if (ParkingSpots.isEmpty()) {
	        	System.out.println(ParkingSpotName + "ParkingSpots not found matching: "+queryParameters);
	        } else {
	        	firstSpot = ParkingSpots.get(0);

	        	String ParkingStructure = "number, ParkingSpot(parking_spot_id, latitude, longitude, price, owner)";

	        	allParkingSpots = "Found ParkingSpots with parameters:" + queryParameters + "\n";
	            allParkingSpots += ParkingStructure +"\n";

	        	int i = 0;
	        	 for (Entity parking : ParkingSpots) {
	        		String lat = parking.getProperty("lat").toString();
	        		String lng = parking.getProperty("lng").toString();
	        		String price = parking.getProperty("price").toString();
	        		String owner = parking.getProperty("owner").toString();
	        		
	        		String entity = "#"+ i + "null, (" +lat + "," + lng + ") $" + price + ", " + owner; 
	        		allParkingSpots += entity +"\n";
	        		i++;
	        	 }
	        }
	        System.out.println(allParkingSpots);
	        return firstSpot;
		}
		
		/**
		 * Helper method that queries the Datastore for Booking entities and compares the results with the 
		 * provided Booking Entity. Uses Booking attributes start_time, end_time and ParkingSpot to determine conflicts.
		 * Return false if a conflicting Booking is found. 
		 * Returns true if a conflicting Booking is not found.
		 * 
		 * @param checkBooking
		 * @param ParkingSpotName
		 * @return
		 * @throws IOException
		 */
		private boolean conflictFreeBooking(Entity checkBooking, String ParkingSpotName)
				throws IOException {
	        
			// TODO: Check if the provided Entity if of type Booking ??			
			//
			
			// Retrieve information from Booking Entity 
			
			User booking_user = (User) checkBooking.getProperty("user");
			String booking_username = (String) checkBooking.getProperty("username");
			
			Entity booking_parkingSpot = (Entity) checkBooking.getProperty("parkingspot");
			Date booking_start_time = (Date) checkBooking.getProperty("start_time");
			Date booking_end_time = (Date) checkBooking.getProperty("end_time");

			String booking_lat = (String) checkBooking.getProperty("lat");
			String booking_lng = (String) checkBooking.getProperty("lng");

			 String BookingStructure = "Booking(booking_id, username, ParkingSpot, start_Date-Time, end_Date-Time)"; 
		        System.out.println(BookingStructure +"\n"
		        				+ booking_user + ": Booking(" + booking_username + ", ParkingSpot:(" + booking_lat + ", " + booking_lng+"), "
		        				+booking_start_time+", "+booking_end_time+", "+current_time +")\n");
	        	        
	        // Filters to compare attributes in the data store and with an attribute from provided Booking.
	        // A property of a Booking entity from the Data-store is (<=,>=,==) to the property of the Booking provided.
	        // TODO: Check logic (greater than vs greater than or equal).
	        Filter parkingSpotFilter =
	        		new FilterPredicate("ParkingSpot",
	        				FilterOperator.EQUAL,
	        				booking_parkingSpot);
	        
	        Filter startAboveMinFilter =
	        		new FilterPredicate("start_time",
	        				FilterOperator.LESS_THAN_OR_EQUAL,
	        				booking_start_time);

	        Filter startBelowMinFilter =
	        		new FilterPredicate("start_time",
	        				FilterOperator.GREATER_THAN_OR_EQUAL,
	        				booking_start_time);

	        Filter endAboveMaxFilter =
	        		new FilterPredicate("end_time",
	        				FilterOperator.LESS_THAN_OR_EQUAL,
	        				booking_end_time);
	        
	        Filter endBelowMaxFilter =
	        		new FilterPredicate("end_time",
	        				FilterOperator.GREATER_THAN_OR_EQUAL,
	        				booking_end_time);
	        /*****/
	        Filter startAboveMaxFilter =
	        		new FilterPredicate("end_time",
	        				FilterOperator.LESS_THAN_OR_EQUAL,
	        				booking_start_time);

	        Filter startBelowMaxFilter =
	        		new FilterPredicate("end_time",
	        				FilterOperator.GREATER_THAN_OR_EQUAL,
	        				booking_start_time);

	        Filter endAboveMinFilter =
	        		new FilterPredicate("start_time",
	        				FilterOperator.LESS_THAN_OR_EQUAL,
	        				booking_end_time);
	        
	        Filter endBelowMinFilter =
	        		new FilterPredicate("start_time",
	        				FilterOperator.GREATER_THAN_OR_EQUAL,
	        				booking_end_time);
	        
	        // Construct filters that detect conflicting Bookings. 
	        // Filters will obtain the Conflicting Bookings, 
	        // if 0 Bookings match the filters, then Conflicting Bookings DO NOT EXIST.
	        // if >0 Bookings match the filters, then Conflicting Bookings EXIST.

	        Filter startRangeConflictFilter =
	        		  CompositeFilterOperator.and(startAboveMinFilter, startBelowMaxFilter);
	        
	        Filter endRangeConflictFilter =
	        		  CompositeFilterOperator.and(endBelowMaxFilter, endAboveMinFilter);
	        

	        Filter parkingSpotWithStartRangeConflict =
	        		  CompositeFilterOperator.and(parkingSpotFilter, startRangeConflictFilter);
	        
	        Filter parkingSpotWithEndRangeConflict =
	        		  CompositeFilterOperator.and(parkingSpotFilter, endRangeConflictFilter);
	        
	        Filter conflictFilter =
	        		  CompositeFilterOperator.or(parkingSpotWithStartRangeConflict, parkingSpotWithEndRangeConflict);

	        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	        Query q = new Query("Booking").setFilter(conflictFilter);
	        PreparedQuery pq = datastore.prepare(q);
	       
	        if(pq.asIterator().hasNext())
	        	return false;
	        else
	        	return true;
		}
		
		/**
		 * Helper method that converts a (pre-formated) Date representation into a Date Object to be stored in DataStore.
		 * @param dateAsString
		 * @return
		 */
			private Date asDate(String dateAsString){
				 SimpleDateFormat sdf = 
					      new SimpleDateFormat ("E yyyy.MM.dd hh:mm:ss a zzz");
					long timeInMillisSinceEpoch = 0;
					Date date = new Date(timeInMillisSinceEpoch);
				try {
					date = sdf.parse(dateAsString);
					timeInMillisSinceEpoch = date.getTime();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    long timeInMinutesSinceEpoch = timeInMillisSinceEpoch / (60 * 1000);
			    //return timeInMinutesSinceEpoch;
			    return date;
			}
			
			/**
			 * Helper Methos that returns the current time in a (pre-formated) string representation 
			 * to be saved in the Datastore.
			 * @return
			 */
			private String getCurrentTime(){

			      Date dNow = new Date();
			      SimpleDateFormat ft = 
			      new SimpleDateFormat ("E yyyy.MM.dd hh:mm:ss a zzz");

			      System.out.println("Current Date: " + ft.format(dNow));
			      return ft.format(dNow);
			}
			
}