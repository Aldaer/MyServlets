<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <fmt:setLocale value="${param['language']}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="title"/></title>
    <c:set var="root" value = "${pageContext.request.contextPath}" />
    <link href="${root}/images/clock_icon.png" rel="icon" type="image/png" />
</head>
<body>
<h1><fmt:message key="header"/></h1>

<p><fmt:message key="selectedlocale"/> = "${param['language']}"</p>

<p><fmt:message key="selecttz"/></p>
<form action="${root}/serv" method="get">
    <p>
        <select name="timezone" onchange="submit()">
            <c:forEach items="${supportedTZ}" var="tzentry">
                <option value="${tzentry.key}" ${lastTZ == tzentry.key ? 'selected' : ''} >${tzentry.value}</option>
            </c:forEach>
        </select>
        <input type="hidden" name="language" value="${param['language']}">
    <noscript>
        <input type="submit" value="Show time...">
    </noscript>
    </p>

</form>

<p><fmt:message key="beanreport"/><br>
    <fmt:message key="through"/> fmt:formatDate: <i><fmt:formatDate pattern = "HH:mm:ss dd-MM-yyyy (z)" value="${sessionScope.timer.date}" timeZone="${sessionScope.timer.tz}"/></i><br>
    <fmt:message key="through"/>  getDate(): <i>${sessionScope.timer.date}</i><br>
    <fmt:message key="through"/> toString(): <i>${sessionScope.timer}</i>
</p>
<a href="${root}/serv?action=restart"><fmt:message key="newsession"/></a>

</body>
</html>
