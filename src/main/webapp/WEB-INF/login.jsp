<%--suppress JspAbsolutePathInspection --%>
<%--suppress CheckTagEmptyBody --%>
<%--suppress JspAbsolutePathInspection --%>
<%--@elvariable id="regAttempt" type="java.lang.String"--%>
<%--@elvariable id="userFound" type="java.lang.Boolean"--%>
<%--@elvariable id="authByContainer" type="java.lang.Boolean"--%>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <c:set var="locale" value="${param['language']}"/>
    <c:if test="${empty locale}"><c:set var="locale" value="en"/></c:if>
    <fmt:setLocale value="${locale}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="login.title"/></title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
    <link rel="stylesheet" type="text/css" href="/extras/green-login.css">
    <script src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js'></script>
</head>
<body>
<div class="login-page">
    <div class="wform">
        <form class="register-form" name="register" method="post" action="/doRegister" accept-charset="UTF-8">
            <input type="text" placeholder="<fmt:message key="register.username"/>" name="j_username" id="r_name"/>
            <input type="password" placeholder="<fmt:message key="login.password"/>" name="j_password"/>
            <input type="password" placeholder="<fmt:message key="register.repeatPassword"/>" name="j_password2"/>
            <button type="submit"><fmt:message key="register.do"/></button>
            <p class="failcause"></p>
            <p class="message"><fmt:message key="login.registered"/> <a href="#"><fmt:message key="login.signIn"/></a></p>
            <input type="hidden" name="language" value="${locale}">
        </form>
        <form class="login-form" name="login" method="post" action="${authByContainer? "j_security_check" : "/doLogin"}" accept-charset="UTF-8">
            <input type="text" placeholder="<fmt:message key="login.username"/>" name="j_username" />
            <input type="password" placeholder="<fmt:message key="login.password"/>" name="j_password" />
            <button type="submit"><fmt:message key="login.enter"/></button>

            <p class="failcause"><c:choose>
            <c:when test="${empty userFound}"></c:when>
            <c:when test="${! userFound}"><fmt:message key="login.badLogin"/></c:when>
            <c:otherwise><fmt:message key="login.badPassword"/></c:otherwise>
            </c:choose></p>

            <p class="message"><fmt:message key="login.notRegistered"/> <a href="#"><fmt:message key="register.begin"/></a></p>
            <input type="hidden" name="language" value="${locale}">
        </form>
    <table width="100%"><tr>
      <td><button class="smallbutton" onclick="location.href='/main/serv?language=en'">English</button></td>
      <td><button class="smallbutton" onclick="location.href='/main/serv?language=ru'">Русский</button></td>
    </tr></table>
    </div>
</div>

<script>
   var noUser = '<fmt:message key="alert.noUsername"/>';
   var pwdMismatch = '<fmt:message key="alert.pwdMismatch"/>';
   var pwdTooShort = '<fmt:message key="alert.pwdTooShort"/>';
   var unTooLong = '<fmt:message key="alert.unTooLong"/>';

   var regAttempt = '${regAttempt}';
   var userExists = '<fmt:message key="register.userExists"/>';
</script>

<script src="/extras/login.js"></script>
</body>
</html>