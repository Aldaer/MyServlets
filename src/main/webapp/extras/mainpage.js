$('#showmsg').click(function () {
    $('#slideout').toggleClass('on');
    $('#messagealert').html('');

    $.getJSON("/main/messages?type=from,to&offset=0&limit=2&convId=0", loadmessages);
});

function loadmessages(data) {
    alert("Messages received! " + data.totalCount);
};