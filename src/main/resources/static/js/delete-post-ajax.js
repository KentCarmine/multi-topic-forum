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
    // console.log("### deletePost fired");
    let elem = $(event.target)[0];
    let postId = $(elem).attr("data-post-id");

    // console.log("### Elem: ");
    // console.log(elem);
    // console.log("### PostId: ");
    // console.log(postId);

    let req = {
        postId: postId,
    };
    // console.log("Req:");
    // console.log(req);

    let token = $("meta[name='_csrf']").attr("content");
    let header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    $.ajax({
        type: "POST",
        contentType: "application/json",
        dataType: 'json',
        url: "/deletePostAjax",
        cache: false,
        data: JSON.stringify(req)
    }).done(function (resp) {
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