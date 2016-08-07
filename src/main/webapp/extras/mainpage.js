const BODY = $('BODY');
const MSG_BOX = $('#msgbox');
const CONV_TABLE = $('#convtable');
const MSG_PRIVATE = "0,-1";
const TZ_OFFSET_MS = new Date().getTimezoneOffset() * 60000;

var dispDivs;
var chainSort = true;
var messageCache;
var convCache;
var convListMode = 0;
var currentConv = MSG_PRIVATE;

$('#showmsg').click(function () {
    $('#msglist').addClass('on');
    loadAllMessages(MSG_PRIVATE);
});

$('#showconv').click(function () {
    var convDiv;
    (convDiv = $('#convdiv')).toggleClass("hidden");
    if (!convDiv.hasClass("hidden")) {
        loadConversations(convListMode);
    }
});

$('#showtime').click(function () {
    $('.time').toggleClass("hidden");
});

function loadAllMessages(convid) {
    currentConv = convid;
    $.getJSON("/main/messages?type=from,to&offset=0&limit=20&convId=" + convid, onLoadMessages);
}

var replyingTo;

$('#reply').click(function () {
    var msgData = {
        action: "send",
        to: replyingTo.data("msgFrom"),
        refId: replyingTo.data("msgId"),
        convId: 0,
        text: $('#msgreply').val()
    };
    $.post("/main/messageAction", msgData, closeReply());
    loadAllMessages("0,-1");
});

$('#delete').click(function () {
    var msgData = {
        action: "delete",
        msgId: replyingTo.data("msgId")
    };
    $.post("/main/messageAction", msgData, closeReply());
    replyingTo.remove();
});

$('#closeview').click(function () {
    closeReply();
});

function closeReply() {
    $('#msgview').removeClass("centered");
}

function onLoadMessages(data) {
    /*    alert("Received " + data.messages.length + " of " + data.totalCount + " messages."); */
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

    var msgTime = new Date(msg.utcTimestamp - TZ_OFFSET_MS);

    mdiv.append(ulink, ' [', msgTime.toLocaleString(jsLocale), ']:<br>');
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
        nowReading.css("cursor", "");
        $.post("/main/messageAction?action=update&id=" + nowReading.data("msgId") + "&unread=false");
        $('#messagealert').css("display", "none");
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
    $('#msgview').addClass("centered");
    $('#msgreply').val("");
    var msgPlace = $('#msgtext');
    msgPlace.empty();
    var mClone = replyingTo.clone();
    mClone.css("margin", "3px");
    msgPlace.append(mClone);
}

function offsetByPx(pxValue, offs, maxoffs) {
    var v = Number(pxValue.substring(0, pxValue.indexOf("px"))) + offs;
    if (v > maxoffs) v = maxoffs;
    return v + "px";
}

function setSortMode(mode) {
    chainSort = (mode == 1);
    if (messageCache == null) loadAllMessages(currentConv);
    else onLoadMessages(messageCache);
}

function loadConversations(mode) {
    convListMode = mode;
    $.getJSON("/main/conversations?mode=" + convListMode, onLoadConversations);
}

function onLoadConversations(data) {
//     alert("Received " + data.length + " conversations.");
    convCache = data;
    CONV_TABLE.empty();
    $.each(data, displayConversation);
}

function displayConversation(i, conv) {
    var newrow = $("<tr></tr>");
    newrow.append("<td>" + conv.name + "</td>");
    newrow.append("<td>" + createdByStr + "</td>");
    newrow.append(userlink(conv.starter).appendTo("<td></td>"));
    newrow.append("<td width='99%'>" + conv.desc + "</td>");
    CONV_TABLE.append(newrow);
}