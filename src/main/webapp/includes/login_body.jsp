<%--
    Body for login and login-retry pages
--%>
<form name="login" method="post" action=
<c:choose>
    <c:when test="${applicationScope.authByContainer == true}">"j_security_check" </c:when>
    <c:otherwise>"/doLogin" </c:otherwise>
</c:choose>
accept-charset="UTF-8">
    <p align="center"><fmt:message key="login.username"/> <input type="text" name="j_username" required></p>
    <p align="center"><fmt:message key="login.password"/> <input type="password" name="j_password" required></p>
    <p align="center"><input type="submit" value="<fmt:message key="login.enter"/>" ></p>

    <input type="hidden" name="language" value="${param['language']}">
</form>
