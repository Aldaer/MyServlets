$('.message a').click(function () {
    $('form').animate({height: "toggle", opacity: "toggle"}, "slow");
});


$('.login-form').submit(function () {
    var x = document.forms["login"]["j_username"].value;
    if (x == null || x == "") {
        alert(noUser);
        return false;
    }
    return true;
});


$('.register-form').submit(function () {
    var un = document.forms["register"]["j_username"].value;
    if (un == null || un == "") {
        alert(noUser);
        return false;
    }
    if (un.length > 50) {
        alert(unTooLong);
        return false;
    }
    if (un.length < 4) {
        alert(unTooShort);
        return false;
    }


    var p1 = document.forms["register"]["j_password"].value;
    var p2 = document.forms["register"]["j_password2"].value;
    if (p1.length < 3) {
        alert(pwdTooShort);
        return false;
    }

    if (p1 != p2) {
        alert(pwdMismatch);
        return false;
    }
    return true;
});

$('#r_name').keypress(function () {
    $(this).css("color", "");
    $('.register-form .failcause').html("");
});

$(document).ready(function () {
    if (regAttempt != '') {
        var rinp = $('#r_name');
        rinp.val(regAttempt);
        rinp.css("color", "red");
        $('form').animate({height: "toggle", opacity: "toggle"}, "fast");
        $('.register-form .failcause').html(userExists);
    }
});
