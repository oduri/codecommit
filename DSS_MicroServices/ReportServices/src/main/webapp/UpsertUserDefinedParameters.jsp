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
 <form name="report" method="post" action="upsertUserConfigParameters">

 <table border='1' width="80">

	<tr>
		
		<td>Enter a session key</td>
		<td><input type="text"  name="sessionkey" value="5af5a657590801027e2a86fe"></td>
		
	</tr>
	<tr>
		
		<td>Enter a  deviceCategoryName</td>
		<td><input type="text" name="deviceCategoryName" value="Corrosion"></td>
		
	</tr>
		<tr>
		
		<td>Enter a  device json</td>
		<td><textarea name="devicejson" rows="10", cols="80">{\"deviceJsonDoc\":[{\"device_id\":\"970536179\",\"part_material_type\":\"Test_99\"}]}</textarea></td>
		
	</tr>
	
	<tr>
		
		<td>Enter a  channel json</td>
		<td><textarea name="channeljson" rows="10", cols="80">{"channelJsonDoc":[{"device_id":"970536179","channel_parameters.channel_1.part_material_velocity":12.0},{"device_id":"970536179","channel_parameters.channel_2.part_material_velocity":12.0}]}</textarea></td>
		
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
