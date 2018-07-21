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
 <form name="report" method="post" action="getTimeSeriesData">

 <table border='1' width="80">

	<tr>
		
		<td>Enter a session key</td>
		<td><input type="text" name="sessionkey" value="5ad8ad0ef6e86a7d0a23814d"></td>
		
	</tr>
		<tr>
		
		<td>Enter a json</td>
		<td><textarea name="json" rows="10", cols="80">{ "asset_id" : "AssetGas_2", "device_id" : ["67241219"], "site_id" : "gas_messages_temp", "from_date" : "01/12/2018", "to_date" : "04/23/2018", "type" : "All", "sub_type" : "All", "deviceCategoryName" : "Gas" }</textarea></td>
		
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
