<!doctype html>
<html lang="en">
 <head>
  <meta charset="UTF-8">
  <meta name="Generator" content="EditPlus®">
  <meta name="Author" content="">
  <meta name="Keywords" content="">
  <meta name="Description" content="">
  <title>LoginServices</title>
 </head>
 <body>
 <form name="login" method="post">

 <table border='1'>

	<tr>
		
		<td>Enter a User Id</td>
		<td><input type="text" name="userId" value="Sam"></td>
		
	</tr>
		<tr>
		
		<td>Enter a Password</td>
		<td><input type="text" name="userPassword" value="12345"></td>
		
	</tr>
	
	<tr>
		<td colspan="2"><input type="submit" name="ClickMe" value="ClickMe" onclick="validateLogin()"></td>
	</tr>
	

</table>

 </form>

<script>


function validateLogin(){
	
	//document.login.action="http://52.1.245.10:8080/MolexServices/rest/authenticate/login";
	document.login.action="https://dev.msyte.io/LoginServices/login";
	document.login.submit();
}


</script>

 </body>
</html>
