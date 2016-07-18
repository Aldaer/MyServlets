<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <fmt:setLocale value="${sessionScope.language}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="main.title"/></title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
</head>
<body>
<h1><fmt:message key="main.header"/></h1>


<p><fmt:message key="main.currentlogin"/> ${sessionScope.currentUser.username} [${sessionScope.currentUser.email}]<br>
    <fmt:message key="main.selectedlocale"/> = "${sessionScope.language}"</p>

<p><fmt:message key="main.selecttz"/></p>
<form action="/main/serv" method="get">
    <p>
        <select name="timezone" onchange="submit()">
            <c:forEach items="${supportedTZ}" var="tzentry">
                <option value="${tzentry.key}" ${lastTZ == tzentry.key ? 'selected' : ''} >${tzentry.value}</option>
            </c:forEach>
        </select>
    <noscript>
        <input type="submit" value="Show time...">
    </noscript>
    </p>

</form>

<p><fmt:message key="main.beanreport"/><br>
    <fmt:message key="main.through"/> fmt:formatDate: <i><fmt:formatDate pattern = "HH:mm:ss dd-MM-yyyy (z)" value="${sessionScope.timer.date}" timeZone="${sessionScope.timer.tz}"/></i><br>
    <fmt:message key="main.through"/>  getDate(): <i>${sessionScope.timer.date}</i><br>
    <fmt:message key="main.through"/> toString(): <i>${sessionScope.timer}</i>
</p>
<a href="/main/serv?action=logout"><fmt:message key="main.newsession"/></a>

</body>
</html>
