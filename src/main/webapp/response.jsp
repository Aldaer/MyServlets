<%--
  Created by IntelliJ IDEA.
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>My first jsp response</title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
</head>
<body>
<h1>My first jsp page (response)</h1>
<fmt:setLocale value="${param['language']}" />
User-selected locale = "${param['language']}"

<p>To know time, select timezone:</p>
<form action="${pageContext.request.contextPath}/serv" method="get">
    <p>
            <select name="timezone">
                <c:forEach items="${supportedTZ}" var="tzentry">
                    <option value="${tzentry.key}" ${lastTZ == tzentry.key ? 'selected' : ''} >${tzentry.value}</option>
                </c:forEach>
            </select>
        <input type="hidden" name="language" value="${param['language']}">
        <input type="submit" value="Show time...">
    </p>

</form>

This is what timer bean reports:<br>
Through fmt:formatDate: <i><fmt:formatDate pattern = "HH:mm:ss dd-MM-yyyy (z)" value="${sessionScope.timer.date}" timeZone="${sessionScope.timer.tz}"/></i><br>
Through toString(): <i>${sessionScope.timer.date}</i>
<br>
That's all for now!
</body>
</html>
