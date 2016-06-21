<%--
  Created by IntelliJ IDEA.
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>

    <title>My first jsp response</title>
</head>
<body>
<h1>My first jsp page (response)</h1>
To know time, select timezone:<br>
<form action="${pageContext.request.contextPath}/serv" method="get">
    <p>
        <select name="timezone">
            <option value="GMT" ${lastTZ == "GMT" ? 'selected' : ''}>Greenwich</option>
            <option value="Europe/Moscow" ${lastTZ == "Europe/Moscow" ? 'selected' : ''}>Moscow</option>
            <option value="EST" ${lastTZ == "EST" ? 'selected' : ''}>East Standard</option>
        </select>
        <input type="submit" value="Show time...">
    </p>

</form>

This is what timer bean reports:<br>
${sessionScope.timer}<br>
<fmt:formatDate patttern = "HH:mm:ss Z dd-MM-yyyy" value="${sessionScope.timer.date}" /><br>
<br>
That's all for now!
</body>
</html>
