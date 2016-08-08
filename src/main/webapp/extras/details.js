const DETAILS = $('.details');
var friendList;
var displayedList;
var filterMode = 0;
var andShow = false;

const BODY = $("body");

$(document).ready(function () {

    DETAILS.change(function () {
        $(this).addClass("edited");
        $('#update').removeClass("hidden");
    });

    $('#userlistbox').on("click", ".userlink", function (event) {
        event.preventDefault();
        reloadUser(this.text);
        return false;
    });

    $('#allfriends').click(showFriends);

    $('#addfriend').click(addToFriends);

    $('#remfriend').click(removeFromFriends);

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

    BODY.addClass("waiting");
    $.getJSON("/main/userSearch?friends=ids", updateFriendList);
});

function updateFriendList(flist) {
    BODY.removeClass("waiting");
    friendList = flist;
    updateElements();
    if (andShow) displayFilteredUsers(filterMode);    
}

function isFriend(id) {
    return friendList.includes(id);
}

function updateElements() {
    if (own) {
        DETAILS.prop("disabled", false);
        $('#update').removeClass("hidden");
        $('#addfriend').addClass("hidden");
        $('#remfriend').addClass("hidden");
    } else {
        $('#update').addClass("hidden");
        DETAILS.prop("disabled", true);
        $('.warning').addClass("hidden");
        if (isFriend(displayedId)) {
            $('#addfriend').addClass("hidden");
            if (exists) $('#remfriend').removeClass("hidden");
        } else {
            if (exists) $('#addfriend').removeClass("hidden");
            $('#remfriend').addClass("hidden");
        }
    }
    DETAILS.removeClass("edited");

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

$('.DETAILS-form').submit(function () {
    // TODO: user DETAILS form validation
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
        BODY.addClass("waiting");
        $.getJSON("/main/userSearch?query=" + encodeURIComponent(querybox.val()), onLoadUsers);
    }
});

function onLoadUsers(data) {
    BODY.removeClass("waiting");
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

function addToFriends() {
    var msgData = {
        action: "addfriend",
        id: displayedId,
        friends: "ids"
    };
    requestFriendUpdate(msgData);
}

function removeFromFriends() {
    var msgData = {
        action: "remfriend",
        id: displayedId,
        friends: "ids"
    };
    requestFriendUpdate(msgData);
}

function requestFriendUpdate(data) {
    BODY.addClass("waiting");
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