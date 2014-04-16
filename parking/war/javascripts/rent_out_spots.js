$(document).ready(function() {

	var my_parking_map = new parking_map();
	my_parking_map.load_map(-37.397, 155.644);
	
	$('#submit_address').click(function(){
		my_parking_map.add_new_marker_by_address();
	});
	
	

	
});