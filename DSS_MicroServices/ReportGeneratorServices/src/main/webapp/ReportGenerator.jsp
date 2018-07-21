<html>
<head>SES Implementation Modified</head>
<body>

<form action="generatePDF" method="post">
	 <table border='1' width="80">
		<tr>
		
			<td>Enter a session key</td>
			<td><input type="text" name="sessionkey" value="5aeb3466c9e77c027a31e05e"></td> <p>5af5a722590801027e2a8700</p>
		</tr>
		<tr>
		
			<td>Enter a filter Type</td>
			<td><input type="text" name="filterType" value="site"></td>
		</tr>
		<tr>
			<td>Enter a json</td>
			<td><textarea name="json" rows="10", cols="80">{ "company_id" : "FHR", "site_id":"LisleMolex","filename":"test","date":"07/07/2017","time":"07/17","timezone":"CDT"}</textarea></td>
		</tr>
		<tr>
			<td colspan="2"><input type="submit" name="Submit" value="Clickme"></td>
		</tr>
	  </table>
	  
	</form>
 
</body>
</html>