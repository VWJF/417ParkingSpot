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
	private double latitude;
	private double longitude;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		this.req = req;
		this.resp = resp;
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
   		if (user == null) return;
   		
		
		String requestType = req.getParameter("request_type");
		
		switch(requestType)
		{
			case("ALL"):
				hourly_rate = Integer.parseInt(req.getParameter("hourly_rate").toString());
				returnAllParkingSpots(hourly_rate);
				break;
			default:
				break;
		}
		

	}
	
	private void returnAllParkingSpots(int hourlyRate) throws IOException
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		boolean success = false;
		Query query = new Query("parkingspot").setFilter(new FilterPredicate("hourly_rate",FilterOperator.LESS_THAN_OR_EQUAL, hourlyRate));
		
		JSONObject jsonResult = new JSONObject();
		JSONObject jsonParkingSpots = new JSONObject();
		
		try {
			List<Entity> parkingSpots = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
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
