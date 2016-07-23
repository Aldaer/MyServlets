<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <fmt:setLocale value="${language}"/>
    <fmt:setBundle basename="jsp"/>
    <title>User details</title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
    <link rel="stylesheet" type="text/css" href="/extras/mymain.css">
</head>
<body>
<div class="bigform">
<h1>Your details</h1>

<form class="details-form" method="post" action="/updateUser" accept-charset="UTF-8">
<p class="login">${currentUser.username}</p>
<input type="text" value="${currentUser.fullName}"/>
<input type="text" value="${currentUser.email}"/>
<td><button class="smallbutton" action="submit">Update user data</button></td>
</form>
<td><button class="smallbutton" onclick="location.href='/main/serv'">Go to main page...</button></td>
</div>
</body>
</html>
