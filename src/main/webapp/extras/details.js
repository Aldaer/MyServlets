var details = $('.details');
var friendList;
var displayedList;
var filterMode = 0;
var displayedId;

var body = $("body");

$(document).ready(function () {

    details.change(function () {
        $(this).addClass("edited");
        $('#update').removeClass("hidden");
    });

    $('#userfindbox').click(".userlink", function () {
        reloadUser(this.text);
    });

    $('#allfriends').click(function () {
        showFriends();
    });

    $('#addfriend').click(function () {
        addToFriends(displayedId);
    });

    $('#remfriend').click(function () {
        removeFromFriends(displayedId);
    });

    getFriendList();
});

function getFriendList() {
    body.addClass("waiting");
    $.getJSON("/main/userSearch?friends=ids", function (flist) {
        body.removeClass("waiting");        
        friendList = flist;
        updateElements();
    });
}

function isFriend(id) {
    return friendList.includes(id);
}

function updateElements() {
    if (own) {
        details.prop("disabled", false);
        $('#update').removeClass("hidden");
        $('#addfriend').addClass("hidden");
        $('#remfriend').addClass("hidden");
    } else {
        $('#update').addClass("hidden");
        details.prop("disabled", true);
        $('.warning').addClass("hidden");
        if (isFriend(displayedId)) {
            $('#addfriend').addClass("hidden");
            $('#remfriend').removeClass("hidden");
        } else {
            $('#addfriend').removeClass("hidden");
            $('#remfriend').addClass("hidden");
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
        updateElements();
    });
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
        body.addClass("waiting");
        $.getJSON("/main/userSearch?query=" + encodeURIComponent(querybox.val()), onLoadUsers);
    }
});

function onLoadUsers(data) {
    body.removeClass("waiting");
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

function addToFriends(id) {
    var msgData = {
        action: "addfriend",
        id: id
    };
    $.post("/main/messageAction", msgData, getFriendList());
}

function removeFromFriends(id) {
    var msgData = {
        action: "remfriend",
        id: id
    };
    $.post("/main/messageAction", msgData, getFriendList());
}
