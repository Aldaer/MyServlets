var details = $('.details');
var friendList;
var displayedList;
var filterMode = 0;
var displayedId;
var andShow = false;

var body = $("BODY");

$(document).ready(function () {

    details.change(function () {
        $(this).addClass("edited");
        $('#update').removeClass("hidden");
    });

    $('#userlistbox').on("click", ".userlink", function () {
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

    $('#createmsg').click(function() {
        var recp = $('#recipient');
        recp.text($('#login').text());
        if (isFriend(displayedId))
            recp.addClass("friend");
        else
            recp.removeClass("friend");
        $('#newmsg').addClass("centered");
    });

    $('#cancel').click(closeMessageBox);

    $('#send').click(sendNow);

    body.addClass("waiting");
    $.getJSON("/main/userSearch?friends=ids", updateFriendList);
});

function updateFriendList(flist) {
    body.removeClass("waiting");
    friendList = flist;
    updateElements();
    if (andShow) displayFilteredUsers(filterMode);    
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
            if (exists) $('#remfriend').removeClass("hidden");
        } else {
            if (exists) $('#addfriend').removeClass("hidden");
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
    $.getJSON("/main/userSearch?details=" + encodeURIComponent(name), function (usr) {
        exists = usr.exists;
        $('#login').text(usr.username);
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
        $('#fpanel').removeClass('on');
        querybox.css("color", "red");
        querybox.focus();
    } else {
        $('#fpanel').addClass('on');
        querybox.css("color", "initial");
        body.addClass("waiting");
        $.getJSON("/main/userSearch?query=" + encodeURIComponent(querybox.val()), onLoadUsers);
    }
});

function onLoadUsers(data) {
    body.removeClass("waiting");
    $('#usersFoundHeader').text(usersFoundMsg + data.users.length);
    $('#userlistbox').empty();
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
    $('#userlistbox').append(mdiv);
}

function displayFilteredUsers(mode) {
    filterMode = mode;
    $('#userlistbox').empty();
    $.each(displayedList, outputUser);
}

function showFriends() {
    $('#fpanel').addClass('on');
    $.getJSON("/main/userSearch?friends=all", onLoadUsers);
}

function addToFriends(id) {
    var msgData = {
        action: "addfriend",
        id: id,
        friends: "ids"
    };
    requestFriendUpdate(msgData);
}

function removeFromFriends(id) {
    var msgData = {
        action: "remfriend",
        id: id,
        friends: "ids"
    };
    requestFriendUpdate(msgData);
}

function requestFriendUpdate(data) {
    body.addClass("waiting");
    andShow = true;
    $.getJSON("/main/updateUser", data, updateFriendList);    
}

function sendNow() {
    var msgData = {
        action: "send",
        to: $('#recipient').text(),
        refId: 0,
        convId: 0,
        text: $('#msgtext').val()
    };
    $.post("/main/messageAction", msgData, closeMessageBox);
}

function closeMessageBox() {
    $('#newmsg').removeClass("centered");
}