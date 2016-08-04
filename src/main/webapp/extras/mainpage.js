const body = $('body');
const msgbox = $('#msgbox');

var chainSort = true;
var dispDivs;

$('#showmsg').click(function () {
    $('#msglist').addClass('on');

    loadAllMessages();
});

function loadAllMessages() {
    $.getJSON("/main/messages?type=from,to&offset=0&limit=20&convId=0,-1", onLoadMessages);
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
    loadAllMessages();
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

var tzOffsetMillis;

function onLoadMessages(data) {
    /*    alert("Received " + data.messages.length + " of " + data.totalCount + " messages."); */
    tzOffsetMillis = new Date().getTimezoneOffset() * 60000;
    msgbox.empty();
    dispDivs = [];
    $.each(data.messages, displayMessage);
}

function displayMessage(i, msg) {
    var mdiv = $('#bubbleprototype').clone();
    mdiv.removeAttr("id");
    var userlink;
    if (msg.from == user) {
        mdiv.append("--> ");
        userlink = $("<a />", {
            href: "/main/userdetails?user=" + encodeURIComponent(msg.to),
            text: msg.to
        });
        mdiv.addClass("messageout");
    } else {
        mdiv.append("<-- ");
        userlink = $("<a />", {
            href: "/main/userdetails?user=" + encodeURIComponent(msg.from),
            text: msg.from
        });
        mdiv.addClass("messagein");
        if (!chainSort) mdiv.addClass("rightshift");
    }
    if (chainSort) {

    } else if (msg.from == user)
        mdiv.addClass("leftshift");
    else
        mdiv.addClass("rightshift");

    if (msg.conversationId == 0) {
        mdiv.addClass("unread");
        if (msg.to == user) {
            mdiv.on("mouseenter", startTimer);
            mdiv.on("mouseleave", stopTimer);
        }
    }

    var msgTime = new Date(msg.utcTimestamp - tzOffsetMillis);

    mdiv.append(userlink, ' [', msgTime.toLocaleString(jsLocale), ']:<br>');
    mdiv.append(msg.text);

    var msgid = msg.id;
    mdiv.data("msgId", msgid);
    mdiv.data("msgFrom", msg.from);
    mdiv.data("msgTo", msg.to);
    mdiv.css("display", "block");
    mdiv.click(msgid, messageClicked);

    var attachPoint = msgbox;
    if (chainSort && msg.refId > 0)
        for (var j = 0; j < dispDivs.length; j++)
            if (dispDivs[j].data("msgId") == msg.refId) {
                attachPoint = dispDivs[j];
                break;
            }

    attachPoint.append(mdiv);
    dispDivs.push(mdiv);
}


var readTimer;
var nowReading;

function startTimer(event) {
    readTimer = setTimeout(markAsRead, 2000);
    nowReading = $(event.currentTarget);
    body.addClass("waiting");
}

function stopTimer() {
    clearTimeout(readTimer);
    body.removeClass("waiting");
}

function markAsRead() {
    body.removeClass("waiting");
    if (nowReading.hasClass("unread")) {
        nowReading.removeClass("unread");
        nowReading.css("cursor", "");
        $.post("/main/messageAction?action=update&id=" + nowReading.data("msgId") + "&unread=false");
        $('#messagealert').css("display", "none");
    }
}

function messageClicked(event) {
    replyingTo = $(event.currentTarget);
    var to = replyingTo.data("msgTo");
    var id = replyingTo.data("msgId");
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