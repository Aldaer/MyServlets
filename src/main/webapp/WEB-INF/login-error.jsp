<%-- Used only with Container-based authentication --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
  <head>
    <title>Login error</title>
  </head>
  <body>
    <%-- cannot check whether username or password was wrong when using container-based authentication --%>
    <c:set var="userFound" value="${true}" scope="session"/>
    <c:redirect url="/main/serv"/>
  </body>
</html>
