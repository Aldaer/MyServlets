var details = $('.details');

$(document).ready( function() {
    updateElements();
    
    details.change(function () {
        $(this).addClass("edited");
        $('#update').css("display", "inline-block");        
    });
});

function updateElements() {
    if (own) {
        details.prop("disabled", false);
        $('#update').css("display", "inline-block");
    } else {
        $('#update').css("display", "none");
        details.prop("disabled", true);
        $('.warning').css("display", "none");
    }
    details.removeClass("edited");

    if (exists) {
        $('#login').removeClass("strikeout");
    } else {
        $('#login').addClass("strikeout");
    }
}

function reloadUser(name) {
    own = (currUser == name);
    $('#login').text(name);
    $.getJSON("/main/userSearch?details=" + encodeURIComponent(name), function(usr) {
        exists = usr.exists;
        $('input[name="fullname"]').val(usr.fullName);
        $('input[name="email"]').val(usr.email);
    });
    updateElements();
}

$('.details-form').submit(function () {
    // TODO: user details form validation
});

$('#find').click(function () {
    var querybox =$('#query');
    if(querybox.val().length < 2) {
        $('.slideout').removeClass('on');
        querybox.css("color", "red");
        querybox.focus();
    } else {
        $('.slideout').addClass('on');
        querybox.css("color", "initial");
        $.getJSON("/main/userSearch?query=" + encodeURIComponent(querybox.val()), onLoadUsers);
    }
});

function onLoadUsers(data) {
//    alert("Displaying " + data.users.length + " found users.");
    $('#userfindbox').empty();
    $.each(data.users, outputUser);
    
    $('.userlink').on("click", function() {
        reloadUser(this.text);
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
    $('#userfindbox').append(mdiv);
}
