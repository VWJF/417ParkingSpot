$(document).ready(function() {
	
	// JS date month is 0 - 11
	var start_date;
	var end_date;
	var my_parking_map = new parking_map();
	
	my_parking_map.load_map(-37.397, 155.644);
	
//	$('#submit_address').click(function(){
//		my_parking_map.add_new_marker_by_address();
//	});
	
	set_default_dates();
	
	$("#search_form").submit(function(e){
		e.preventDefault(e);
		start_date = create_date($("#start_date").val() + "-" + $("#start_time").val());
		end_date = create_date($("#end_date").val() + "-" + $("#end_time").val());
		var is_date_valid = check_dates_valid();
		
		if($('#search_type').val() === 'current_location')
			my_parking_map.load_map_for_current_location();
		else
			my_parking_map.add_new_marker_by_address();	
			//alert('to be implemented');
	});
	
	
	$('#search_type').change(function(){
		if($(this).val() === "by_address")
		{
			$('#address_search_bar').removeAttr('disabled');
			var address_from_searchbar = $('#address_search_bar').val(); 
			$('#address').attr("name", address_from_searchbar);
			
		}
		else
		{
			$('#address_search_bar').attr('disabled','disabled');		
		}

    });
	
	
	$(document).on('submit','#rent_out_form', function(e){
		 e.preventDefault(e);

		var url = "/make_booking_servlet/";

		    $.ajax({
		    	url: url,
		    	dataType: 'json',
		    	type: 'post',
		    	data: {
		    		latitude: $('#latitude').val(), longitude: $('#longitude').val(), 
		    		hourly_rate: $('#hourly_rate').val(), start_time: $('#start_time').val,
		    		end_time: $('#end_time').val
		    	},
		    success: function (result) {

		    		if(result.status == true)
		    	    {
		    			$("#rent_out_menu").html("Successfully Made Booking!<br> Thank You!")
		    	    }

		        },
		    
		    	error: function(xhr, textStatus, errorThrown){
		    		alert('request failed');
		    	}
		    });	
		    
	});
	
	// Set the default date for the start and end dates to today
	function set_default_dates()
	{
		var today = new Date();
		var month = today.getMonth() + 1;
		var day  =  today.getDate();
		var hours = today.getHours();
		
		// Format into string that must have a 0 in front of it if only 1 digit
		month = make_double_digits(month);
		day = make_double_digits(day);
		hours = make_double_digits(hours);

		$('#start_time').val(hours);
		$('#end_time').val(hours);
		today = today.getFullYear() + "-" + month + "-" +  day;
		$('#start_date').val(today);
		$('#end_date').val(today);

		
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
	
	function check_dates_valid()
	{
		var is_date_valid = true;
		
		if(start_date > end_date)
		{
			is_date_valid = false;
			alert("Your start date can't be greater than your end date!");
		}
		
		var today_date = new Date();
		today_date.setHours(today_date.getHours(), 0, 0, 0);

		if(start_date < today_date)
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
		
		return new Date(year, month, day, hours);


	}
});