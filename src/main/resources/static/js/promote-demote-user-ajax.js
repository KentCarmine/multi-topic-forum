$(document).ready(function() {
    setClickListenersOnButtons();
});

function setClickListenersOnButtons() {
    $(".promote-user-button").off("click");
    $(".promote-user-button").click(function (event) {
        event.preventDefault();
        event.stopPropagation();
        promoteConfirmation(event);
    });

    $(".demote-user-button").off("click");
    $(".demote-user-button").click(function (event) {
        event.preventDefault();
        event.stopPropagation();
        demoteConfirmation(event);
    });
}

function promoteConfirmation(event) {
    let elem = $(event.target)[0];
    let username = $(elem).attr("data-username");
    let promotableRank = $(elem).attr("data-promotable-rank-display");

    let c = confirm("Are you sure you wish to promote " + username + " to " + promotableRank + "?");
    if (c) {
        promoteUser(event);
    }
}

function demoteConfirmation(event) {
    let elem = $(event.target)[0];
    let username = $(elem).attr("data-username");
    let demotableRank = $(elem).attr("data-demotable-rank-display");

    let c = confirm("Are you sure you wish to demote " + username + " to " + demotableRank + "?");
    if (c) {
        demoteUser(event);
    }
}

function promoteUser(event) {
    let elem = $(event.target)[0];
    let username = $(elem).attr("data-username");
    let promotableRank = $(elem).attr("data-promotable-rank");

    let req = {
        username: username,
        promotableRank: promotableRank
    };
    // console.log(req);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        dataType: 'json',
        url: "/promoteUserAjax",
        cache: false,
        data: JSON.stringify(req)
    }).done(function (resp) {
        // console.log("### Resp:");
        // console.log(resp);

        updateButtons(resp.newPromoteButtonUrl, resp.newDemoteButtonUrl)
        alert(resp.message);
    }).fail(function(xhr, status, e) {
        // console.log("Failed!!!");
        // console.log(xhr);
        if (xhr.status === 401 || xhr.status === 404) {
            alert(xhr.responseJSON.message);
        } else {
            alert("Unknown error when promoting user!");
        }
    });
}

function demoteUser(event) {
    let elem = $(event.target)[0];
    let username = $(elem).attr("data-username");
    let demotableRank = $(elem).attr("data-demotable-rank");

    let req = {
        username: username,
        demotableRank: demotableRank
    };
    // console.log(req);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        dataType: 'json',
        url: "/demoteUserAjax",
        cache: false,
        data: JSON.stringify(req)
    }).done(function(resp){
        // console.log("### Resp:");
        // console.log(resp);

        updateButtons(resp.newPromoteButtonUrl, resp.newDemoteButtonUrl)
        alert(resp.message);
    }).fail(function(xhr, status, e){
        // console.log("Failed!!!");
        // console.log(xhr);
        if (xhr.status === 401 || xhr.status === 404) {
            alert(xhr.responseJSON.message);
        } else {
            alert("Unknown error when demoting user!");
        }
    });
}

function updateButtons(promoteButtonUrl, demoteButtonUrl) {
    // console.log("Updating Buttons");

    $("#promote-user-button-container").load(promoteButtonUrl, function(responseText, responseStatus) {
        // console.log("### Response text:")
        // console.log(responseText)
        // console.log("### Response status:")
        // console.log(responseStatus)

        setClickListenersOnButtons();
    });

    $("#demote-user-button-container").load(demoteButtonUrl, function(responseText, responseStatus) {
        // console.log("### Response text:")
        // console.log(responseText)
        // console.log("### Response status:")
        // console.log(responseStatus)

        setClickListenersOnButtons();
    });
}