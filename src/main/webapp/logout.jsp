<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%

   response.setHeader("Cache-Control", "no-store");

   if(session.getAttribute("userid") != null)
   {
        session.invalidate();
        String custsession = "JSESSIONID=" + " " +";Path=/;Secure;HttpOnly;SameSite=Strict";
        response.setHeader("Set-Cookie", custsession);
   }
   else
   {
       session.invalidate(); 
       response.sendRedirect("/error.html");
   }

%>    
    
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="styles/main.css">
<title>Logout Page</title>
</head>
<body>

<div class="mainbody">

<p>
You have been logged out !
</p>

<p>
<a href="/index.jsp">Login Again</a>
</p>

<%@include file="templates/footer.html" %>


</div>



</body>
</html>