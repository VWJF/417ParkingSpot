package parking;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import java.io.IOException;

import javax.servlet.http.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


// Servlet to return entities about ParkingSpots
// request type from http request determines the content of json object to return
@SuppressWarnings("serial")
public class ParkingServlet extends HttpServlet {
	
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private String address;
	private int hourly_rate;
	private float latitude;
	private float longitude;
	private long start_date_ms;
	private long end_date_ms;
	
	
	
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		this.req = req;
		this.resp = resp;
		
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
   		if (user == null) return;
   		
		start_date_ms = Long.parseLong(req.getParameter("start_date_ms").toString());
		end_date_ms = Long.parseLong(req.getParameter("end_date_ms").toString());

		
		String requestType = req.getParameter("request_type");
		
		switch(requestType)
		{
			case("ALL"):
				hourly_rate = Integer.parseInt(req.getParameter("hourly_rate").toString());
				List<Entity> parkingSpots = getConflictFreeSpots();
				jsonPrintParkingSpots(parkingSpots);
				break;
			default:
				break;
		}
		

	}
	
	private List<Entity> getConflictFreeSpots() throws IOException
	{
		MakeBookingServlet bookingServlet = new MakeBookingServlet();
		List<Entity> parkingSpots = getAllParkingSpots(new FilterPredicate("hourly_rate",FilterOperator.LESS_THAN_OR_EQUAL, hourly_rate));	
		List<Entity> bookings = bookingServlet.getAllBookings(null, null);
		List<Entity> conflictBookings = new ArrayList<Entity>();
		bookingServlet.checkConflicts(conflictBookings, bookings, start_date_ms, end_date_ms);

		for(int i = 0; i < parkingSpots.size(); i++)
		{
			
			float spotLng = Float.parseFloat(parkingSpots.get(i).getProperty("longitude").toString());
			float spotLat = Float.parseFloat(parkingSpots.get(i).getProperty("latitude").toString());

			for(int j = 0; j < conflictBookings.size(); j++)
			{	
				float bookLng = Float.parseFloat(conflictBookings.get(j).getProperty("longitude").toString());
				float bookLat = Float.parseFloat(conflictBookings.get(j).getProperty("latitude").toString());
		
				System.out.println("spot lat " + spotLat );
				System.out.println("spot long " + spotLng);
				System.out.println("book lat " + bookLat );
				System.out.println("book long " + bookLng);
				System.out.println("END==========");

				if(bookLng == spotLng && bookLat == spotLat)
				{

					parkingSpots.remove(i);
					conflictBookings.remove(j);

					i--;
					j--;
					break;
				}
			}
		}

		return parkingSpots;
	}
	
	
	private List<Entity> getAllParkingSpots(FilterPredicate predicate) throws IOException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query("parkingspot");
		
		if(predicate != null)
			query.setFilter(predicate);
		
		List<Entity> parkingSpots = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		return parkingSpots;
	}
	
	private void jsonPrintParkingSpots(List<Entity> parkingSpots) throws IOException
	{
		boolean success = false;

		JSONObject jsonResult = new JSONObject();
		JSONObject jsonParkingSpots = new JSONObject();

		try {
			int i = 0;
			for(Entity parkingSpot: parkingSpots)
			{
				//Make json object containing many json objects that represent parking spots

				// Get values
				String owner = parkingSpot.getProperty("owner").toString();
				String address = parkingSpot.getProperty("address").toString();
				String longitude = parkingSpot.getProperty("longitude").toString();
				String latitude = parkingSpot.getProperty("latitude").toString();
				String hourly_rate = parkingSpot.getProperty("hourly_rate").toString();
				JSONObject jsonParkingSpotContent = new JSONObject();

				// Put content of parking spot into jsonobject
				jsonParkingSpotContent.put("owner", owner);
				jsonParkingSpotContent.put("address", address);
				jsonParkingSpotContent.put("longitude", longitude);
				jsonParkingSpotContent.put("latitude", latitude);
				jsonParkingSpotContent.put("hourly_rate", hourly_rate);

				// Put json object into json object container for all parking spots
				jsonParkingSpots.put(new Integer(i).toString(), jsonParkingSpotContent);
				i++;

			}
			
			jsonResult.put("parking_spots", jsonParkingSpots);
			success = true;
			jsonResult.put("status", success);
			resp.setContentType("json");
			resp.getWriter().println(jsonResult);    
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	

	
}
