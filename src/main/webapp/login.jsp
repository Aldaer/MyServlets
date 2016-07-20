<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <meta charset="utf-8"/>
    <c:set var="locale" value="${param['language']}"/>
    <c:if test="${empty locale}"><c:set var="locale" value="en"/></c:if>
    <fmt:setLocale value="${locale}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="login.title"/></title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
    <link rel="stylesheet" type="text/css" href="/extras/mygreen.css">
    <script src='http://cdnjs.cloudflare.com/ajax/libs/jquery/2.1.3/jquery.min.js'></script>
</head>
<body>
<div class="login-page">
    <div class="form">
        <form class="register-form" name="register" method="post" action="/registerUser" accept-charset="UTF-8">
            <input type="text" placeholder="<fmt:message key="register.username"/>" name="username"/>
            <input type="password" placeholder="<fmt:message key="login.password"/>" name="password"/>
            <input type="password" placeholder="<fmt:message key="register.repeatPassword"/>" name="password2"/>
            <button action="submit"><fmt:message key="register.do"/></button>
            <p class="message"><fmt:message key="login.registered"/> <a href="#"><fmt:message key="login.signIn"/></a></p>
        </form>
        <form class="login-form" name="login" method="post" action=<c:choose>
            <c:when test="${authByContainer == true}">"j_security_check"</c:when>
            <c:otherwise>"/doLogin"</c:otherwise>
            </c:choose> accept-charset="UTF-8">

            <input type="text" placeholder="<fmt:message key="login.username"/>" name="j_username" />
            <input type="password" placeholder="<fmt:message key="login.password"/>" name="j_password" />
            <button action="submit"><fmt:message key="login.enter"/></button>

            <p class="message"><fmt:message key="login.notRegistered"/> <a href="#"><fmt:message key="register.begin"/></a></p>
            <input type="hidden" name="language" value="${locale}">
        </form>
    <table width="100%"><tr>
      <td><button class="smallbutton" onclick="location.href='/main/serv?language=en'">English</a></button></td>
      <td><button class="smallbutton" onclick="location.href='/main/serv?language=ru'">Русский</a></button></td>
    </tr></table>
    </div>
</div>

<script>
   var noUser = '<fmt:message key="alert.noUsername"/>';
   var pwdMismatch = '<fmt:message key="alert.pwdMismatch"/>';
   var pwdTooShort = '<fmt:message key="alert.pwdTooShort"/>';
</script>

<script src="/extras/login.js"></script>
</body>
</html>