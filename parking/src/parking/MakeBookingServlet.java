package parking;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
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


		//Desired format for datastore
		float longitude = Float.parseFloat(longitude_str);
		float latitude = Float.parseFloat(latitude_str);
		long start_date_ms = Long.parseLong(req.getParameter("start_date_hours"));
		long end_date_ms = Long.parseLong(req.getParameter("end_date_hours"));

		Date start = new Date(start_date_ms);
		Date end = new Date(end_date_ms);

		System.out.println("Start: "+start+" End: "+end);

		//Create a new Booking entity.
		String booking_key = user +"_" + start_date_ms_str + "_" + end_date_ms_str;

		Entity parentParkingSpot = getParkingSpots(latitude_str, longitude_str); 

		boolean success = false;

		if(parentParkingSpot == null){
			System.out.println("doPost(): ParkingSpot Not found.");

			generateJSONResponse(resp, success);
			return;
		}

		Key ancestor_path = new KeyFactory.Builder(parentParkingSpot.getKey())
		.addChild("Booking", booking_key)
		.getKey();

		Entity booking = new Entity("Booking", ancestor_path); //Note: booking.getKey() is an incomplete (unusable) key until a datastore.put(booking) occurs.

		/*use deprecated?:
		 * embeddedParkingspotWithBooking(parentParkingSpot, booking);
		 * */

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
		booking.setProperty("coordinates", new GeoPt(latitude, longitude));


		// Check for conflicts with other Bookings, add the Booking to the datastore if it is conflict free.

		Key datatore_booking_key = null;

		if( isNonConflictBooking(parentParkingSpot.getKey(), booking, user, latitude, longitude, start_date_ms, end_date_ms ) ){
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
			Entity booked = datastore.get(datatore_booking_key);
			System.out.println("booking.getKey().getParent() "+KeyFactory.keyToString( datastore.get(datatore_booking_key).getParent() ));
			System.out.println("booking.getKey() "+KeyFactory.keyToString(datatore_booking_key));
			System.out.println("Booked.Date() [start,end]: "+(Date) booked.getProperty("start_date")+", "+(Date) booked.getProperty("end_date"));
			System.out.println("Booked.GeoPt(): "+(GeoPt) booked.getProperty("coordinates"));


		} catch (EntityNotFoundException | NullPointerException e) {
			if(e instanceof NullPointerException)
				System.out.println("Booking: isConflictFreeBooking blocked datastore.put(). "+ e.getLocalizedMessage());
			if(e instanceof EntityNotFoundException)
				System.out.println("Not able to retrive inserted Booking.");
		}

		System.out.println("doPost(): Booking complete. Respond with status = ("+success+").");
		System.out.println();
		
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
	 * Finds (NON)conflicts.
	 * return 'false' if new booking detected a conflict, 'true' otherwise.
	 * Uses Booking attributes start_time, end_time and ParkingSpot(latitude, longitude) to determine conflicts.
	 * // interpretation of false if a the new booking WILL cause conflict with existing Booking. 
	 * // interpretation of true if a the new booking WILL NOT cause conflict with existing Booking.
	 * @param new_booking
	 * @param booking_user
	 * @param booking_lat
	 * @param booking_lng
	 * @param booking_start_time
	 * @param booking_end_time
	 * @param booking_key
	 * @return 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	private boolean isNonConflictBooking(Key parent_key, Entity new_booking, User booking_user, float booking_lat, float booking_lng, long booking_start_time, long booking_end_time )
			throws IOException {

		if(new_booking == null) return false;
		
		Date book_start = new Date(booking_start_time); Date book_end = new Date(booking_end_time); //In case implementation changes to use Date Object.

		System.out.println("    Starting isConflictFreeBooking()");

		System.out.println("    Booking(" + booking_user + ", ParkingSpot:(" + booking_lat + ", " + booking_lng+"), "
				+booking_start_time+", "+booking_end_time+", "+current_time +")");
		System.out.println("    parkingSpot.getKey(): "+ KeyFactory.keyToString(parent_key));

		// If the Parent ParkingSpot does not exist, there is no use in trying to make a Booking.
		Query query = new Query("Booking", parent_key);	
		List<Entity> booked_parkingSpot = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));

		if(booked_parkingSpot.size() == 0){
			System.out.println("    isConflictFreeBooking(): Parent ParkingSpot Not found.");

			return true;
		}
		System.out.println("    booking.getKey(): "+ KeyFactory.keyToString(booked_parkingSpot.get(0).getKey()));
		System.out.println("    booking.getParent(): "+ KeyFactory.keyToString(booked_parkingSpot.get(0).getParent()));
		System.out.println("    isConflictFreeBooking(): Previous ParkingSpot Found.");

		// My understanding of USAGE OF FILTER PREDICATE: FilterPredicate("start_date_ms", FilterOperator.GREATER_THAN_OR_EQUAL, booking_start_time);
		// Filter Entities such that 
		// datastore Entities with property "start_date_ms" are
		// are GREATER_THAT_OR_EQUAL to the value of booking_start_time

		/////Find elements that lie outside [d1, d2] inclusive
		// Equation: start_time < @d2 AND @d1 < end_time.
		// Query 1: start_time < @d2
		// QUery 2: @d1 < end_time
		// Query+Filter will return all entities satisfying the condition.
		// Obtaining a result from the query means that the proposed 
		// Booking(d1, d2) overlaps with [datastore.start, datastore.end] and 
		// does cause a conflict.
		// Assumption: Query 1: datastore.start_time property in ASCENDING order.
		// For comparing datastore.start_date_ms < booking_end_time
		// Assumption: Query 2: datastore.end_time property in DESCENDING order
		// For comparing datastore.end_date_ms > booking_start_time

		//Looking for results of each query to be the same entry.

		/////
		// Query 1: start_time < @d2.
		///// Find entities such that datastore.start_date_ms < booking_end_time (start_time < @d2)
		Filter filter_1 =
				new FilterPredicate("start_date_ms",
						FilterOperator.LESS_THAN,
						booking_end_time);

		/////
		// Query 2: end_time > @d1.
		///// Find entities such that datastore.end_date_ms > booking_start_time (end_time > @d1)
		Filter filter_2 =
				new FilterPredicate("end_date_ms",
						FilterOperator.GREATER_THAN,
						booking_start_time);


		Query ancestor_query = new Query("Booking").setAncestor(parent_key);
		//Query 1:
		Query query_1 = new Query("Booking").setAncestor(parent_key)
				.addSort("start_date_ms", SortDirection.ASCENDING)
				.setFilter(filter_1);
		//Query 2:
		Query query_2 = new Query("Booking").setAncestor(parent_key)
				.addSort("end_date_ms", SortDirection.DESCENDING)
				.setFilter(filter_2);
		

		PreparedQuery pq_1 = datastore.prepare(query_1);
		List<Entity> results_q1 = pq_1.asList(FetchOptions.Builder.withLimit(1));
		PreparedQuery pq_2 = datastore.prepare(query_2);
		List<Entity> result_q2 = pq_2.asList(FetchOptions.Builder.withLimit(1));

		Entity conflict_before_newBooking = null, conflict_after_newBooking = null; 
		
		System.out.println("    Existing Booking.start--then--New Booking.end: "
								+results_q1.isEmpty());
		if( !results_q1.isEmpty()){
			conflict_before_newBooking = results_q1.get(0);
			System.out.println("    Conflict in datastore Existing Booking.start--then--New Booking.end: "
						+"\n#found "+ results_q1.size() +" with key " + KeyFactory.keyToString( conflict_before_newBooking.getKey() ));
			System.out.println("    start_date: " + conflict_before_newBooking.getProperty("start_date")
							+"\n    end_date: " + conflict_before_newBooking.getProperty("end_date"));
		}
		
		System.out.println("    New Booking.start--then--Existing Booking.end: "
								+result_q2.isEmpty());
		if( !result_q2.isEmpty()) {
			conflict_after_newBooking = result_q2.get(0);
			System.out.println("    Conflict Bookings in datastore New Booking.start--then--Existing Booking.end: "
						+"\n#found "+ result_q2.size() +" with key " + KeyFactory.keyToString( conflict_after_newBooking.getKey() ));
			System.out.println("    start_date: " + conflict_after_newBooking.getProperty("start_date")
							+"\n    end_date: " + conflict_after_newBooking.getProperty("end_date"));
		}
		
		// Now we have the two relevant bookings that may cause 
		// overlap before(conflict_before_newBooking) and after(conflict_after_newBooking) the new_booking.
		boolean conflictDetected = false;
		
		if( conflict_before_newBooking != null ){
			System.out.println("    isOverlapRange() detected: with datastore.start_time < newbooking: "
								+ isOverlapRange(book_start, book_end, conflict_before_newBooking));
			conflictDetected = isOverlapRange(book_start, book_end, conflict_before_newBooking);
		}
		if( conflict_after_newBooking != null ){
			System.out.println("    isOverlapRange() detected: with newbooking.start < datastore.end: "
						+ isOverlapRange(book_start, book_end, conflict_before_newBooking));
			conflictDetected = conflictDetected && isOverlapRange(book_start, book_end, conflict_after_newBooking);
		}
		
		System.out.println("    isConflictFreeBooking() response (conflict found): "+ conflictDetected);
		
		return ! conflictDetected; //Return 'false' if new booking detected a conflict, 'true' otherwise.
		
	}

	/**
	 * Returns true if (start_date, end_date) of e1 and e2 overlap.
	 * @param book_start TODO
	 * @param book_end TODO
	 * @param e2
	 * @return
	 */
	private boolean isOverlapRange(Date book_start, Date book_end, Entity e2){
		// Overlap occurs when start1 <= end2 and start2 <= end1 == true.
		// For exclusive end_date use '<' instead of '<='.
		
		//Date start1 = (Date) e1.getProperty("start_date"); 
		//Date end1 = (Date) e1.getProperty("end_date");
		Date start1 = book_start;
		Date end1 = book_end;
		Date start2 = (Date) e2.getProperty("start_date");
		Date end2 = (Date) e2.getProperty("end_date");
		System.out.println("Trying overlap.");
		if( start1.before(end2) && start2.before(end1)) 
			return true;
		return false;
	}
	
	/**
	 * Helper method that queries the Datastore for Booking entities and compares the results with the 
	 * provided Booking entity parameters provided. 
	 * Finds Conflicts.
	 * Uses Booking attributes start_time, end_time and ParkingSpot(latitude, longitude) to determine conflicts.
	 * Return false if a conflicting Booking with the provided properties IS FOUND. 
	 * Returns true if a conflicting Booking with the provided properties IS NOT FOUND.
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
	private boolean isConflictFreeBooking(Key parent_key, User booking_user, float booking_lat, float booking_lng, long booking_start_time, long booking_end_time )
			throws IOException {

		long booking_duration = booking_end_time - booking_start_time;
		assert (booking_duration >= 0);

		//In case implementation changes to use Date Object.
		Date book_start = new Date(booking_start_time); Date book_end = new Date(booking_end_time);
		Date book_start_plusduration = new Date(booking_start_time+booking_duration); Date book_end_minusduration = new Date(booking_end_time-booking_duration);

		System.out.println("    Starting isConflictFreeBooking()");

		System.out.println("    Booking(" + booking_user + ", ParkingSpot:(" + booking_lat + ", " + booking_lng+"), "
				+booking_start_time+", "+booking_end_time+", "+current_time +")");
		System.out.println("    parent.getKey(): "+ KeyFactory.keyToString(parent_key));


		// If the Parent ParkingSpot does not exist, there is no use in trying to make a Booking.
		Query query = new Query("Booking", parent_key);	
		List<Entity> booked_parkingSpot = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));

		if(booked_parkingSpot.size() == 0){
			System.out.println("    isConflictFreeBooking(): Parent ParkingSpot Not found.");

			return true;
		}
		System.out.println("    parkingSpot.getKey(): "+ KeyFactory.keyToString(booked_parkingSpot.get(0).getKey()));
		System.out.println("    isConflictFreeBooking(): Previous ParkingSpot Found.");

		//FIXME: TODO:
		// My understanding of USAGE OF FILTER PREDICATE: FilterPredicate("start_date_ms", FilterOperator.GREATER_THAN_OR_EQUAL, booking_start_time);
		// Filter Entities such that 
		// datastore Entities with property "start_date_ms" are
		// are GREATER_THAT_OR_EQUAL to the value of booking_start_time

		/////
		// Filter A: See definition in Spreadsheet.
		/////
		Filter BookingstartBelowDatastartFilter =
				new FilterPredicate("start_date_ms",
						FilterOperator.GREATER_THAN_OR_EQUAL,
						booking_start_time);
		//Filter with Date Object instead of milliseconds		
		//Filter BookingstartBelowDatastartFilter =
		//		new FilterPredicate("start_date",
		//				FilterOperator.GREATER_THAN_OR_EQUAL,
		//				book_start);		
		Filter BookingStartDurationAboveDatastartFilter =
				new FilterPredicate("start_date_ms",
						FilterOperator.LESS_THAN_OR_EQUAL,
						booking_start_time + booking_duration);
		//Filter with Date Object instead of milliseconds			
		//Filter BookingStartDurationAboveDatastartFilter =
		//		new FilterPredicate("start_date",
		//			FilterOperator.LESS_THAN_OR_EQUAL,
		//			book_start_plusduration);

		Filter startDurationDataStartConflictFilter =
				CompositeFilterOperator.and(BookingstartBelowDatastartFilter, BookingStartDurationAboveDatastartFilter);

		/////
		// Filter B: See definition in Spreadsheet.
		/////
		Filter BookingendAboveDataendFilter =
				new FilterPredicate("end_date_ms",
						FilterOperator.LESS_THAN_OR_EQUAL,
						booking_end_time);
		//Filter with Date Object instead of milliseconds			
		//Filter BookingendAboveDataendFilter =
		//		new FilterPredicate("end_date_ms",
		//				FilterOperator.LESS_THAN,
		//				book_end);

		Filter BookingEndDurationAboveDataendFilter =
				new FilterPredicate("end_date_ms",
						FilterOperator.GREATER_THAN_OR_EQUAL,
						booking_end_time-booking_duration);
		//Filter with Date Object instead of milliseconds			
		//Filter BookingEndDurationAboveDataendFilter =
		//		new FilterPredicate("end_date",
		//				FilterOperator.GREATER_THAN_OR_EQUAL,
		//				booking_end_minusduration);
		Filter endDurationDataStartConflictFilter =
				CompositeFilterOperator.and(BookingendAboveDataendFilter, BookingEndDurationAboveDataendFilter);


		//Filter A:
		Query q_start_before = new Query("Booking").setAncestor(parent_key).addSort("start_date_ms", SortDirection.ASCENDING).setFilter(startDurationDataStartConflictFilter);
		//Filter B:
		Query q_end_after = new Query("Booking").setAncestor(parent_key).addSort("end_date_ms", SortDirection.ASCENDING).setFilter(endDurationDataStartConflictFilter);


		PreparedQuery pq_before = datastore.prepare(q_start_before);
		List<Entity> result_start_before = pq_before.asList(FetchOptions.Builder.withLimit(10));
		PreparedQuery pq_after = datastore.prepare(q_end_after);
		List<Entity> result_end_after = pq_after.asList(FetchOptions.Builder.withLimit(10));

		System.out.println("    Number of conflict Bookings in datastore with CurrentBooking.start_time < DatastoreBooking.start_time: "
				+result_start_before.size());
		if( !result_start_before.isEmpty())
			System.out.println("    Key of conflict Bookings in datastore with CurrentBooking.start_time < DatastoreBooking.start_time: "
					+KeyFactory.keyToString(result_start_before.get(0).getKey()));
		System.out.println("    Number of conflict Bookings in datastore with CurrentBooking.end_time > DatastoreBooking.end_time: "
				+result_end_after.size());
		if( !result_end_after.isEmpty()) System.out.println("    Key of conflict Bookings in datastore with CurrentBooking.start_time < DatastoreBooking.start_time: "
				+KeyFactory.keyToString(result_end_after.get(0).getKey()));

		//Filter C: See definition in Spreadsheet. Implemented by creating HashSet of the Keys from Filter A and Filter B. 
		//Duplicate entries in HashSet are components for Filter C.
		HashSet<Key> intersection = new HashSet<Key>(result_start_before.size()+result_end_after.size());

		Iterator<Entity> components = result_start_before.iterator();
		long collisions = 0;
		while(components.hasNext()){
			intersection.add(components.next().getKey());
		}
		components = result_end_after.iterator();
		while(components.hasNext()){
			if(intersection.add(components.next().getKey()) == false)
				collisions++;
		}

		System.out.println("    Number of conflict Bookings in datastore with DatastoreBooking.start_time < CurrentBooking.*_time < DatastoreBooking.end_time: "
				+ collisions);

		System.out.println("    isConflictFreeBooking() response (proceed with booking): "+ ! (result_start_before.size() == 1 || result_end_after.size() == 1 || collisions == 1));

		return ! (result_start_before.size() == 1 || result_end_after.size() == 1 || collisions == 1);
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
