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
    var querybox =$('#query');
    if(querybox.val().length < 2) {
        $('#slideout').removeClass('on');
        querybox.css("color", "red");
        querybox.focus();
    } else {
        $('#slideout').addClass('on');
        querybox.css("color", "initial");
        $.getJSON("/main/userSearch?query=" + encodeURIComponent(querybox.val()), onLoadUsers);
    }
});

function onLoadUsers(data) {
//    alert("Displaying " + data.users.length + " found users.");
    $('.databox').html('');
    $.each(data.users, outputUser);
    
    $('.userlink').on("click", function() {
        alert(this.text);
    });
}

function outputUser(i, usr) {
    var mdiv = $('#bubbleprototype').clone();
    mdiv.removeAttr("id");
    var userlink = $("<a />", {
        class:"userlink",
        href:"#",
        text: usr.username
    });

    mdiv.append(userlink, ':<br>');
    mdiv.append(usr.fullName);
    mdiv.css("display", "block");
    $('.databox').append(mdiv);
}
