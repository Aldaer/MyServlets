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
    var un = document.forms["register"]["username"].value;
    if (un == null || un == "") {
        alert(noUser);
        return false;
    }
    if (un.length > 50) {
        alert(unTooLong);
        return false;
    }

    var p1 = document.forms["register"]["password"].value;
    var p2 = document.forms["register"]["password2"].value;
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
