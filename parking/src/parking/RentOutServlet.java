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
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class RentOutServlet extends HttpServlet {
	
	private DatastoreService datastore;
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		
		// Get values
		String address = req.getParameter("address_value");
		String longitude_str = req.getParameter("longitude");
		String latitude_str = req.getParameter("latitude");

	    double longitude = Double.parseDouble(longitude_str );
	    double latitude = Double.parseDouble(latitude_str);   
	    int hourly_rate = Integer.parseInt(req.getParameter("hourly_rate"));
	    String master_key = latitude_str + "_" + longitude_str;
	    
		datastore = DatastoreServiceFactory.getDatastoreService();
		Key parkingSpotKey = KeyFactory.createKey("parkingspot", master_key);
		JSONObject resultJson = new JSONObject();
		boolean success = false;
		
		// Insert into data store if non-duplicate
		if(isDuplicateParkingSpot(parkingSpotKey) == false)
		{

			Entity parkingSpot = new Entity("parkingspot", parkingSpotKey);
			parkingSpot.setProperty("owner", user);
			parkingSpot.setProperty("address", address);
			parkingSpot.setProperty("longitude", longitude);
			parkingSpot.setProperty("latitude", latitude);
			parkingSpot.setProperty("hourly_rate", hourly_rate);
			datastore.put(parkingSpot);	
			success = true;
		}
		
		
		try {
			resultJson.put("status", success);
	        resp.setContentType("json");
			resp.getWriter().println(resultJson);     
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	private boolean isDuplicateParkingSpot(Key parkingSpotKey)
	{
        Query query = new Query("parkingspot", parkingSpotKey);
		
		List<Entity> parkingSpot = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));
		return parkingSpot.size() == 1;
	}
	
}


