// Parking Map Class, basically google map stuff

// basically call functions by doing parking_map.load_map(lat, long)
// You can do this with function with this. or prototype, 

function parking_map()
{
	var current = this;
	this.selected_marker;
	this.selected_address;
	this.map;
	this.lng;
	this.lat;
	this.geocoder;
	
	
	//Load map onto canvas
	// Privileged method but can call like a public method
	// Call this by parking_map(your instance).load_map();
	this.load_map = function(lat, lng)
	{
		current.geocoder = new google.maps.Geocoder()
		var myOptions = {
				mapTypeId: google.maps.MapTypeId.ROADMAP,
				center: new google.maps.LatLng(lat, lng),
				zoom: 14,

		};
		
		current.lng = lng;
		current.lat = lat;
		

		current.map = new google.maps.Map( document.getElementById("map_canvas"), myOptions);
		current.map.setCenter( myOptions.center);

		var marker = new google.maps.Marker({
			position: myOptions.center,
			map: current.map,
		});
			
	}
	
	this.add_info_window = function(contentString, marker)
	{
		infoWindow = new google.maps.InfoWindow({
		       maxWidth: 400,
		 });
		
		 google.maps.event.addListener(marker, 'click', function() {
			   
			   infoWindow.setContent(contentString);
			   infoWindow.open(current.map, marker);
			   current.selected_marker = marker;
			   current.lng = marker.getPosition().lng();
			   current.lat = marker.getPosition().lat();
		});
	}

}

// Creates a map onto to canvas usign persons geolocation
// Call this by parking_map(your instance).load_map_for_current_location();
parking_map.prototype.load_map_for_current_location = function()
{
	var browserSupportFlag =  new Boolean();
	var current = this;
	
	// Try W3C Geolocation (Preferred)
	if(navigator.geolocation) {
		browserSupportFlag = true;

		navigator.geolocation.getCurrentPosition(function(position) {
			current.load_map(position.coords.latitude, position.coords.longitude);
			
		}, function() {

			handleNoGeolocation(browserSupportFlag);
			
		});
	}

	// Browser doesn't support Geolocation
	else {
		browserSupportFlag = false;
		handleNoGeolocation(browserSupportFlag);
	}


	function handleNoGeolocation(errorFlag) {
		if (errorFlag == true) {
			alert("Geolocation service failed.");
		} else {
			alert("Your browser doesn't support geolocation");
		}
	}
	

}




//Finds the position of the address (long/lat)
//Call this by parking_map(your instance).add_new_marker_by_address();
parking_map.prototype.add_new_marker_by_address = function(callback) {
	
	var current = this;
	
	current.selected_address = document.getElementById('address').value;
	current.geocoder.geocode( { 'address': current.selected_address}, function(results, status) {
		
		if (status == google.maps.GeocoderStatus.OK) {
			var position_val = results[0].geometry.location;
			current.map.setCenter(position_val);

			var marker = new google.maps.Marker({
				position: position_val,
				map: current.map,
			});
			
		    var contentString =  "<div id ='rent_out_menu'>"
				 + "<p id='address_title'>" + current.selected_address + "</p><br>"
				 + "<form id='rent_out_form' action='post'>"
				 + "<input type='hidden' id ='latitude' name='latitude' value='" + position_val.lat() + "'>"
				 + "<input type='hidden' id ='longitude' name='longitude' value='" + position_val.lng()+ "'>"
				 + "<label for='hourly_rate'> Hourly Rate</label>"
				 + "<input type='number' id ='hourly_rate' name='hourly_rate' min='1' required><br><br>"
				 + "<input id ='address_value' name='address_value' type='hidden' value='" + current.selected_address  + "'><br><br>"
				 + "<input type='submit' value='Rent out spot'>"
				 + "</form>"
				 + "</div>";

		    current.add_info_window(contentString, marker);
   
		} else {
			alert('Geocode was not successful for the following reason: ' + status);
			current.selected_address = null;
		}
	});	
}



