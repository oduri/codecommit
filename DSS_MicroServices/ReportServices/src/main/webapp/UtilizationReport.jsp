<!doctype html>
<html lang="en">
 <head>
  <meta charset="UTF-8">
  <meta name="Generator" content="EditPlus®">
  <meta name="Author" content="">
  <meta name="Keywords" content="">
  <meta name="Description" content="">
  <title>Document</title>
 </head>
 <body>
 <form name="report" method="post" action="generateUtilizationForSite">
  <p align="center">for json request for overall:{ "site_id" : "LisleMolex", "from_date" : "05/01/2018", "to_date" : "05/31/2018", "period" : "overall" } </p>
  <p align="center">for json request for Month:{ "site_id" : "LisleMolex", "from_date" : "05/01/2018", "to_date" : "05/31/2018", "period" : "month" } </p>
  <p align="center">for json request for day:{ "site_id" : "LisleMolex", "from_date" : "05/01/2018", "to_date" : "05/31/2018", "period" : "day" } </p>
  <p align="center">for json request for array of assets:{ "site_id" : "LisleMolex", "from_date" : "05/01/2018", "to_date" : "05/31/2018", "period" : "month", "asset_id": ["InletCompressor_3170","InletCompressor_3171"] } </p>
  
 <table border='1' width="80">

	<tr>
		
		<td>Enter a session key</td>
		<td><input type="text" name="sessionkey" value="5ad8ad0ef6e86a7d0a23814d"></td>
		
	</tr>
		<tr>
		
		
		<td>Enter a json</td>
		<td><textarea name="json" rows="10", cols="80">{ "site_id" : "LisleMolex", "from_date" : "05/01/2018", "to_date" : "05/31/2018", "period" : "overall" }</textarea></td>
		
	</tr>
	
	<!-- <tr>
		
		<td>Enter a deviceCategoryName</td>
		<td><input type="text" name="deviceCategoryName" value="Vibration"></td>
		
	</tr> -->
	
	<tr>
		<td colspan="2"> <input type="submit" value="clickme" /></td>
	</tr>
	

</table>

 </form>

 </body>
</html>
