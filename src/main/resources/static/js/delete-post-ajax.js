$(document).ready(function() {
    $(".delete-post-button").click(function (event) {
        event.preventDefault();
        event.stopPropagation();
        deletePostConfirmation(event);
    });
});

function deletePostConfirmation(event) {
    let c = confirm("Are you sure you wish to delete that post?");
    if (c) {
        deletePost(event);
    }
}

function deletePost(event) {
    let elem = $(event.target)[0];
    let postId = $(elem).attr("data-post-id");

    let req = {
        postId: postId,
    };
    console.log("Req:");
    console.log(req);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        dataType: 'json',
        url: "/deletePostAjax",
        cache: false,
        data: JSON.stringify(req)
    }).done(function (resp) {
        // console.log("Resp:");
        // console.log(resp);
        if (resp.reloadUrl !== null) {
            window.location.href = resp.reloadUrl;
            window.location.reload(true);
        }
        alert(resp.message);
    }).fail(function (xhr, status, e) {;
        if (xhr.status === 401 || xhr.status === 404) {
            alert(xhr.responseJSON.message);
        } else {
            alert("Unknown error when deleting post!");
        }
    });
}