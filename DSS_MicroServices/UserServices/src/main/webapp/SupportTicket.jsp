<html>
<body>
	<h1>Support Ticket</h1>
	 <form action="upsertTicketObject" method="post" enctype="multipart/form-data">
	
	   <p>
		Select a file : <input type="file" name="file" size="45" />
	   </p>
	  
	  
	    <p>
		Enter  a sessionkey : <input type="sessionkey" name="sessionkey" size="45" value=""/>
	   </p>
	   
	    <p>
		Select a json : <textarea name="json" rows="10", cols="80">{ "title" : "test title", "severity" : "High", "module" : "test module", "description" : "test description" }</textarea>
	   </p>
	   
	   
	   
	   <p>
		Enter  a mode : <input type="mode" name="mode" value="add"/>
	   </p>
	  
 		
	   <input type="submit" value="Upload It" />
	</form>
 
</body>
</html>