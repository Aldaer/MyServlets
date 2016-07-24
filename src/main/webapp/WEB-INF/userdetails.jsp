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
    <title><fmt:message key="details.title"/></title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
    <link rel="stylesheet" type="text/css" href="/extras/green_main.css">
    <c:set var="incomplete" value="${not currentUser.regComplete}"/>
    <c:set target="${currentUser}" property="regComplete" value="true"/>
    <script src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js'></script>
</head>
<body>
<div class="bigform">
<h1><fmt:message key="details.header"/></h1>
<c:if test="${incomplete}"><p class="warning"><fmt:message key="details.unconfirmed"/></p></c:if>

<form class="details-form" method="post" action="/main/updateUser" accept-charset="UTF-8">
<p class="login">${currentUser.username}</p>
<table width="100%"><tr><td>
    <fmt:message key="details.fullname"/>:
</td>
<td width="80%">
    <input type="text" class="details" name="fullname" value="${currentUser.fullName}"/>
</td></tr><tr><td>
    <fmt:message key="details.email"/>:
</td><td>
    <input type="text" class="details" name="email" value="${currentUser.email}"/>
</td></tr></table>
<button class="smallbutton" type="submit"><fmt:message key="details.update"/></button>
<a href="/main/serv" class="smallbutton"><fmt:message key="details.gotomain"/></a>
</form>
</div>

<script src="/extras/details.js"></script>
</body>
</html>
