$('#showmsg').click(function () {
    $('#slideout').toggleClass('on');

    $.getJSON("/main/messages?type=from,to&offset=0&limit=20&convId=0,-1", onLoadMessages);
});

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
    mdiv.data("msgTo", msg.to);
    mdiv.css("display", "block");
    mdiv.on("click", msg.id, markAsRead);
    $('.databox').append(mdiv);
}

function markAsRead(event) {
    var thisM = $(event.currentTarget);
    var to = thisM.data("msgTo");
    var id = thisM.data("msgId");
    if (to == user && thisM.hasClass("unread")) {
        thisM.removeClass("unread");
        $.post("/main/updateMessage?id=" + id + "&unread=false");
    }
}