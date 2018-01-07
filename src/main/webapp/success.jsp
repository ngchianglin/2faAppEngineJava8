<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="sg.nighthour.app.UserDAO" %>
<%@page import="sg.nighthour.app.AuthSession" %>
  
 
<%

AuthSession.validate(request, response);
response.setHeader("Cache-Control", "no-store");

String userid = (String)session.getAttribute("userid");
if(userid == null)
{
    response.sendRedirect("/error.html");
}
String username = UserDAO.getUserName(userid, request.getRemoteAddr());

%> 
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="styles/main.css">
<title>Success Page</title>
</head>
<body>

<div class="mainbody">

<p>
Welcome 
<% 
if(username != null)
{ 
    out.print(username); 
}
else
{
    out.print("Unknown"); 
}

%> 
<br>
</p>

<p>
<a href="/logout.jsp">Logout</a>
</p>


<%@include file="templates/footer.html" %>


</div>


</body>
</html>