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
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	    System.out.println("yahoo");

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		String address = req.getParameter("address_value");
		String longitude_str = req.getParameter("longitude");
		String latitude_str = req.getParameter("latitude");

	    double longitude = Double.parseDouble(longitude_str );
	    double latitude = Double.parseDouble(latitude_str);   
	    int hourly_rate = Integer.parseInt(req.getParameter("hourly_rate"));
	    String master_key = latitude_str + "_" + longitude_str;
	    		
		Key guestbookKey = KeyFactory.createKey("parkingspot", "default");
		Entity parkingSpot = new Entity("parkingspot", guestbookKey);

	    System.out.println(master_key);

		parkingSpot.setProperty("master_key", master_key);
		parkingSpot.setProperty("owner", user);
		parkingSpot.setProperty("address", address);
		parkingSpot.setProperty("longitude", longitude);
		parkingSpot.setProperty("latitude", latitude);
		parkingSpot.setProperty("hourly_rate", hourly_rate);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(parkingSpot);	
		JSONObject resultJson = new JSONObject();
		
		try {
			resultJson.put("status", true);
			resp.getWriter().println(resultJson);     
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
}
