package parking;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
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
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MakeBookingServlet extends HttpServlet {

	/**
	 * Booking(username, ParkingSpot, start_Date-Time, end_Date-Time)
	 * ParkingSpot(parking_spot_id, latitude, longitude, price, owner)
	 * 
	 */
	
	private DatastoreService datastore;
	private static final long serialVersionUID = 1L;
	private Date current_time = new Date(0);


	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		datastore = DatastoreServiceFactory.getDatastoreService();

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		this.current_time = new Date();  //Date current_time = (Date) req.getAttribute("current_time");

		//Get post values as string
		String address = req.getParameter("address_value");
		String longitude_str = req.getParameter("longitude");
		String latitude_str = req.getParameter("latitude");
		String end_date_ms_str =  req.getParameter("end_date_hours");
		String start_date_ms_str =  req.getParameter("start_date_hours");

		
		//Desired format for datastore
	    double longitude = Double.parseDouble(longitude_str );
	    double latitude = Double.parseDouble(latitude_str);
	    long end_date_ms = Long.parseLong(req.getParameter("end_date_hours"));
	    long start_date_ms = Long.parseLong(req.getParameter("end_date_hours"));

		
		//Create a new Booking entity.
		String booking_key = user +"_" + start_date_ms_str + "_" + end_date_ms_str;
		//Key BookingKey = KeyFactory.createKey("Booking", booking_key);

		String parking_key = latitude_str + "_" + longitude_str;
		Entity parentParkingSpot = getParkingSpots(latitude_str, longitude_str); 
		
		boolean success = false;

		if(parentParkingSpot == null){
			System.out.println("doPost(): ParkingSpot Not found.");
			
			generateJSONResponse(resp, success);
			return;
		}
		
		//Entity booking = new Entity("Booking", "parkingspot", parentParkingSpot.getKey());
		//Entity booking = new Entity("Booking", parentParkingSpot.getKey());

		Key ancestor_path = new KeyFactory.Builder(parentParkingSpot.getKey())
        									.addChild("Booking", booking_key)
        									.getKey();

		Entity booking = new Entity("Booking", ancestor_path); //Note: booking.getKey() is an incomplete (unusable) key until a datastore.put(booking) occurs.
		
		/*deprecated:
		 * embeddedParkingspotWithBooking(parentParkingSpot, booking);
		 * */
		
		booking.setProperty("user", user);
		//booking.setProperty("booking_key", BookingKey);
		booking.setProperty("latitude", latitude);
		booking.setProperty("longitude", longitude);
		booking.setProperty("start_date_ms", start_date_ms);
		booking.setProperty("end_date_ms", end_date_ms);
		
		
		// Check for conflicts with other Bookings, add the Booking to the datastore if it is conflict free.
		
		Key datatore_booking_key = null;

		if( isConflictFreeBooking(parentParkingSpot.getKey(), user, latitude, longitude, start_date_ms, end_date_ms ) ){
			datatore_booking_key = datastore.put(booking);
			success = true;
		}
		else{
			success = false;
		}

		
		System.out.println("Booking(" + user + ", ParkingSpot:(" + latitude + ", " + longitude+"), "
							+start_date_ms+", "+end_date_ms+") "+current_time +" : "+ parentParkingSpot.getProperty("address") );
		System.out.println("parkingSpot.getKey(): "+ KeyFactory.keyToString(parentParkingSpot.getKey()));
		try {
			System.out.println("booking.getKey() "+KeyFactory.keyToString(datatore_booking_key));
			System.out.println("booking.getKey().getParent() "+KeyFactory.keyToString( datastore.get(datatore_booking_key).getParent() ));
		} catch (EntityNotFoundException | NullPointerException e) {
			if(e instanceof NullPointerException)
				System.out.println("Booking: isConflictFreeBooking failed.");
			if(e instanceof EntityNotFoundException)
				System.out.println("Not able to retrive inserted Booking.");
		}
	
		System.out.println("doPost(): Booking complete with status = ("+success+").");
		
		//success = true;
		generateJSONResponse(resp, success);
		
	}


	/**
	 * @param resp
	 * @param success
	 * @throws IOException
	 */
	private void generateJSONResponse(HttpServletResponse resp, boolean success)
			throws IOException {
		JSONObject resultJson = new JSONObject();
		try {
			resultJson.put("status", success);
			resp.setContentType("json");
			resp.getWriter().println(resultJson);     
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * Given the identifiers of a ParkingSpot, retrieve from the Datastore the ParkingSpots that match.
	 * 
	 * ParkingSpot(parking_spot_id, latitude, longitude, price, owner)
	 * @param String latitude_str
	 * @param String longitude_str
	 * @return 
	 * @throws IOException
	 */
	private Entity getParkingSpots(String latitude_str, String longitude_str)
			throws IOException {


		String parking_spot_parameters = "[lat->"+latitude_str+"] [lng->"+longitude_str+"]";

		String master_key = latitude_str + "_" + longitude_str;

		Key ParkingSpotKey = KeyFactory.createKey("parkingspot", master_key);

		// Run an ancestor query to ensure we see the most up-to-date
		// view of the Greetings belonging to the selected Guestbook.

		Query query = new Query("parkingspot", ParkingSpotKey);
		
		
		List<Entity> parkingSpot = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));
		if(parkingSpot.size() == 1){
			Entity parking = parkingSpot.get(0);
			String owner = parking.getProperty("owner").toString();
			String addr = parking.getProperty("address").toString();
			String lat = parking.getProperty("latitude").toString();
			String lng = parking.getProperty("longitude").toString();
			String hourly_rate = parking.getProperty("hourly_rate").toString();

			String entity = "  ParkingSpot: "+ owner + " (" +lat + "," + lng + ") $" + hourly_rate + ", " + addr; 
			System.out.println(entity);
			
			return parkingSpot.remove(0);
		}
		System.out.println("  Parking Spot "+parking_spot_parameters+" Not found");
		return null;
		
	}

	/**
	 * Helper method that queries the Datastore for Booking entities and compares the results with the 
	 * provided Booking entity parameters provided. 
	 * Uses Booking attributes start_time, end_time and ParkingSpot(latitude, longitude) to determine conflicts.
	 * Return false if a conflicting Booking is found. 
	 * Returns true if a conflicting Booking is not found.
	 * @param booking_key
	 * @param booking_user
	 * @param booking_lat
	 * @param booking_lng
	 * @param booking_start_time
	 * @param booking_end_time
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	private boolean isConflictFreeBooking(Key parent_key, User booking_user, double booking_lat, double booking_lng, long booking_start_time, long booking_end_time )
			throws IOException {

		long booking_duration = booking_end_time - booking_start_time;
		assert (booking_duration >= 0);
		
		System.out.println("    Starting isConflictFreeBooking()");
		
		System.out.println("    Booking(" + booking_user + ", ParkingSpot:(" + booking_lat + ", " + booking_lng+"), "
								+booking_start_time+", "+booking_end_time+", "+current_time +")");
		System.out.println("    parent.getKey(): "+ KeyFactory.keyToString(parent_key));


		//FIXME:Currently, datastore is checked for existing Bookings with the provided parent_key for a ParkingSpot.

		Query query = new Query("Booking", parent_key);	
		List<Entity> booked_parkingSpot = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));

		if(booked_parkingSpot.size() == 0){
			System.out.println("    isConflictFreeBooking(): Previous ParkingSpot Not found.");

			return true;
		}
		System.out.println("    parkingSpot.getKey(): "+ KeyFactory.keyToString(booked_parkingSpot.get(0).getKey()));
		System.out.println("    isConflictFreeBooking(): Previous ParkingSpot Found.");

		//return false;

		
		//FIXME: Incomplete:....
		//....Problem with Filter in Query.setFilter(Filter). Filter can only operate on one property. We want to Filter by two properties (i.e. start_date & end_date)
		
		

		// Filters to compare attributes in the data store and with an attribute from provided Booking.
		// A property of a Booking entity from the Data-store is (<=,>=,==) to the property of the Booking provided.
		// TODO: Check logic (greater than vs greater than or equal).
		//Filter parkingSpotFilter =
		//		new FilterPredicate("ParkingSpot",
		//				FilterOperator.EQUAL,
		//				booking_parkingSpot);

		Filter startAboveMinFilter =
				new FilterPredicate("start_date_ms",
						FilterOperator.LESS_THAN_OR_EQUAL,
						booking_start_time);

		Filter startBelowMinFilter =
				new FilterPredicate("start_date_ms",
						FilterOperator.GREATER_THAN_OR_EQUAL,
						booking_start_time);

		Filter endAboveMaxFilter =
				new FilterPredicate("end_date_ms",
						FilterOperator.LESS_THAN_OR_EQUAL,
						booking_end_time);

		Filter endBelowMaxFilter =
				new FilterPredicate("end_date_ms",
						FilterOperator.GREATER_THAN_OR_EQUAL,
						booking_end_time);
		//////////////
		Filter startAboveMaxFilter =
				new FilterPredicate("end_date_ms",
						FilterOperator.LESS_THAN_OR_EQUAL,
						booking_start_time);

		Filter startBelowMaxFilter =
				new FilterPredicate("end_date_ms",
						FilterOperator.GREATER_THAN_OR_EQUAL,
						booking_start_time);

		Filter endAboveMinFilter =
				new FilterPredicate("start_date_ms",
						FilterOperator.LESS_THAN_OR_EQUAL,
						booking_end_time);

		Filter endBelowMinFilter =
				new FilterPredicate("start_date_ms",
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

			Filter MinRangeConflictFilter =
					CompositeFilterOperator.and(startAboveMinFilter, endAboveMinFilter);
			
			Filter MaxRangeConflictFilter =
					CompositeFilterOperator.and(startBelowMaxFilter, endAboveMinFilter);
					
		////////////////////////////////////////////////////////////
					
		//startBelowMinFilter and endAboveMinFilter
		
		Filter BookingendAboveDatastartFilter =
				new FilterPredicate("start_date_ms",
					FilterOperator.LESS_THAN_OR_EQUAL,
					booking_start_time + booking_duration);
		Filter startBeforeDataStartConflictFilter =
				CompositeFilterOperator.and(startBelowMinFilter, BookingendAboveDatastartFilter);
		
		//endAboveMaxFilter and startBelowMax
		
		Filter BookingstartBelowDataendFilter =
				new FilterPredicate("end_date_ms",
					FilterOperator.GREATER_THAN_OR_EQUAL,
					booking_start_time - booking_duration);
		Filter endAfterDataEndRangeConflictFilter =
				CompositeFilterOperator.and(endAboveMaxFilter, BookingstartBelowDataendFilter);


		//Filter conflictFilter =
		//		CompositeFilterOperator.or(parkingSpotWithStartRangeConflict, parkingSpotWithEndRangeConflict);

		Query q_start_before = new Query("Booking").setAncestor(parent_key).setFilter(startBeforeDataStartConflictFilter);
		Query q_end_after = new Query("Booking").setAncestor(parent_key).setFilter(startBeforeDataStartConflictFilter);

		PreparedQuery pq = datastore.prepare(q_start_before);
		List<Entity> result_start_before = pq.asList(FetchOptions.Builder.withLimit(10));
		pq = datastore.prepare(q_end_after);
		List<Entity> result_end_after = pq.asList(FetchOptions.Builder.withLimit(10));

		System.out.println("    Number of conflict Bookings in datastore with CurrentBooking.start_time < DatastoreBooking.start_time: "+result_start_before.size());
		System.out.println("    Number of conflict Bookings in datastore with CurrentBooking.end_time > DatastoreBooking.end_time: "+result_end_after.size());

		return ! (result_start_before.size() == 1);
//		if(pq.asIterator().hasNext())
//			return false;
//		else
//			return true;
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
	private String getCurrentTimeFormatted(){

		Date dNow = new Date();
		SimpleDateFormat ft = 
				new SimpleDateFormat ("E yyyy.MM.dd hh:mm:ss a zzz");

		System.out.println("Current Date: " + ft.format(dNow));
		return ft.format(dNow);
	}
	
	/**
	 * Alternative_#2 to ParentEntity = ParkingSpot
	 * Properties of an embedded entity are not indexed and cannot be used in queries.
	 * You can optionally associate a key with an embedded entity, but (unlike a full-fledged entity)
	 * the key is not required and, even if present, cannot be used to retrieve the entity.
	 * @param parentParkingSpot
	 * @param booking
	 */
	private void embeddedParkingspotWithBooking(Entity parentParkingSpot,
			Entity booking) {


		EmbeddedEntity embeddedParkngSpot = new EmbeddedEntity();

		embeddedParkngSpot.setKey(parentParkingSpot.getKey());
		embeddedParkngSpot.setPropertiesFrom(parentParkingSpot);

		booking.setProperty("parkingspot", embeddedParkngSpot);
	}

}
