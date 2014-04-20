package parking;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DeleteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    	
    	String key_str = request.getParameter("key");
    	
    	String[] parts = key_str.split(" ");
    	String kind = parts[0]; 
    	String name = parts[1]; 
    	
    	System.out.println("kind: " + kind + "  name: " + name);
    	
    	if(kind.equals("Booking")){
    		Key key = KeyFactory.createKey(kind, name);
        	
        	Query query = new Query(kind, key);
        	List<Entity> parkingSpot = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
        	System.out.println("Query size!!!: " + parkingSpot.size());
    	}
    	else if(kind.equals("parkingspot")){
    		Key key = KeyFactory.createKey(kind, name);
        	
        	Query query = new Query(kind, key);
        	List<Entity> parkingSpot = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
        	System.out.println("Query size!!!: " + parkingSpot.size());
        	datastore.delete(parkingSpot.get(0).getKey());  
    	}
    	
    	
    	
    	

        try {
        	response.sendRedirect("/my_account/");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

}