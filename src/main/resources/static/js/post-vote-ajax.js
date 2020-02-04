$(document).ready(function() {
    $(".upvote-button").click(function (event) {
        event.preventDefault();
        event.stopPropagation();
        post_vote(event, 1);
    });
    $(".downvote-button").click(function (event) {
        event.preventDefault();
        event.stopPropagation();
        post_vote(event, -1);
    });
});

/* Submit the user's vote via AJAX and then update the DOM on response */
function post_vote(event, vote_value) {
    let elem = $(event.target)[0];
    let postId = $(elem).attr("data-post-id");
    // console.log("Post Id: " + postId);

    let msg = {
        postId: postId,
        voteValue: vote_value
    };

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
        // console.log("Vote updated");

        let upvoteButton = $(".upvote-button[data-post-id=" + response.postId + "]");
        let downvoteButton = $(".downvote-button[data-post-id=" + response.postId + "]");

        upvoteButton.attr("disabled", true);
        downvoteButton.attr("disabled", true);

        let voteCtr = $(".vote-counter[data-post-id=" + response.postId + "]");
        voteCtr.text(response.voteTotal);

        if (response.hasUpvote) {
            upvoteButton.attr("src", "/images/black-arrow-green-filled.png");
        } else {
            upvoteButton.attr("src", "/images/green-arrow-hollow.png");
        }

        if (response.hasDownvote) {
            downvoteButton.attr("src", "/images/black-arrow-red-filled.png");
        } else {
            downvoteButton.attr("src", "/images/red-arrow-hollow.png");
        }
    } else {
        console.log("Vote not updated");
    }
}
