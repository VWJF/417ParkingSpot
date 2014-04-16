$(document).ready(function() {
	
	// JS date month is 0 - 11
	var start_date;
	var end_date;
	var my_parking_map = new parking_map();
	
	set_default_dates();
	
	$("#search_form").submit(function(e){
		e.preventDefault(e);
		start_date = create_date($("#start_date").val() + "-" + $("#start_time").val());
		end_date = create_date($("#end_date").val() + "-" + $("#end_time").val());
		var is_date_valid = check_dates_valid();
		if($('#search_type').val() === 'current_location')
			my_parking_map.load_map_for_current_location();
		else
			alert('to be implemented');
	});
	
	$('#search_type').change(function(){
		if($(this).val() === "by_address")
		{
			$('#address_search_bar').removeAttr('disabled');
			
		}
		else
		{
			$('#address_search_bar').attr('disabled','disabled');		
		}
    });
	
	// Set the default date for the start and end dates to today
	function set_default_dates()
	{
		var today = new Date();
		var month = today.getMonth() + 1;
		var day  =  today.getDate();
		
		// Format into string that must have a 0 in front of it if only 1 digit
		if(month < 10)
		{
			month = "0" + month.toString();
		}
		
		if(day < 10)
		{
			day = "0" + day.toString();
		}
		
		$('#start_time').val(today.getHours());
		$('#end_time').val(today.getHours());
		today = today.getFullYear() + "-" + month + "-" +  day;
		$('#start_date').val(today);
		$('#end_date').val(today);

		
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