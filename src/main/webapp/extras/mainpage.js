$('#showmsg').click(function () {
    $('#slideout').toggleClass('on');
    $('#messagealert').html('');

    $.getJSON("/main/messages?type=from,to&offset=0&limit=20&convId=0", onLoadMessages);
});

function onLoadMessages(data) {
    alert("Received " + data.messages.length + " of " + data.totalCount + " messages.");
    $('#messagebox').html('');
    $.each(data.messages, outputMessage)
}

function outputMessage(i, msg) {
    var mdiv = $('#bubbleprototype').clone();
    mdiv.removeAttr("id");
    mdiv.append(msg.from);
    mdiv.append(' => ', msg.to, ':<br>');
    mdiv.append(msg.text);
    mdiv.css("display", "block");
    if (msg.from == user) {
        mdiv.addClass("messageout");
    } else {
        mdiv.addClass("messagein");
    }
    $('#messagebox').append(mdiv);
}