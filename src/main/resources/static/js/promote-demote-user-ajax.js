$(document).ready(function() {
    setClickListenersOnButtons();
});

function setClickListenersOnButtons() {
    $(".promote-user-button").click(function (event) {
        event.preventDefault();
        event.stopPropagation();
        promoteConfirmation(event);
    });
    $(".demote-user-button").click(function (event) {
        event.preventDefault();
        event.stopPropagation();
        // demoteConfirmation(event);
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

function promoteUser(event) {
    let elem = $(event.target)[0];
    let username = $(elem).attr("data-username");
    let promotableRank = $(elem).attr("data-promotable-rank");

    let req = {
        username: username,
        promotableRank: promotableRank
    };
    console.log(req);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        dataType: 'json',
        url: "/promoteUserAjax",
        cache: false,
        data: JSON.stringify(req)
    }).done(function (resp) {
        console.log("### Resp:");
        console.log(resp);

        updateButtons(resp.newPromoteButtonUrl, resp.newDemoteButtonUrl)
        alert(resp.message);
    }).fail(function(xhr, status, e) {
        if (xhr.status === 401 || xhr.status === 404) {
            alert(xhr.responseJSON.message);
        } else {
            alert("Unknown error when promoting user!");
        }
    });
}

function updateButtons(promoteButtonUrl, demoteButtonHtml) {
    $("#promote-user-button-container").load(promoteButtonUrl, function() {
        setClickListenersOnButtons();
    });
    // $('#demote-user-button-container').load(demoteButtonUrl, function() { // TODO: Update
    //     setClickListenersOnButtons();
    // });
}