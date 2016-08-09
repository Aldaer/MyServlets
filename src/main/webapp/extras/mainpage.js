const BODY = $('BODY');

const MSG_BOX = $('#msgbox');
const MSG_LIST = $('#msglist');
const CONV_TABLE = $('#convtable');
const CONV_HEADER = $('#convheader').clone();
const CONV_BOX_HEADER = $('#convBoxHdr');
const CONV_BOX_HEADER_HTML = CONV_BOX_HEADER.html();
const PARTCS = $('#participants');
const PARTCS_HTML = PARTCS.html();
const RECIPIENT = $('#recipient');
const SEND_BUTTON = $('#send');
const INVIT_BUTTON = $('#invconv');
const PRIVATE_MSG = -1;

var dispDivs;
var chainSort = true;
var messageCache;
var convCache;
// 0 = list owned, 1 = list watched, 2 = list invites, 3 = accept/decline invites, 10 = create new
var convListMode = 0;
var currentConvIndex = PRIVATE_MSG;
var convOwner = false;
var replyingTo; // null => new message

function showPrivMessages() {
    MSG_LIST.addClass('on');
    $('#privateHdr').removeClass("hidden");
    $('#convBoxHdr').addClass("hidden");
    $('#participants').addClass("hidden");
    RECIPIENT.parent().removeClass("hidden");
    currentConvIndex = PRIVATE_MSG;
    loadAllMessages();
}

function toggleConversations() {
    var convDiv;
    (convDiv = $('#convdiv')).toggleClass("hidden");
    if (!convDiv.hasClass("hidden")) {
        loadConversations(convListMode);
    }
}

function newMessage() {
    replyingTo = null;
    RECIPIENT.prop("disabled", false);
    RECIPIENT.val("");
    RECIPIENT.focus();

    $('#msgview').addClass("centered");
    $('#newmsgtext').val("");
    $('#msgtext').empty();
    SEND_BUTTON.html(sendText);
    document.getElementById("delete").disabled = true;
}

function toggleTime() {
    $('.time').toggleClass("hidden");
}

function loadAllMessages() {
    $.getJSON("/main/messages?offset=0&limit=20&convId="
        + (currentConvIndex == PRIVATE_MSG ? "0,-1&type=from,to" : convCache[currentConvIndex].id),
        onLoadMessages); // TODO: implement limits
}

function sendMessage() {
    const MSG_TEXT = $('#newmsgtext');
    if (MSG_TEXT.val() == "") {
        MSG_TEXT.focus();
        return;
    }
    var msgData = {
        action: "send",
        convId: currentConvIndex == PRIVATE_MSG ? 0 : convCache[currentConvIndex].id,
        text: MSG_TEXT.val()
    };

    if (replyingTo != null) {
        msgData.to = replyingTo.data("msgFrom");
        msgData.refId = replyingTo.data("msgId");
    } else {
        msgData.to = RECIPIENT.val();
    }
    $.post("/main/messageAction", msgData, loadAllMessages);
    closeReply();
}

function deleteMessage() {
    var msgData = {
        action: "delete",
        msgId: replyingTo.data("msgId")
    };
    $.post("/main/messageAction", msgData, loadAllMessages);
    closeReply();
}

function closeReply() {
    $('#msgview').removeClass("centered");
}

function onLoadMessages(data) {
    messageCache = data;

    MSG_BOX.empty();
    dispDivs = [];
    $.each(data.messages, displayMessage);
}

function userlink(uname) {
    return $("<a />", {
        href: "/main/userdetails?user=" + uname,
        text: uname
    });
}

function displayMessage(i, msg) {
    var mdiv = $('#bubbleprototype').clone();
    mdiv.removeAttr("id");
    var ulink;
    if (msg.from == user) {
        mdiv.append("--> ");
        ulink = userlink(msg.to);
        mdiv.addClass("messageout");
        if (!chainSort) mdiv.addClass("leftshift");
    } else {
        mdiv.append("<-- ");
        ulink = userlink(msg.from);
        mdiv.addClass("messagein");
        if (!chainSort) mdiv.addClass("rightshift");
    }
    if (msg.conversationId == 0) {
        mdiv.addClass("unread");
        if (msg.to == user) {
            mdiv.on("mouseenter", startHoverTimer);
            mdiv.on("mouseleave", stopHoverTimer);
        }
    }

    mdiv.append(ulink, ' [', localTime(msg.utcTimestamp), ']:<br>');
    mdiv.append(msg.text);

    var msgid = msg.id;
    mdiv.data("msgId", msgid);
    mdiv.data("msgFrom", msg.from);
    mdiv.data("msgTo", msg.to);
    mdiv.css("display", "block");
    mdiv.click(msgid, messageClicked);

    var attachPoint = MSG_BOX;
    var append = true;
    if (chainSort && msg.refId > 0)
        for (var j = 0; j < dispDivs.length; j++)
            if (dispDivs[j].data("msgId") == msg.refId) {
                attachPoint = dispDivs[j];
                mdiv.css("margin-left", offsetByPx(attachPoint.css("margin-left"), 20, 400));
                append = false;
                break;
            }

    if (append) attachPoint.append(mdiv);
    else attachPoint.after(mdiv);
    dispDivs.push(mdiv);
}

