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

