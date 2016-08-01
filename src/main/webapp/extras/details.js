var details = $('.details');
var friendList;
var displayedList;
var filterMode = 0;
var displayedId;

$(document).ready(function () {
    updateElements();

    details.change(function () {
        $(this).addClass("edited");
        $('#update').css("display", "inline-block");
    });

    $('#userfindbox').on("click", ".userlink", function () {
        reloadUser(this.text);
    });

    $('#allfriends').on("click", function () {
        showFriends();
    });

    $.getJSON("/main/userSearch?friends=ids", function (flist) {
        friendList = flist;
    });
});

function isFriend(id) {
    return friendList.includes(id);
}

function updateElements() {
    if (own) {
        details.prop("disabled", false);
        $('#update').css("display", "inline-block");
    } else {
        $('#update').css("display", "none");
        details.prop("disabled", true);
        $('.warning').css("display", "none");
        if (isFriend(displayedId)) {

        } else {

        }
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
    $.getJSON("/main/userSearch?details=" + encodeURIComponent(name), function (usr) {
        exists = usr.exists;
        $('input[name="fullname"]').val(usr.fullName);
        $('input[name="email"]').val(usr.email);
        displayedId = usr.id;
    });
    updateElements();
}

$('.details-form').submit(function () {
    // TODO: user details form validation
});

$('#find').click(function () {
    var querybox = $('#query');
    if (querybox.val().length < 2) {
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
    $('#usersFoundHeader').text(usersFoundMsg + data.users.length);
    $('#userfindbox').empty();
    displayedList = data.users;
    displayFilteredUsers(filterMode);
}

function outputUser(i, usr) {
    if (filterMode != 0) {
        var friend = isFriend(usr.id);
        if ((friend && filterMode == 2) || (!friend && filterMode == 1))
            return;
    }
    var mdiv = $('#bubbleprototype').clone();
    mdiv.removeAttr("id");
    var userlink = $("<a />", {
        class: "userlink",
        href: "#",
        text: usr.username
    });

    mdiv.append(userlink, ':<br>');
    mdiv.append(usr.fullName);
    mdiv.css("display", "block");
    if (isFriend(usr.id))
        mdiv.addClass("friend");
    $('#userfindbox').append(mdiv);
}

function displayFilteredUsers(mode) {
    filterMode = mode;
    $('#userfindbox').empty();
    $.each(displayedList, outputUser);
}

function showFriends() {
    $('.slideout').addClass('on');
    $.getJSON("/main/userSearch?friends=all", onLoadUsers);
}