function localTime(timestamp) {
    return new Date(timestamp).toLocaleString(jsLocale);
}

var hoverTimer;
var nowReading;

function startHoverTimer(event) {
    hoverTimer = setTimeout(markAsRead, 2000);
    nowReading = $(event.currentTarget);
    BODY.addClass("waiting");
}

function stopHoverTimer() {
    clearTimeout(hoverTimer);
    BODY.removeClass("waiting");
}

function markAsRead() {
    BODY.removeClass("waiting");
    if (nowReading.hasClass("unread")) {
        nowReading.removeClass("unread");
        nowReading.off("mouseenter");
        nowReading.off("mouseleave");
        var msgData = {
            action: "update",
            id: nowReading.data("msgId"),
            unread: false
        };
        $.post("/main/messageAction", msgData);
        $('#messagealert').addClass("hidden");
    }
}

function messageClicked(event) {
    if (event.target.nodeName.toLowerCase() == "a")
        return;

    replyingTo = $(event.currentTarget);
    var to = replyingTo.data("msgTo");
    if (to == user && replyingTo.hasClass("unread")) {
        nowReading = replyingTo;
        markAsRead();
    }
    RECIPIENT.val(replyingTo.data("msgFrom"));
    RECIPIENT.prop("disabled", true);
    $('#msgview').addClass("centered");
    SEND_BUTTON.html(replyText);
    $('#msgreply').val("");
    var msgPlace = $('#msgtext');
    msgPlace.empty();
    var mClone = replyingTo.clone();
    mClone.css("margin", "3px");
    msgPlace.append(mClone);
    var canDelete = currentConvIndex == PRIVATE_MSG || convOwner;
    document.getElementById("delete").disabled = !canDelete;

}

function offsetByPx(pxValue, offs, maxoffs) {
    var v = Number(pxValue.substring(0, pxValue.indexOf("px"))) + offs;
    if (v > maxoffs) v = maxoffs;
    return v + "px";
}

function setSortMode(mode) {
    chainSort = (mode == 1);
    if (messageCache == null) loadAllMessages();
    else onLoadMessages(messageCache);
}

function loadConversations(mode) {
    convListMode = mode;
    $('.invit').addClass("hidden");
    $.getJSON("/main/conversations?mode=" + convListMode, onLoadConversations);
}

function onLoadConversations(data) {
    convCache = data;
    CONV_TABLE.html(CONV_HEADER.clone());
    $.each(data, displayConversation);
}

function loadConvParticipants(i) {
    const convid = convCache[i].id;
    $.getJSON("/main/userSearch?convId=" + convid, fillConvParticipants);
}

function fillConvParticipants(data) {
    if (data.users.length < 1) return;
    PARTCS.append(userlink(data.users[0].username));
    for (i = 1; i < data.users.length; i++)
        PARTCS.append(", ").append(userlink(data.users[i].username));
}

function displayConversation(i, conv) {
    var newrow = $("<tr></tr>");
    var convcheck = $("<td></td>");
    var convlink;
    if (INVIT_BUTTON.is(':checked')) {
        convcheck.addClass("conv_invite");
        convcheck.html("<input type='checkbox' onclick='convChecked(event)'>");
        convcheck.data("convId", conv.id);
        convlink = conv.name;
    } else
        convlink = $("<a href='#' class='convlink' onclick='return convClicked(event," + i + ")'></a>").append(conv.name);
    newrow.append(convcheck);
    newrow.append($("<td></td>").append(convlink));
    newrow.append($("<td align='center'></td>").append(userlink(conv.starter)));
    newrow.append("<td>" + localTime(conv.started) + "</td>");
    newrow.append("<td width='99%'>" + conv.desc + "</td>");
    CONV_TABLE.append(newrow);
}

function convChecked(event) {
    if (event.target.checked)
        $('.invit').removeClass("hidden");
}

function acceptInvitation(accept) {
    var convList = "";
    $('.conv_invite').each(function () {
        convList += $(this).data("convId") + ",";
    });
//    alert(convList.slice(0, -1));
    var convData = {
        mode: 3,
        accept: accept ? "yes" : "no",
        ids: convList.slice(0, -1)
    };
    $.post("/main/conversations", convData, onLoadConversations, "json");
}

function convClicked(event, i) {
    event.preventDefault();
    MSG_LIST.addClass('on');
    $('#privateHdr').addClass("hidden");
    RECIPIENT.parent().addClass("hidden");
    CONV_BOX_HEADER.html(CONV_BOX_HEADER_HTML);
    CONV_BOX_HEADER.removeClass("hidden");
    CONV_BOX_HEADER.append(" ").append(convCache[i].name);
    PARTCS.html(PARTCS_HTML);
    PARTCS.removeClass("hidden");
    PARTCS.append(" ");
    convOwner = (convCache[i].starter == user);
    currentConvIndex = i;
    loadAllMessages();
    loadConvParticipants(i);
    return false;
}

function showNewConv(show) {
    if (show)
        $('#newconv').removeClass("hidden");
    else
        $('#newconv').addClass("hidden");
}

function createNewConv() {
    var convData = {
        mode: 10,
        name: $('#convname').val(),
        desc: $('#convdesc').val()
    };
    $.post("/main/conversations", convData, onLoadConversations, "json");
    $('#ownconv').prop("checked", true);
    showNewConv(false);
}