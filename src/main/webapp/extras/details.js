var details = $('.details');

if (own) {
    details.change(function () {
        $(this).css("color", "green");
    });
} else {
    details.prop("disabled", true);
}

if (! exists) {
    $('#login').addClass("strikeout");
}

$('.details-form').submit(function () {
    // TODO: user details form validation
});

$('#find').click(function () {
    $('#slideout').addClass('on');

    $.getJSON("/main/userSearch?query=" + encodeURIComponent($('#query').val()), onLoadMessages);
});

function onLoadMessages(data) {
    alert("Displaying " + data.messages.length + " of " + data.totalCount + " found users.");
}
