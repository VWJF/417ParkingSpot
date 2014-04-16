$(document).ready(function() {

	var selected_marker;
	var selected_address;
	var map;
	//var my_parking_map = new parking_map();
	
	$('#submit_address').click(function(){
		codeAddress(add_rent_out_parking_spot_marker);
	});
	
	function initialize() {
		geocoder = new google.maps.Geocoder();
		var latlng = new google.maps.LatLng(-34.397, 150.644);
		var mapOptions = {
				zoom: 8,
				center: latlng
		}
		map = new google.maps.Map(document.getElementById('map_canvas'), mapOptions);
	}

	//Finds the position of the address (long/lat)
	function codeAddress(callback) {
		
		//remove last marker
		if(selected_marker != null)
			selected_marker.setMap(null);
		
		selected_address = document.getElementById('address').value;
		geocoder.geocode( { 'address': selected_address}, function(results, status) {
			if (status == google.maps.GeocoderStatus.OK) {
				callback(results[0].geometry.location);
				    
			
			} else {
				alert('Geocode was not successful for the following reason: ' + status);
				selected_address = null;
			}
		});	
	}
	
	function add_rent_out_parking_spot_marker(position_val)
	{
		
		map.setCenter(position_val);
		
	    selected_marker = new google.maps.Marker({
	    	map: map,
	    	position: position_val,
	    });
	    
	    
	    var contentString =  "<div id ='rent_out_menu'>"
	    					 + "<p id='address_title'>" + selected_address + "</p><br>"
	    					 + "<form id='rent_out_menu' action='post'>"
	    					 + "<input type='hidden' id ='latitude' name='latitude' value='" + position_val.latitude + "'>"
	    					 + "<input type='hidden' id ='longitude' name='longitude' value='" + position_val.longitude + "'>"
	    					 + "<label for='hourly_rate'> Hourly Rate</label>"
	    					 + "<input type='number' id ='hourly_rate' name='hourly_rate' min='1' required><br><br>"
	    					 + "<input type='submit' value='Rent spot'>"
	    					 + "</div>";

	    
	    add_info_window(contentString, selected_marker);
	    
	}
	
	function add_info_window(contentString, marker )
	{
		  var infowindow = new google.maps.InfoWindow({
		      content: contentString,
		      maxWidth: 400,
		  });
		  
		  google.maps.event.addListener(marker, 'click', function() {
			    infowindow.open(map,marker);
			    
			  });
	}
	
	google.maps.event.addDomListener(window, 'load', initialize);
});