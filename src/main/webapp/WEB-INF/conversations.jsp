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
    <link rel="stylesheet" type="text/css" href="/extras/green-common.css">
    <script src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js'></script>

    <title>Conversations</title>
</head>
<body>
<div class="bigpanel">
    <h1><fmt:message key="conversations.header"/></h1>
        <button class="smallbutton" id="gotomain">Main page</button>
</div>

<script src="/extras/conversations.js"></script>
</body>
</html>
