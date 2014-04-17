package parking;

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
	
	private static final long serialVersionUID = 1L;
	private Date current_time = new Date(0);

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		
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

		
		//Entity parentParkingSpot = getParkingSpots(latPosition, lngPosition); //FIXME:Test without this line first
		//Key BookingKey = KeyFactory.createKey("Booking", parentParkingSpot.getKey()); //FIXME:Test without this line first


		//Create a new Booking entity.
		String booking_key = user +"_" + start_date_ms_str + "_" + end_date_ms_str;
		Key BookingKey = KeyFactory.createKey("Booking", booking_key);

		//TODO: need checking and stuff for no conflict
		Entity booking = new Entity("Booking", BookingKey);
		booking.setProperty("user", user);
		booking.setProperty("latitude", latitude);
		booking.setProperty("longitude", longitude);
		booking.setProperty("start_date_ms", start_date_ms);
		booking.setProperty("end_date_ms", end_date_ms);
		
		boolean success = false;

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(booking);
		success = true;
		JSONObject resultJson = new JSONObject();
		try {
			resultJson.put("status", success);
			resp.setContentType("json");
			resp.getWriter().println(resultJson);     
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		/*
		// Check for conflicts with other Bookings, add the Booking to the datastore if it is conflict free.
		boolean success = false;

		try{ //FIXME:Test without try{..}catch{..} line first
			if( false && isConflictFreeBooking(booking) ){

				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				datastore.put(booking);
				success = true;
			}
		}catch(IllegalArgumentException e){
			System.out.println("## Error: Booking not saved.");
			System.out.println("MakeBooking.doPost() conflictFreeBooking()"+e.getLocalizedMessage());
		}

		JSONObject resultJson = new JSONObject();
		try {
			resultJson.put("status", success);
			resp.setContentType("json");
			resp.getWriter().println(resultJson);     
		} catch (JSONException e) {
			e.printStackTrace();
		}
		*/
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
	private Entity getParkingSpots(String lat_str, String long_str)
			throws IOException {


		String parking_spot_parameters = "[lat->"+lat_str+"] [lng->"+long_str+"]";

		String master_key = lat_str + "_" + long_str;

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key ParkingSpotAppKey = KeyFactory.createKey("parkingspot", master_key);

		// Run an ancestor query to ensure we see the most up-to-date
		// view of the Greetings belonging to the selected Guestbook.

		Query query = new Query("ParkingSpot", ParkingSpotAppKey);
		query.addSort("hourly_rate", Query.SortDirection.DESCENDING);
		//Add a filter for a specific ParkingSpot(lat,lng)
		query.setFilter(FilterOperator.EQUAL.of("latitude",lat_str)).setFilter(FilterOperator.EQUAL.of("longitude",long_str));

		List<Entity> ParkingSpots = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));

		Entity firstSpot = null;
		String allParkingSpots = "";

		if (ParkingSpots.isEmpty()) {
			System.out.println(master_key + "ParkingSpots not found matching: "+parking_spot_parameters);
		} else {
			firstSpot = ParkingSpots.get(0);

			String ParkingStructure = "number, ParkingSpot(parking_spot_id, latitude, longitude, price, owner)";

			allParkingSpots = "Found ParkingSpots with parameters:" + parking_spot_parameters + "\n";
			allParkingSpots += ParkingStructure +"\n";

			int i = 0;
			for (Entity parking : ParkingSpots) {
				String lat = parking.getProperty("latitude").toString();
				String lng = parking.getProperty("longitude").toString();
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
	private boolean isConflictFreeBooking(Entity checkBooking)
			throws IOException, IllegalArgumentException {

		// TODO: Check if the provided Entity if of type Booking ??			
		//
		if (checkBooking.getKind() == "Booking"){
			throw new IllegalArgumentException("Entity Kind should be \"Booking\"");
		}

		// Retrieve information from Booking Entity 

		User booking_user = (User) checkBooking.getProperty("user");

		Entity booking_parkingSpot = (Entity) checkBooking.getProperty("parkingspot");
		Date booking_start_time = (Date) checkBooking.getProperty("start_time");
		Date booking_end_time = (Date) checkBooking.getProperty("end_time");

		String booking_lat = (String) checkBooking.getProperty("latitude");
		String booking_lng = (String) checkBooking.getProperty("longitude");

		String BookingStructure = "Booking(booking_id, username, ParkingSpot, start_Date-Time, end_Date-Time)"; 
		System.out.println(BookingStructure +"\n"
				+ booking_user + ": Booking(" + booking_user + ", ParkingSpot:(" + booking_lat + ", " + booking_lng+"), "
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
