package parking;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;


// Servlet to return entities about Bookings
@SuppressWarnings("serial")
public class BookingServlet extends HttpServlet {
	
	private HttpServletRequest req;
	private HttpServletResponse resp;
	
	private Date current_time = new Date(0);
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		this.req = req;
		this.resp = resp;

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) return;


		this.current_time = new Date();  //Date current_time = (Date) req.getAttribute("current_time");

		List<Entity> unexpiredBookings = getAllBookingsOfUser(user);

		if(unexpiredBookings == null){
			//User does not have Bookings OR User does not have unexpired Bookings
			// do nothing.

			boolean success = false; //Depending on how .js wants to interpret success = true/false		
			JSONObject jsonResult = new JSONObject();
			//JSONObject jsonBooking = new JSONObject();
			
			try{
				//jsonResult.put("bookings", jsonBooking);
				jsonResult.put("status", success);
				resp.setContentType("json");
				resp.getWriter().println(jsonResult);  
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return;
		}

		jsonPrintBookings(unexpiredBookings);	
	}
	

	/**
	 * Get all unexpired Bookings of a User. Will satisfy the condition:
	 * (current_user == datastore.user) && (current_time < datastore.startdate)		
	 * @param user
	 * @return
	 */
	public List<Entity> getAllBookingsOfUser(User user){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("Booking");
			
		if(user != null)
			query.setFilter(new FilterPredicate("user",FilterOperator.EQUAL, user));
		
		List<Entity> bookingsOfUser = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		if (bookingsOfUser.isEmpty()){
			return null;
		}
			
		List<Entity> unexpiredBookings = new ArrayList<Entity>(bookingsOfUser.size());
		for(Entity booking: bookingsOfUser){
			// String book_start_ms_str =  booking.getProperty("start_date_ms").toString();
			// String book_end_ms_str =  booking.getProperty("end_date_ms").toString();
			
			// long book_start_ms  = Long.parseLong(book_start_ms_str);
			// long book_end_ms = Long.parseLong(book_end_ms_str);
				
			Date book_start =  (Date) booking.getProperty("start_date");

			if(current_time.before(book_start)){
				unexpiredBookings.add(booking);
			}
		}
			
		if(unexpiredBookings.isEmpty())
			return null;
				
		return bookingsOfUser;	
	}
	
	/**
	 * 
	 * @param Bookings
	 * @throws IOException
	 */
	private void jsonPrintBookings(List<Entity> Bookings) throws IOException{
		boolean success = false;

		JSONObject jsonResult = new JSONObject();
		JSONObject jsonBooking = new JSONObject();
	
		try {
			int i = 0;
			for(Entity booking: Bookings){
				//Make json object containing many json objects that represent Bookings spots

				// Get values	
				User user =	(User) booking.getProperty("user");
				String start_date_str = booking.getProperty("start_date_ms").toString();
				String end_date_str = booking.getProperty("end_date_ms").toString();
				Date start = (Date) booking.getProperty("start_date");
				Date end = (Date) booking.getProperty("end_date");
				Date reservation_date = (Date) booking.getProperty("reservation_date");

				GeoPt coordinate = (GeoPt) booking.getProperty("coordinates");
				String address = booking.getProperty("address").toString();
				String latitude = booking.getProperty("latitude").toString();
				String longitude = booking.getProperty("longitude").toString();
				
				JSONObject jsonBookingContent = new JSONObject();

				// Put content of Booking spot into jsonobject
				jsonBookingContent.put("user", user.toString());
				jsonBookingContent.put("address", address);
				jsonBookingContent.put("longitude", longitude);
				//jsonBookingContent.put("latitude", String.valueOf(coordinate.getLatitude()));
				//jsonBookingContent.put("longitude", String.valueOf(coordinate.getLongitude()));
				jsonBookingContent.put("latitude", latitude);
				jsonBookingContent.put("start_date", start.toString());
				jsonBookingContent.put("end_date", end.toString());

				// Put json object into json object container for all Bookings.
				jsonBooking.put(new Integer(i).toString(), jsonBookingContent);
				i++;
			}
				
			jsonResult.put("bookings", jsonBooking);
			success = true;
			jsonResult.put("status", success);
			resp.setContentType("json");
			resp.getWriter().println(jsonResult);    
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
}
