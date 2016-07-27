$('#showmsg').click(function () {
    $('#slideout').toggleClass('on');
    $('#messagealert').html('');

    $.getJSON("/main/messages?type=from,to&offset=0&limit=20&convId=0", onLoadMessages);
});

function onLoadMessages(data) {
/*    alert("Received " + data.messages.length + " of " + data.totalCount + " messages."); */
    $('#messagebox').html('');
    $.each(data.messages, outputMessage)
}

function outputMessage(i, msg) {
    var mdiv = $('#bubbleprototype').clone();
    mdiv.removeAttr("id");
    if (msg.from == user) {
        var userlink = $("<a />", {
            href:"/main/userdetails?user=" + encodeURIComponent(msg.to),
            text: msg.to
        });
        mdiv.addClass("messageout");
    } else {
        var userlink = $("<a />", {
            href:"/main/userdetails?user=" + encodeURIComponent(msg.from),
            text: msg.from
        });
        mdiv.addClass("messagein");
        if (msg.conversationId == 0) {
            mdiv.addClass("unread");
        }
    }
    mdiv.append(userlink, ':<br>');
    mdiv.append(msg.text);
    mdiv.css("display", "block");
    $('#messagebox').append(mdiv);
}