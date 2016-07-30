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

<div class="bigpanel">
    <h1><fmt:message key="main.header"/></h1>

    <p><fmt:message key="main.currentlogin"/> <a href="/main/userdetails">${currentUser.fullName}
        [${currentUser.email}]</a><br>
        <fmt:message key="main.selectedlocale"/> = "${language}"
    </p>

    <p id="messagealert">
        <c:if test="${unreadPM > 0}">
            ###You have unread messages! [${unreadPM}]
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
        <fmt:message key="main.through"/> fmt:formatDate: <i><fmt:formatDate pattern="HH:mm:ss dd-MM-yyyy (z)"
                                                                             value="${timer.date}"
                                                                             timeZone="${timer.tz}"/></i><br>
        <fmt:message key="main.through"/> getDate(): <i>${timer.date}</i><br>
        <fmt:message key="main.through"/> toString(): <i>${timer}</i>
    </p>
    <button class="smallbutton" id="showmsg"><fmt:message key="main.mymessages"/></button>
    <a href="/main/logout" class="smallbutton amber"><fmt:message key="main.logout"/></a>
</div>

<div class="bigpanel slideout" id="msglist">
    <div style="width: 20%; float: left">
        buttons<br>
        more buttons
    </div>
    <div style="width: 80%; float:right">
        <div class="databox" id="msgbox">
            Loading messages...
        </div>
    </div>
</div>

<div class="messagebubble" id="bubbleprototype"></div>

<div class="bigpanel slideout" id="msgview">
    <h2>Message view</h2>
    <p id="msgheader"></p>
    <p id="msgtext" rows="6">TEXT</p>
    <textarea id="msgreply" rows="10">REPLY</textarea>
    <button class="smallbutton" id="reply">Reply</button>
    <button class="smallbutton ruby" id="delete">Delete</button>
    <button class="smallbutton amber" id="closeview">Close</button>
</div>

<script>
    var user = '${currentUser.username}';
</script>
<script src="/extras/mainpage.js"></script>
</body>
</html>
