<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
	<script src="js/jquery-2.1.4.js" ></script>
    <script src="js/search.js"></script>
    <link rel="stylesheet" type="text/css" href="css/theme.css"/>
    <script type="text/javascript">
    $(document).ready(function(){
    	$('.workshop').css('position', 'relative');
    	$('body').css('background', '#3366ff');   
    	$('body').css("background-image","url(https://dc8hdnsmzapvm.cloudfront.net/assets/images/blog/categories/580/search-engines.png?666918f)");
    });
   </script>
</head>
<body>
<section class="workshop">
	<form id="searchForm" action="search" label="SEARCH" method="GET">
	
		<input id="query" type="search" name ="query" placeholder="Search...">
		<button type="submit" value="Search">Search</button>
			<div class="clear"></div>
		<div style="margin-top: 4pc;">
		<span>TF.IDF</span>
			<input class="smallRadio" id="radio1" type="radio" name="radioOption" value="1" checked="true">
			<input class="radioText" disabled="true" type="text" value="Lucene:TF">
			<input class="smallRadio" id="radio2" type="radio" name="radioOption" value="2">
			<input class="radioText" disabled="true" type="text" value="Lucene:PR">
			<input class="smallRadio" id="radio4" type="radio" name="radioOption" value="3">
			<input class="radioText" disabled="true" type="text" value="Hadoop">
			

		</div>
	</form>
</section>
</body>
</html>