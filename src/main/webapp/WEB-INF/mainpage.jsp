<%--suppress JspAbsolutePathInspection --%>
<%--@elvariable id="timer" type="model.MyTimer"--%>
<%--@elvariable id="language" type="java.lang.String"--%>
<%--@elvariable id="currentUser" type="model.dao.User"--%>
<%--@elvariable id="lastTZ" type="java.lang.String"--%>
<%--@elvariable id="supportedTZ" type="java.util.Properties"--%>
<%--@elvariable id="unreadPM" type="java.lang.Integer"--%>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <fmt:setLocale value="${language}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="main.title"/></title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png"/>
    <link rel="stylesheet" type="text/css" href="/extras/green-main.css">
    <script src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js'></script>
</head>
<body>
<div class="bigform">
    <h1><fmt:message key="main.header"/></h1>

    <p><fmt:message key="main.currentlogin"/> <a href="/main/userdetails">${currentUser.fullName}
        [${currentUser.email}]</a><br>
        <fmt:message key="main.selectedlocale"/> = "${language}"
    </p>

    <p id="messagealert">
        <c:if test="${unreadPM > 0}">
            You have unread messages! [${unreadPM}]
            <button class="smallbutton" id="showmsg">Read now...</button>
        </c:if>
    </p>

    <p><fmt:message key="main.selecttz"/></p>
    <form action="/main/serv" method="get">
        <p>
            <select name="timezone" onchange="submit()">
                <c:forEach items="${supportedTZ}" var="tzentry">
                    <option value="${tzentry.key}" ${lastTZ == tzentry.key ? 'selected' : ''} >${tzentry.value}</option>
                </c:forEach>
            </select>
        </p>
    </form>

    <p>
        <fmt:message key="main.beanreport"/><br>
        <fmt:message key="main.through"/> fmt:formatDate: <i><fmt:formatDate pattern="HH:mm:ss dd-MM-yyyy (z)" value="${timer.date}" timeZone="${timer.tz}"/></i><br>
        <fmt:message key="main.through"/> getDate(): <i>${timer.date}</i><br>
        <fmt:message key="main.through"/> toString(): <i>${timer}</i>
    </p>
    <a href="/main/logout" class="smallbutton"><fmt:message key="main.logout"/></a>
</div>


<div class="bigform" id="slideout">
    <div style="width: 20%; float: left">
        buttons<br>
        more buttons
    </div>
    <div style="width: 80%; float:right">
        <div id = "messagebox"></div>
    </div>
</div>

<div class="messagebubble" id="bubbleprototype"></div>

<script>
    var user = '${currentUser.username}';
</script>
<script src="/extras/mainpage.js"></script>
</body>
</html>
