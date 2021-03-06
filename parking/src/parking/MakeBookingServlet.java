package parking;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

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

		if (user == null) return;


		this.current_time = new Date();  //Date current_time = (Date) req.getAttribute("current_time");

		//Get post values as string
		String address = req.getParameter("address_value");
		String longitude_str = req.getParameter("longitude");
		String latitude_str = req.getParameter("latitude");
		String end_date_ms_str =  req.getParameter("end_date_hours");
		String start_date_ms_str =  req.getParameter("start_date_hours");

		System.out.println("long str" + longitude_str);
		System.out.println("lat str" + latitude_str);


		//Desired format for datastore
		float longitude = Float.parseFloat(longitude_str);
		float latitude = Float.parseFloat(latitude_str);
		long start_date_ms = Long.parseLong(req.getParameter("start_date_hours"));
		long end_date_ms = Long.parseLong(req.getParameter("end_date_hours"));
		GeoPt coordinate = new GeoPt(latitude, longitude);
		Date start = new Date(start_date_ms);
		Date end = new Date(end_date_ms);

	

		Entity parentParkingSpot = getParkingSpot(latitude_str, longitude_str); 
		List<Entity> conflictBookings = new ArrayList<Entity>();
		boolean success = false;
		
		List<Entity> bookingsForSpot = getAllBookings(new FilterPredicate("coordinates",FilterOperator.EQUAL, coordinate), parentParkingSpot);

		if(parentParkingSpot == null ){
			System.out.println("Parent ParkingSpot Not found.");
			generateJSONResponse(resp, success, "parking spot not found");
		}
		else if(checkConflicts(conflictBookings, bookingsForSpot, start_date_ms, end_date_ms))
		{
			System.out.println("Conflict exists!");
			generateJSONResponse(resp, success, "conflict exists");
		}
		else
		{			
			String booking_key = user +"_" + start_date_ms_str + "_" + end_date_ms_str +  "_" + address;
			System.out.println("MakeBooking\nKind: Booking  Name: "+booking_key);
			
			//Entity booking = new Entity("Booking", booking_key, parentParkingSpot.getKey() ); //Note: booking.getKey() is an incomplete (unusable) key until a datastore.put(booking) occurs.

			Entity booking = new Entity("Booking", booking_key); //Note: booking.getKey() is an incomplete (unusable) key until a datastore.put(booking) occurs.
			
			System.out.println("Booking Key: "+booking.getKey());
			System.out.println("Booking Key String: "+KeyFactory.keyToString(booking.getKey()));

			booking.setProperty("user", user);
			booking.setProperty("latitude", latitude);
			booking.setProperty("longitude", longitude);
			booking.setProperty("start_date_ms", start_date_ms);
			booking.setProperty("end_date_ms", end_date_ms);
			booking.setProperty("reservation_date_ms", current_time.getTime());
			booking.setProperty("address", address);
			booking.setProperty("start_date", start);
			booking.setProperty("end_date", end);
			booking.setProperty("reservation_date", current_time);
			booking.setProperty("coordinates", coordinate);
			datastore.put(booking);
			success = true;
			generateJSONResponse(resp, success, "it was successful, no error");
		}

	}


	/**
	 * @param resp
	 * @param success
	 * @throws IOException
	 */
	private void generateJSONResponse(HttpServletResponse resp, boolean success, String errorMsg)
			throws IOException {
		JSONObject resultJson = new JSONObject();
		try {
			resultJson.put("status", success);
			resultJson.put("error", errorMsg);
			resp.setContentType("json");
			resp.getWriter().println(resultJson);     
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	// Get the parking spot for the given coordinates
	public Entity getParkingSpot(String latitude_str, String longitude_str)
			throws IOException {

		String master_key = latitude_str + "_" + longitude_str;
		Entity parkingSpot = null;
		Key ParkingSpotKey = KeyFactory.createKey("parkingspot", master_key);

		// Run an ancestor query to ensure we see the most up-to-date
		// view of the Greetings belonging to the selected Guestbook.
		Query query = new Query("parkingspot", ParkingSpotKey);		

		List<Entity> parkingSpotQuery = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));
		if(parkingSpotQuery.size() == 1){
			parkingSpot = parkingSpotQuery.get(0);
		}
		
		if( parkingSpot != null){
			System.out.println("Ancestor PakringSpot Key: "+parkingSpot.getKey());
			System.out.println("Ancestor PakringSpot KeyString: "+KeyFactory.keyToString(parkingSpot.getKey()));
		}
		
		return parkingSpot;
	}

	// Get all bookings with a given predicate
	public List<Entity> getAllBookings(FilterPredicate predicate, Entity ancestor)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("Booking");
		if(ancestor != null)
			query.setAncestor(ancestor.getKey());
		
		if(predicate != null)
			query.setFilter(predicate);
		
		List<Entity> Bookings = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		return Bookings;	
	}

	// See if any conflicts exist at the given time interval for the given parking spots
	public boolean checkConflicts(List<Entity> conflictBookings, List<Entity> bookingsForSpot, long start_ms, long end_ms)
	{
		boolean isConflict = false ;

		//Iterate through and see if a conflict exists
		for(Entity booking: bookingsForSpot)
		{
			String book_start_ms_str =  booking.getProperty("start_date_ms").toString();
			String book_end_ms_str =  booking.getProperty("end_date_ms").toString();
			
			long book_start_ms  = Long.parseLong(book_start_ms_str);
			long book_end_ms = Long.parseLong(book_end_ms_str);

			if(start_ms < book_end_ms && end_ms > book_start_ms)
			{
				isConflict = true;
				conflictBookings.add(booking);
			}

		}

		return isConflict;
	}



}
