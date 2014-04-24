$(document).ready(function() {
	
	// JS date month is 0 - 11
	// Date includes year, month, day, hours
	var start_date_hours;
	var end_date_hours;
	var my_parking_map = new parking_map();
	
	 
	set_default_dates();
	
	// Submit search form to find spots
	$("#search_form").submit(function(e){
		e.preventDefault(e);

		if(check_dates_valid())
		{
			var parking_info = new Object();
			parking_info.start_date_ms = start_date_hours;
			parking_info.end_date_ms = end_date_hours;
			parking_info.max_price = $('#price_range').val();
			parking_info.reserveMenuBuilder  = reservation_menu_content_builder;
			parking_info.address = $('#address').val();
			
			if($('#search_type').val() === 'current_location')
			{
				my_parking_map.find_parking_map_for_current_location(parking_info);
			}
			else
			{
				my_parking_map.load_map(-37.397, 155.644);
				my_parking_map.find_parking_spots_nearby_address(parking_info);
			}
		}
	});
	
	//Submit Reservation form for spot from infowindow
	$(document).on('submit','#reservation_form', function(e){
		e.preventDefault(e);
		if(check_dates_valid())
		{
			var url = "/make_booking/";

			$.ajax({
				url: url,
				dataType: 'json',
				type: 'post',
				data: {
					latitude: $('#latitude').val(), 
					longitude: $('#longitude').val(), 
					address_value: $('#address_value').val(),
					end_date_hours: end_date_hours,
					start_date_hours: start_date_hours
				},
				success: function (result) {

					if(result.status == true)
					{
						alert('Successfully added Booking!'); //Successfully added Booking to datastore.
					}
					else
					{
						alert(result.error); //Failed to add Booking to datastore. Servlet Error.
					}

				},

				error: function(xhr, textStatus, errorThrown){
					alert('request failed');
				}
			});	
		}

	});
	
	$('#search_type').change(function(){
		if($(this).val() === "by_address")
		{
			$('#address').removeAttr('disabled');
			
		}
		else
		{
			$('#address').attr('disabled','disabled');		
		}
    });
	
	// Set the default date for the start and end dates to today
	function set_default_dates()
	{
		var today = new Date();
		var month = today.getMonth() + 1;
		var day  =  today.getDate();
		var hours = (today.getHours() + 1) % 24;
		
		// Format into string that must have a 0 in front of it if only 1 digit
		month = make_double_digits(month);
		day = make_double_digits(day);
		var start_hours = make_double_digits(hours);
		var end_hours = make_double_digits(hours + 1);
		$('#start_time').val(start_hours);
		$('#end_time').val(end_hours);
		today = today.getFullYear() + "-" + month + "-" +  day;
		$('#start_date').val(today);
		$('#end_date').val(today);
		
	}
	
	function reservation_menu_content_builder (parking_spot)
	{
		var contentString =  "<div id ='reserve_spot_menu'>"
			+ "<h4 id='address_title'>" + parking_spot.address + "</h4><br>"
			+ "<p> Owned by " + parking_spot.owner + "</p>" 
			+ "<p> Hourly Rate: $" + parking_spot.hourly_rate + "</p>"
			+ "<form id='reservation_form' action='post'>"
			+ "<input type='submit' value='Reserve Spot'>"
			+ "<input type='hidden' id ='latitude' name='latitude' value='" + parking_spot.latitude + "'>"
			+ "<input type='hidden' id ='longitude' name='longitude' value='" + parking_spot.longitude + "'>"
			+ "<input id ='address_value' name='address_value' type='hidden' value='" + parking_spot.address + "'>"
			+ "</form>"
			+ "</div>";
		
		return contentString;
	}
	
	//If only single digit, add 0 to it to make double digit
	function make_double_digits(digits)
	{
		if(digits < 10)
		{
			digits = "0" + digits.toString();
		}
		
		return digits;
	}
	
	// Build and check if the start and end dates valid
	function check_dates_valid()
	{
		var is_date_valid = true;
		start_date_hours= create_date($("#start_date").val() + "-" + $("#start_time").val());
		end_date_hours= create_date($("#end_date").val() + "-" + $("#end_time").val());
		end_date_hours = end_date_hours -1;
		
		if(start_date_hours >= end_date_hours)
		{
			is_date_valid = false;
			alert("Your start date can't be greater than or equal to your end date!");
		}
		
		var today_date = new Date();
		today_date.setHours(today_date.getHours(), 0, 0, 0);
		today_date = today_date.getTime();
		
		if(start_date_hours< today_date)
		{
			is_date_valid = false;
			alert("Start date must be in the future! Time Machines haven't been built yet")
		}
		
		return is_date_valid;
		
	}
	


	
	function log_date(date)
	{
		
		console.log("new date");
		console.log(date.getFullYear());
		console.log(date.getMonth());
		console.log(date.getDate());
		console.log(date.getHours());
	}
	
	// Create a date object in the format year(xxxx)-month-day-hour(24hr)
	function create_date(date_str)
	{
		var arr = date_str.split('-');
		var year = arr[0];
		var month = parseInt(arr[1]) - 1;
		var day = arr[2];
		var hours = arr[3];
		
		return (new Date(year, month, day, hours)).getTime();


	}
	
	
	
	
});