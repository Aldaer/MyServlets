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
    <h1><fmt:message key="details.header"/></h1>
    <div style="width: 60%; float:left">
        <c:if test="${ownProfile and not currentUser.regComplete}"><p class="warning"><fmt:message
                key="details.unconfirmed"/></p></c:if>
        <form class="details-form" method="post" action="/main/updateUser" accept-charset="UTF-8">
            <p id="login">${displayedName}</p>
            <div>
                <label><fmt:message key="details.fullname"/></label>
                <input type="text" class="details" name="fullname" value="${displayedProfile.fullName}"/>
            </div>
            <div>
                <label><fmt:message key="details.email"/></label>
                <input type="text" class="details" name="email" value="${displayedProfile.email}"/>
            </div>
            <button class="smallbutton blue" id="createmsg" type="button"><fmt:message key="message.button.private"/></button>
            <button class="smallbutton details hidden" id="update" type="submit"><fmt:message
                    key="details.update"/></button>
            <button class="smallbutton friend hidden" id="addfriend" type="button"><fmt:message key="details.addfriend"/></button>
            <button class="smallbutton ruby hidden" id="remfriend" type="button"><fmt:message key="details.remfriend"/></button>
            <a href="/main/serv" class="smallbutton"><fmt:message key="details.gotomain"/></a>
        </form>
    </div>
    <div style="width: 40%; float:right">
        <p><fmt:message key="details.search"/></p>
        <input type="text" id="query" class="smallmargin"/><br>
        <button class="smallbutton" id="find"><fmt:message key="details.find.user"/></button>
        <button class="smallbutton friend" id="allfriends"><fmt:message key="details.show.friends"/></button>
    </div>
</div>

<div class="bigpanel slideout" id="fpanel">
    <div style="width: 20%; float: left">
        <p><fmt:message key="details.filter"/></p>
        <div class="colorbox"><input name="ufilter" type="radio" class="radio" onclick="displayFilteredUsers(0)"
                                     checked=""><fmt:message key="filter.all"/>
        </div>
        <div class="colorbox friend"><input name="ufilter" type="radio" class="radio friend"
                                            onclick="displayFilteredUsers(1)"/><fmt:message key="filter.friends"/>
        </div>
        <div class="colorbox nonfriend"><input name="ufilter" type="radio" class="radio"
                                               onclick="displayFilteredUsers(2)"/><fmt:message key="filter.non.friends"/>
        </div>
    </div>
    <div style="width: 80%; float:right">
        <p id="usersFoundHeader"></p>
        <div class="databox" id="userlistbox"><fmt:message key="loading.users"/></div>
    </div>
</div>

<div class="bigpanel slideout" id="newmsg">
    <h2><fmt:message key="message.button.private"/></h2>
    <p id="recipient"> </p>
    <textarea id="msgtext" rows="10"></textarea>
    <button class="smallbutton" id="send"><fmt:message key="message.button.send"/></button>
    <button class="smallbutton amber" id="cancel"><fmt:message key="message.button.cancel"/></button>
</div>

<div class="userbubble" id="bubbleprototype"></div>

<script>
    var currUser = "${currentUser.username}";
    var own = ${ownProfile};
    var exists = ${not empty displayedProfile};
    var displayedId = ${displayedProfile.id};

    var usersFoundMsg = '<fmt:message key="details.users.found"/> ';
</script>
<script src="/extras/details.js"></script>
</body>
</html>
