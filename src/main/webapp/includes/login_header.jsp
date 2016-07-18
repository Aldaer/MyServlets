<%--
    Header for login and login-retry pages
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=windows-1251"/>
    <fmt:setLocale value="${param['language']}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="login.title"/></title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
</head>
