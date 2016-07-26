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
    var mdiv = $('.messagebubble:first').clone();
    mdiv.text(msg.from, ' => ', msg.to, ':<br>');
    alert(msg.text);
    mdiv.append(msg.text);
    mdiv.css("display", "block");
    $('#messagebox').append(mdiv);
}