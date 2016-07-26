$('#showmsg').click(function(){
    $('#slideout').toggleClass('on');
    $('#messagedata').html('');

    $.getJSON("/main/messages?type=from,to&offset=0&limit=20&convId=0", loadmessages());
});

function loadmessages(data,status,xhr) {
  alert("Messages received!");
};