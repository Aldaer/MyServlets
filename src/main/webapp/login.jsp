<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=windows-1251"/>
    <fmt:setLocale value="${param['language']}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="login.title"/></title>
    <c:set var="root" value = "${pageContext.request.contextPath}" />
    <link href="${root}/images/clock_icon.png" rel="icon" type="image/png" />
</head>
<body>
<h1 align="center"><fmt:message key="login.caption"/></h1>
<form name="login" method="post" action="${root}/doLogin" accept-charset="UTF-8">
    <p align="center"><fmt:message key="login.username"/> <input type="text" name="username" required></p>
    <p align="center"><fmt:message key="login.password"/> <input type="password" name="password" required></p>
    <p align="center"><input type="submit" value="<fmt:message key="login.enter"/>" ></p>

    <input type="hidden" name="language" value="${param['language']}">
</form>

</body>
</html>