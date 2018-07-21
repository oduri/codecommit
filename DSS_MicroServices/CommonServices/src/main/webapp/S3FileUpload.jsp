<html>
<body>
<form  method="post" name="upload" enctype="multipart/form-data">
<style>
.loader {
  border: 16px solid #f3f3f3;
  border-radius: 50%;
  border-top: 16px solid blue;
  border-bottom: 16px solid blue;
  width: 60px;
  height: 60px;
  -webkit-animation: spin 2s linear infinite;
  animation: spin 2s linear infinite;
}

@-webkit-keyframes spin {
  0% { -webkit-transform: rotate(0deg); }
  100% { -webkit-transform: rotate(360deg); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>

<script>
function uploadFile(){
	
document.getElementById("spinnerloader").style="display:block";	
document.upload.action="uploadFile";
document.upload.submit();
}

</script>

	<h1>File Upload</h1>
	
	   <p>
		Select a file : <input type="file" name="file" size="45" />
	   </p>
 		
 		 <p>
		Enter a sessionkey : <input type="text" name="webServiceAuthenticationKey"  size="45" />
		
	   </p>
	   
	   <input type="button" value="Upload File" onclick="uploadFile()"/>
 
<div id="spinnerloader"  class="loader" style='display:none'></div>
</form>
</body>

</html>