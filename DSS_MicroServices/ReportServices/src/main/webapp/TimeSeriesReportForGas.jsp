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
 <form name="report" method="post" action="getTimeSeriesDataForGas">

 <table border='1' width="80">

	<tr>
		
		<td>Enter a session key</td>
		<td><input type="text" name="sessionkey" value="5b1159eb52faff0006d49028"></td>
		
	</tr>
		<tr>
		
		<td>Enter a json</td>
		<td><textarea name="json" rows="10", cols="80">{ "device_id" : ["67241227"], "asset_id" : "asset_0286815", "site_id" : "buffalo", "from_date" : "06/13/2018", "to_date" : "06/13/2018", "type" : "All", "sub_type" : "All", "period" : "minute", "deviceCategoryName" :"gasold"}</textarea></td>
		
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
