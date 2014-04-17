$(document).ready(function() {
 
	var my_parking_map = new parking_map();
	my_parking_map.load_map(-37.397, 155.644);
	
	$('#submit_address').click(function(){
		
		my_parking_map.add_new_rent_marker_by_address($('#address').val());
	});
	
	$(document).on('submit','#rent_out_form', function(e){
		 e.preventDefault(e);

		var url = "/rent_out_servlet/";

		    $.ajax({
		    	url: url,
		    	dataType: 'json',
		    	type: 'post',
		    	data: {
		    		latitude: $('#latitude').val(), longitude: $('#longitude').val(), 
		    		hourly_rate: $('#hourly_rate').val(), address_value: $('#address_value').val() 
		    	},
		    success: function (result) {

		    		if(result.status == true)
		    	    {
		    			$("#rent_out_menu").html("Successfully Registered Spot for Renting!<br> Thank You!");
		    	    }
		    		else
		    		{
		    			$("#rent_out_menu").html("Spot has already been registered!");

		    		}

		        },
		    
		    	error: function(xhr, textStatus, errorThrown){
		    		alert('request failed');
		    	}
		    });	
		    
	});

	
});