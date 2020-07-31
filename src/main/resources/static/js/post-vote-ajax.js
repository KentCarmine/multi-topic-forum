$(document).ready(function() {
    $(".upvote-button").click(function (event) {
        // console.log("upvote clicked");
        event.preventDefault();
        event.stopPropagation();
        post_vote(event, 1);
    });
    $(".downvote-button").click(function (event) {
        // console.log("downvote clicked");
        event.preventDefault();
        event.stopPropagation();
        post_vote(event, -1);
    });
});

/* Submit the user's vote via AJAX and then update the DOM on response */
function post_vote(event, vote_value) {
    let elem = $(event.target)[0];
    // console.log("### Elem: ");
    // console.log(elem);
    let postId = $(elem).attr("data-post-id");
    // console.log("### Post ID: ");
    // console.log(postId);
    // console.log("### Vote Value: ");
    // console.log(vote_value);

    let msg = {
        postId: postId,
        voteValue: vote_value
    };

    let token = $("meta[name='_csrf']").attr("content");
    let header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    $.ajax({
        type: "POST",
        contentType: "application/json",
        dataType: 'json',
        url: "/handleVoteAjax",
        cache: false,
        data: JSON.stringify(msg)
    }).done(function (resp) {
        console.log("Success");
        console.log(resp);
        updateVoteDisplay(resp);
    }).fail(function (xhr, status, e) {
        console.log("error submitting vote");
    });
}

/* Update the DOM to display current vote state */
function updateVoteDisplay(response) {
    if (response.voteUpdated) {
        // console.log("### updateVoteDisplay called");
        // console.log(response);
        // console.log("Vote updated");

        let upvoteButton = $(".upvote-button[data-post-id=" + response.postId + "]");
        let downvoteButton = $(".downvote-button[data-post-id=" + response.postId + "]");
        // console.log("### upvoteButton: ");
        // console.log(upvoteButton);
        //
        // console.log("\n### downvoteButton: ");
        // console.log(downvoteButton);

        let upvoteButtonSelected = $(".upvote-button-selected[data-post-id=" + response.postId + "]");
        let downvoteButtonSelected = $(".downvote-button-selected[data-post-id=" + response.postId + "]");
        // console.log("### upvoteButtonSelected: ");
        // console.log(upvoteButtonSelected);
        //
        // console.log("\n### downvoteButtonSelected: ");
        // console.log(downvoteButtonSelected);

        let voteCtr = $(".vote-counter[data-post-id=" + response.postId + "]");
        voteCtr.text(response.voteTotal);

        if (response.hasUpvote) {
            upvoteButton.removeClass("displayed");
            upvoteButton.addClass("not-displayed");
            upvoteButtonSelected.removeClass("not-displayed");
            upvoteButtonSelected.addClass("displayed");
        } else {
            console.log("### Hiding upvote button")
            upvoteButton.removeClass("visible");
            upvoteButton.addClass("invisible");
        }

        if (response.hasDownvote) {
            downvoteButton.removeClass("displayed");
            downvoteButton.addClass("not-displayed")
            downvoteButtonSelected.removeClass("not-displayed");
            downvoteButtonSelected.addClass("displayed");
        } else {
            console.log("### Hiding downvote button")
            downvoteButton.removeClass("visible");
            downvoteButton.addClass("invisible");
        }
    } else {
        console.log("Vote not updated");
    }
}

