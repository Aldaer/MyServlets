$('#showmsg').click(function () {
    $('#msglist').addClass('on');

    $.getJSON("/main/messages?type=from,to&offset=0&limit=20&convId=0,-1", onLoadMessages);
});

$('#reply').click(function() {
    var msgData = {
        to: replyingTo.data("msgFrom"),
        refId: replyingTo.data("msgId"),
        convId: 0,
        text: $('#msgreply').val()
    }
    $.post("/main/sendMessage", msgData, closeReply());
});

$('#closeview').click(function() {
    closeReply();
});


function closeReply() {
    $('#msgview').removeClass("centered");
}

function onLoadMessages(data) {
/*    alert("Received " + data.messages.length + " of " + data.totalCount + " messages."); */
    $('.databox').html('');
    $.each(data.messages, outputMessage);
}

function outputMessage(i, msg) {
    var mdiv = $('#bubbleprototype').clone();
    mdiv.removeAttr("id");
    if (msg.from == user) {
        mdiv.append("--> ");
        var userlink = $("<a />", {
            href:"/main/userdetails?user=" + encodeURIComponent(msg.to),
            text: msg.to
        });
        mdiv.addClass("messageout");
    } else {
        mdiv.append("<-- ");
        var userlink = $("<a />", {
            href:"/main/userdetails?user=" + encodeURIComponent(msg.from),
            text: msg.from
        });
        mdiv.addClass("messagein");
    }
    if (msg.conversationId == 0) mdiv.addClass("unread");

    mdiv.append(userlink, ':<br>');
    mdiv.append(msg.text);
    mdiv.data("msgId", msg.id);
    mdiv.data("msgFrom", msg.from);
    mdiv.data("msgTo", msg.to);
    mdiv.css("display", "block");
    mdiv.on("click", msg.id, messageClicked);
    $('.databox').append(mdiv);
}

function messageClicked(event) {
    replyingTo = $(event.currentTarget);
    var to = replyingTo.data("msgTo");
    var id = replyingTo.data("msgId");
    if (to == user && replyingTo.hasClass("unread")) {
        replyingTo.removeClass("unread");
        $.post("/main/updateMessage?id=" + id + "&unread=false");
    }
    $('#msgview').addClass("centered");
    $('#msgreply').val("");
    var msgPlace = $('#msgtext');
    msgPlace.empty();
    var mClone = replyingTo.clone();
    mClone.css("margin", "3px");
    msgPlace.append(mClone);
}