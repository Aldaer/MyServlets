<%--suppress JspAbsolutePathInspection --%>
<%--@elvariable id="timer" type="model.MyTimer"--%>
<%--@elvariable id="language" type="java.lang.String"--%>
<%--@elvariable id="currentUser" type="model.dao.User"--%>
<%--@elvariable id="lastTZ" type="java.lang.String"--%>
<%--@elvariable id="supportedTZ" type="java.util.Properties"--%>
<%--@elvariable id="unreadPM" type="java.lang.Integer"--%>
<%--@elvariable id="friendString" type="java.util.String"--%>
<%--@elvariable id="showLegacy" type="java.lang.Boolean"--%>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <fmt:setLocale value="${language}"/>
    <fmt:setBundle basename="jsp"/>
    <title><fmt:message key="main.title"/></title>
    <link href="/images/clock_icon.png" rel="icon" type="image/png"/>
    <link rel="stylesheet" type="text/css" href="/extras/green-main.css">
    <script src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js'></script>
</head>
<body>

<div class="bigpanel">
    <h1><fmt:message key="main.header"/></h1>

    <p><fmt:message key="main.currentlogin"/> <a href="/main/userdetails">${currentUser.fullName}
        [${currentUser.email}]</a><br>
        <fmt:message key="main.selectedlocale"/> = "${language}"
    </p>

    <p id="messagealert" class="warning">
        <c:if test="${unreadPM > 0}">
            <fmt:message key="main.warn.unread"/> [${unreadPM}]
        </c:if>
    </p>

    <c:if test="${showLegacy}">
    <div class="bigpanel time hidden">
        <p><fmt:message key="main.selecttz"/></p>
        <form action="/main/serv" method="get">
            <p>
                <select name="timezone" onchange="submit()">
                    <c:forEach items="${supportedTZ}" var="tzentry">
                        <option value="${tzentry.key}" ${lastTZ == tzentry.key ? 'selected' : ''} >${tzentry.value}</option>
                    </c:forEach>
                </select>
            </p>
        </form>

        <p>
            <fmt:message key="main.beanreport"/><br>
            <fmt:message key="main.through"/> fmt:formatDate: <i><fmt:formatDate pattern="HH:mm:ss dd-MM-yyyy (z)"
                                                                                 value="${timer.date}"
                                                                                 timeZone="${timer.tz}"/></i><br>
            <fmt:message key="main.through"/> getDate(): <i>${timer.date}</i><br>
            <fmt:message key="main.through"/> toString(): <i>${timer}</i>
        </p>
    </div>
    </c:if>
    <div class="bigpanel hidden" id="convdiv">
        <div style="width: 20%; float: left">
            <h3><fmt:message key="main.conv.filter"/></h3>
            <label>
                <input name="cfilter" type="radio" class="radio" id="ownconv" onclick="loadConversations(0)" checked>
                <fmt:message key="main.conv.my"/>
            </label><br>
            <label>
                <input name="cfilter" type="radio" class="radio" onclick="loadConversations(1)">
                <fmt:message key="main.conv.all"/>
            </label><br>
            <label>
                <input name="cfilter" type="radio" class="radio" id="invconv" onclick="loadConversations(2)">
                <fmt:message key="main.conv.invites"/>
            </label><br>
            <button class="smallbutton" onclick="showNewConv(true)"><fmt:message key="conv.new.show"/></button>
        </div>
        <div style="width: 80%; float:right">
            <div class="databox">
                <table id="convtable">
                    <tr id="convheader">
                        <th></th>
                        <th><fmt:message key="conv.table.name"/></th>
                        <th><fmt:message key="conv.table.created.by"/></th>
                        <th><fmt:message key="conv.table.created.at"/></th>
                        <th><fmt:message key="conv.table.description"/></th>
                    </tr>
                    <tr>
                        <td><fmt:message key="loading.conversations"/></td>
                    </tr>
                </table>
                <div id="newconv" class="hidden">
                    <input id="convname" type="text" placeholder="<fmt:message key='conv.new.name'/>"/><br>
                    <input id="convdesc" type="text" placeholder="<fmt:message key='conv.new.desc'/>"/><br>
                    <button class="smallbutton" onclick="createNewConv()"><fmt:message key="conv.button.create"/></button>
                    <button class="smallbutton amber" onclick="showNewConv(false)"><fmt:message key="message.button.close"/></button>
                </div>
                <div class="invit hidden">
                    <button class="smallbutton" onclick="acceptInvitation(true)"><fmt:message key="conv.button.accept"/></button>
                    <button class="smallbutton amber" onclick="acceptInvitation(false)"><fmt:message key="conv.button.decline"/></button>
                </div>
            </div>
        </div>
    </div>
    <button class="smallbutton" onclick="showPrivMessages()"><fmt:message key="main.mymessages"/></button>
    <button class="smallbutton" onclick="toggleConversations()"><fmt:message key="main.conversations"/></button>
    <c:if test="${showLegacy}"><button class="smallbutton" onclick="toggleTime()"><fmt:message key="main.time"/></button></c:if>
    <a href="/main/userdetails" class="smallbutton"><fmt:message key="main.users"/></a>
    <a href="/main/logout" class="smallbutton amber"><fmt:message key="main.logout"/></a>
</div>

<div class="bigpanel slideout" id="msglist">
    <h3 id="privateHdr" class="hidden"><fmt:message key="main.private.header"/></h3>
    <h3 id="convBoxHdr" class="hidden"><fmt:message key="main.conversation.header"/></h3>
    <p id="participants"><fmt:message key="main.conversation.participants"/></p>
    <div style="width: 20%; float: left">
        <h3><fmt:message key="main.message.sorting"/></h3>
        <label>
            <input name="msort" type="radio" class="radio" onclick="setSortMode(0)">
            <fmt:message key="main.sorting.time"/>
        </label><br>
        <label>
            <input name="msort" type="radio" class="radio" onclick="setSortMode(1)" checked>
            <fmt:message key="main.sorting.chain"/>
        </label><br>
        <button class="smallbutton" style="width:80%" onclick="newMessage()"><fmt:message key="main.new.message"/></button>
    </div>
    <div style="width:80%;float:right">
        <div class="databox" id="msgbox">
            <fmt:message key="loading.messages"/>
        </div>
    </div>
</div>

<div class="messagebubble" id="bubbleprototype"></div>

<div class="bigpanel slideout" id="msgview">
    <h2><fmt:message key="main.message.view"/></h2>
    <p id="msgheader"></p>
    <p id="msgtext"></p>
    <div><fmt:message key="message.recip"/> <input type="text" id="recipient" list="friends" disabled/></div>
    <datalist id="friends">
        <c:forTokens items="${friendString}" delims="," var="fname">
            <option value="${fname}">${fname}</option>
        </c:forTokens>
    </datalist>
    <textarea id="newmsgtext" rows="10"></textarea>
    <button class="smallbutton" id="send" onclick="sendMessage()">S/R</button>
    <button class="smallbutton ruby" id="delete" onclick="deleteMessage()"><fmt:message key="message.button.delete"/></button>
    <button class="smallbutton amber" onclick="closeReply()"><fmt:message key="message.button.close"/></button>
</div>

<script>
    const user = '${currentUser.username}';
    const jsLocale = '${language}';

    const sendText = "<fmt:message key="message.button.send"/>";
    const replyText = "<fmt:message key="message.button.reply"/>";
</script>
<script src="/extras/mainpage.js"></script>
</body>
</html>
