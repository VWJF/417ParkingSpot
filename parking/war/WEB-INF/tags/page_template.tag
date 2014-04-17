<%@tag description="Overall Page template" pageEncoding="UTF-8"%>
<%@attribute name="main_content" fragment="true" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>


<div class="wrapper">

<!-- Loads template for top banner part of page -->
<t:top_banner></t:top_banner>


<div class="wrapper-mid">
<div class="mid-gap"></div>
<div class="mid-left">
	<!-- Loads the content for the middle of page inside the white area -->
	<jsp:invoke fragment="main_content"/>
</div>

<!-- Loads that dog ad-->
<t:right_ad></t:right_ad>



</div>
</div>
