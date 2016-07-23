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
    <title>User details</title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png" />
    <link rel="stylesheet" type="text/css" href="/extras/mymain.css">
    <c:set var="incomplete" value="${! currentUser.regComplete}"/>
    <c:set target="${currentUser}" property="regComplete" value="true"/>
</head>
<body>
<div class="bigform">
<h1>Your details</h1>
<c:if test="incomplete">
</c:if>

<form class="details-form" method="post" action="/main/updateUser" accept-charset="UTF-8">
<p class="login">${currentUser.username}</p>
<table width="100%">
<tr><td>Full name:</td>
<td width="80%"><input type="text" value="${currentUser.fullName}"/></td></tr>
<tr><td>Email:</td>
<td><input type="text" value="${currentUser.email}"/></td></tr>
</table>
<button class="smallbutton" type="submit">Update user data</button>
<a href="/main/serv" class="smallbutton">Go to main page...</a>
</form>
</div>
</body>
</html>
