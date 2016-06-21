<%--
  Created by IntelliJ IDEA.
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>

    <title>My first jsp page</title>
</head>
<body>
<h1>My first jsp page</h1>
To know time, select timezone:<br>
<form action="serv" method="get">
    <p>
        <select name="timezone">
            <option value="GMT" >Greenwich</option>
            <option value="Europe/Moscow" >Moscow</option>
            <option value="EST" >East Standard</option>
        </select>
        <input type="submit" value="Show time...">
    </p>

</form>

Meanwhile, this is what timer bean reports:<br>
${sessionScope.timer}
<br>
That's all for now!
</body>
</html>
