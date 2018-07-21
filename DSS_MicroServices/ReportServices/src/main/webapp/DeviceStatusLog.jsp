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
 <form name="report" method="post" action="getDeviceStatusLog">

 <table border='1' width="80">

	<tr>
		
		<td>Enter a session key</td>
		<td><input type="text" name="sessionkey" value="5ad8ad0ef6e86a7d0a23814d"/></td>
		
	</tr>
	<tr>
		
		<td>Enter a   json</td>
		<td><textarea name="json" rows="10", cols="80">{}</textarea></td>
		
	</tr>
	
	<tr>
		
		<td>Enter a   advancedFilterJson</td>
		<td><textarea name="advancedFilterJson" rows="10", cols="80">{"filter_condition":[{"column":"plant_id","operator":"equals","value1":"Refinery"},{"column":"site_id","operator":"in","value1":["pinebend"]},{"column":"unit_id","operator":"in","value1":["Alky","Waste Water","Test Alky"]},{"column":"device_category","operator":"in","value1":["Corrosion"]},{"column":"active_status","operator":"equal","value1":"Yes","value2":""}]}</textarea></td>
		
	</tr>
	
	<tr>
		
		<td>Enter a  companyId</td>
		<td><input type="text" name="companyId" value="FHR"/></td>
		
	</tr>
	
	<tr>
		
		<td>Enter a  timezone</td>
		<td><input type="text" name="timezone" value="CDT"/></td>
		
	</tr>
	
	<tr>
		<td colspan="2"> <input type="submit" value="clickme" /></td>
	</tr>
	

</table>

 </form>

 </body>
</html>
