<html>
<body>
	<h1>Image Upload UserServices</h1>
 
	 <form action="testImageUploader" method="post" enctype="multipart/form-data">
	
	   <p>
		Select a file : <input type="file" name="file" size="45" />
	   </p>
	   
	   <p>
		Enter a path : <input type="path" name="path" size="45" value="/usr/local/images/plant_images/" />
	   </p>
	   
 		
	   <input type="submit" value="Upload It" />
	</form>
 
</body>
</html>