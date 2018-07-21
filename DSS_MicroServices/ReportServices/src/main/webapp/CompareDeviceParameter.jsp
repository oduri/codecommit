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
 <form name="report" method="post" action="compareDeviceParameters">

 <table border='1' width="80">

	<tr>
		
		<td>Enter a session key</td>
		<td><input type="text" name="sessionkey" value="5ad8ad0ef6e86a7d0a23814d"></td>
		
	</tr>
		<tr>
		
		<td>Enter a  device json</td>
		<td><textarea name="devicejson" rows="10", cols="80">{ "compare_device_list" : ["970536179"], "gps_latitude" : 1, "device_id" : 1 }</textarea></td>
		
	</tr>
	
	<tr>
		
		<td>Enter a  channel json</td>
		<td><textarea name="channeljson" rows="10", cols="80">{ "compare_channel_list" : ["970536179_1"], "external_temperature" : 1, "channel_id" : 1 }</textarea></td>
		
	</tr>
	<tr>
		
		<td>Enter a  deviceCategoryName</td>
		<td><input type="text" name="deviceCategoryName" value="Corrosion"></td>
		
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
