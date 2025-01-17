$(document).ready(function() {
    $(".restore-post-button").click(function (event) {
        event.preventDefault();
        event.stopPropagation();
        restorePostConfirmation(event);
    });
});

function restorePostConfirmation(event) {
    let c = confirm("Are you sure you wish to restore that post?");
    if (c) {
        restorePost(event);
    }
}

function restorePost(event) {
    let elem = $(event.target)[0];
    let postId = $(elem).attr("data-post-id");

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
        url: "/restorePostAjax",
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
            alert("Unknown error when restoring post!");
        }
    });
}