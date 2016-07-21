<%-- Used only with Container-based authentication --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
  <head>
    <title>Login error</title>
  </head>
  <body>
    <c:set var="userFound" value="${Boolean.TRUE}" scope="session"/>
    <c:redirect url="/main/serv"/>
  </body>
</html>
