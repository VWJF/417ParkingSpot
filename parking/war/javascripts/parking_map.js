// Parking Map Class, basically google map stuff

// basically call functions by doing parking_map.load_map(lat, long)
// You can do this with function with this. or prototype, 

function parking_map()
{
	var current = this;
	this.infowindow;
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

			
	}
	
	this.add_info_window = function(contentString, marker)
	{
		infoWindow = new google.maps.InfoWindow({
		       maxWidth: 400,
		 });
		
		 google.maps.event.addListener(marker, 'click', function() {
			   
			   if(current.infowindow != null)
				{
				  current.infowindow.close();  
				}
			   
			   infoWindow.setContent(contentString);
			   infoWindow.open(current.map, marker);
			   current.infowindow = infoWindow;
			   current.selected_marker = marker;
			   current.lng = marker.getPosition().lng();
			   current.lat = marker.getPosition().lat();
		});
	}

}

// Creates a map onto to canvas using persons geolocation and load all the parking spot markers
// Call this by parking_map(your instance).find_parking_map_for_current_location
parking_map.prototype.find_parking_map_for_current_location = function(parking_info)
{
	var browserSupportFlag =  new Boolean();
	var current = this;
	
	// Try W3C Geolocation (Preferred)
	if(navigator.geolocation) {
		browserSupportFlag = true;

		navigator.geolocation.getCurrentPosition(function(position) {
			current.load_map(position.coords.latitude, position.coords.longitude);
			current.load_parking_spots_onto_map(parking_info.reserveMenuBuilder, parking_info.max_price, parking_info.start_date_ms, parking_info.end_date_ms);
			
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

//Load all the markers for the given parking spots with infowindwo containing the contentString built 
// By the callback functiob contentStringBuilder
parking_map.prototype.load_parking_spots_onto_map = function(contentStringBuilder, hourly_rate_val, start_date_ms_val, end_date_ms_val)
{
	var current = this;
	var url = "/parking_spot_servlet/";

	$.ajax({
		url: url,
		dataType: 'json',
		type: 'post',
		data: { request_type: "ALL", hourly_rate: hourly_rate_val, start_date_ms: start_date_ms_val, end_date_ms: end_date_ms_val },
		success: function (result) {

			if(result.status == true)
			{
				current.load_parking_spot_markers(result.parking_spots, contentStringBuilder);
			}
			else
			{
				alert('Error Loading map! Try again later');
			}

		},

		error: function(xhr, textStatus, errorThrown){
			alert('request failed');
		}
	});	
}

//Load all the markers for the given parking spots with infowindow containing the contentString
parking_map.prototype.load_parking_spot_markers = function(parking_spots, contentStringBuilder)
{
	var current = this;
	
	$.each(parking_spots, function( index, spot ) {

		var marker = new google.maps.Marker({
			position: new google.maps.LatLng(spot.latitude, spot.longitude),
			map: current.map,
		});
		
		var contentString = contentStringBuilder(spot);
		
	    current.add_info_window(contentString, marker);

	});
	
}



//Finds the position of the address (long/lat) for registering/renting out a spot
//Call this by parking_map(your instance).add_new_marker_by_address();
parking_map.prototype.add_new_register_marker_by_address = function(address_val) {
	
	var current = this;
	
	current.selected_address = address_val;
	current.geocoder.geocode( { 'address': current.selected_address}, function(results, status) {
		
		if (status == google.maps.GeocoderStatus.OK) {
			var position_val = results[0].geometry.location;
			current.map.setCenter(position_val);

			var marker = new google.maps.Marker({
				position: position_val,
				map: current.map,
			});
		    var contentString =  "<div id ='register_menu'>"
				 + "<p id='address_title'>" + current.selected_address + "</p><br>"
				 + "<form id='register_form' action='post'>"
				 + "<input type='hidden' id ='latitude' name='latitude' value='" + position_val.lat() + "'>"
				 + "<input type='hidden' id ='longitude' name='longitude' value='" + position_val.lng()+ "'>"
				 + "<label for='hourly_rate'> Hourly Rate $ </label>"
				 + "<input type='number' id ='hourly_rate' name='hourly_rate' min='1' value='1' required><br><br>"
				 + "<input id ='address_value' name='address_value' type='hidden' value='" + current.selected_address  + "'>"
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

// Find the parking spots near the given address to reserve
parking_map.prototype.find_parking_spots_nearby_address = function(parking_info) {
	
	var current = this;
	
	current.selected_address = parking_info.address;
	current.geocoder.geocode( { 'address': current.selected_address}, function(results, status) {
		
		if (status == google.maps.GeocoderStatus.OK) {
			var position_val = results[0].geometry.location;
			current.map.setCenter(position_val);
			current.load_parking_spots_onto_map(parking_info.reserveMenuBuilder, parking_info.max_price, parking_info.start_date_ms, parking_info.end_date_ms);


		} else {
			alert('Geocode was not successful for the following reason: ' + status);
			current.selected_address = null;
		}
	});	
}

