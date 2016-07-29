<%--suppress JspAbsolutePathInspection --%>
<%--@elvariable id="currentUser" type="model.dao.User"--%>
<%--@elvariable id="language" type="java.lang.String"--%>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <fmt:setLocale value="${language}"/>
    <fmt:setBundle basename="jsp"/>
    <link href="/images/clock_icon.png" rel="icon" type="image/png"/>
    <link rel="stylesheet" type="text/css" href="/extras/green-details.css">
    <script src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js'></script>
    <c:set var="displayedName" value="${empty param.user? currentUser.username : param.user}"/>
    <c:set var="displayedProfile" value="${applicationScope['userDAO'].getUser(displayedName)}"/>
    <c:set var="ownProfile" value="${displayedName eq currentUser.username}"/>

    <title><fmt:message key="details.header"/></title>
</head>
<body>
<div class="bigpanel">
    <h1 ><fmt:message key="details.header"/></h1>
    <div style="width: 60%; float:left">
        <c:if test="${ownProfile and not currentUser.regComplete}"><p class="warning"><fmt:message
                key="details.unconfirmed"/></p></c:if>
        <form class="details-form" method="post" action="/main/updateUser" accept-charset="UTF-8">
            <p id="login">${displayedName}</p>
            <div class="block">
                <label><fmt:message key="details.fullname"/></label>
                <input type="text" class="details" name="fullname" value="${displayedProfile.fullName}"/>
            </div>
            <div class="block">
                <label><fmt:message key="details.email"/></label>
                <input type="text" class="details" name="email" value="${displayedProfile.email}"/>
            </div>
                <button class="smallbutton details" id="update" type="submit"><fmt:message key="details.update"/></button>
            <a href="/main/serv" class="smallbutton"><fmt:message key="details.gotomain"/></a>
        </form>
    </div>
    <div style="width: 40%; float:right">
        <p>Search</p>
        <input type="text" id="query"/><br>
        <button class="smallbutton" id="find">Find user...</button>
    </div>
</div>

<div class="bigpanel" id="slideout">
    <div style="width: 20%; float: left">
        buttons<br>
        more buttons
    </div>
    <div style="width: 80%; float:right">
        <div class="databox" id = "userfindbox">Loading users...</div>
    </div>
</div>

<div class="userbubble" id="bubbleprototype"></div>

<script>
    var currUser = "${currentUser.username}";
    var own = ${ownProfile};
    var exists = ${not empty displayedProfile};
</script>
<script src="/extras/details.js"></script>
</body>
</html>
