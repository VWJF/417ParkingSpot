function parking_map()
{
	var current = this;
	this.long;
	this.lat;
	this.map;
	
	//Load map onto canvas
	this.load_map = function(lat, long)
	{
		
		var myOptions = {
				mapTypeId: google.maps.MapTypeId.ROADMAP,
				position: new google.maps.LatLng(lat, long),
				zoom: 14,

		};
		
		current.long = long;
		current.lat = lat;
		
		console.log("lat" + lat);
		console.log("long" + long);
		

		current.map = new google.maps.Map( document.getElementById("map_canvas"), myOptions);
		current.map.setCenter( myOptions.position);

		var marker = new google.maps.Marker({
			position: new google.maps.LatLng(lat, long),
			map: current.map,
		});
	
			
	}
}


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

