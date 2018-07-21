<html>
<body>
	<h1>File Download</h1>
 
	 <form action="downloadFiles" method="Get" enctype="">
		
	    <p>
		Enter a web service authentication key : <input type="text" name="webServiceAuthenticationKey" value="" size="45" />
	   </p>
	   	
	   <p>
		Enter a from Date(MM/dd/yyyy hh:mm) : <input type="text" name="fromDate" value="07/01/2016 00:00" size="45" />
		 
	   </p>
	   
	   <p>
		Enter a to Date(MM/dd/yyyy hh:mm) : <input type="text" name="toDate" value="07/17/2017 23:59" size="45" />
	   </p>
	   
	   <p>
		Enter a deviceId <input type="text" name="deviceId" value="99745874" size="45" />
	   </p>
	   
	   <p>
		Select downloadType 
		<select name="downloadType">
  			<option value="file">file</option>
  			<option value="data">data</option>
		</select>
	   </p>
	   
	   
	   <input type="submit" value="download" />
	</form>
 
</body>
</html>