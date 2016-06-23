<%--
  Created by IntelliJ IDEA.
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>My first jsp page</title>
    <c:set var="root" value = "${pageContext.request.contextPath}" />
</head>
<body>
<h1>My first jsp page</h1>
<p><img src="${root}/images/clock_icon.png"></p>
Select language:<br>
<form action="${root}/serv" method="get">
    <input name="language" type="radio" value="en">English<br>
    <input name="language" type="radio" value="ru">Russian<br>
    <input type="submit" value="Show time...">
</form>
</body>
</html>
