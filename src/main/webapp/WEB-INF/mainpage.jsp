<%--suppress JspAbsolutePathInspection --%>
<%--@elvariable id="timer" type="model.MyTimer"--%>
<%--@elvariable id="language" type="java.lang.String"--%>
<%--@elvariable id="currentUser" type="model.dao.User"--%>
<%--@elvariable id="lastTZ" type="java.lang.String"--%>
<%--@elvariable id="supportedTZ" type="java.util.Properties"--%>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <fmt:setLocale value="${language}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="main.title"/></title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
    <link rel="stylesheet" type="text/css" href="/extras/mymain.css">
</head>
<body>
<div class="bigform">
<h1><fmt:message key="main.header"/></h1>


<p><fmt:message key="main.currentlogin"/> <a href="/main/userdetails">${currentUser.fullName} [${currentUser.email}]</a><br>
    <fmt:message key="main.selectedlocale"/> = "${language}"</p>

<p><fmt:message key="main.selecttz"/></p>
<form action="/main/serv" method="get">
    <p>
        <select name="timezone" onchange="submit()">
        <c:forEach items="${supportedTZ}" var="tzentry">
            <option value="${tzentry.key}" ${lastTZ == tzentry.key ? 'selected' : ''} >${tzentry.value}</option>
        </c:forEach>
    </select>
    </p>
    <noscript>
        <input type="submit" value="Show time...">
    </noscript>
</form>

<p>
    <fmt:message key="main.beanreport"/><br>
    <fmt:message key="main.through"/> fmt:formatDate: <i><fmt:formatDate pattern = "HH:mm:ss dd-MM-yyyy (z)" value="${timer.date}" timeZone="${timer.tz}"/></i><br>
    <fmt:message key="main.through"/>  getDate(): <i>${timer.date}</i><br>
    <fmt:message key="main.through"/> toString(): <i>${timer}</i>
</p>
<button class="smallbutton" onclick="location.href='/main/serv?action=logout'"><fmt:message key="main.logout"/></button>

</div>
</body>
</html>