$(document).ready(function() {
    let errorContainer = $(".form-error-container");

    // console.log("### Error Container:")
    // console.log(errorContainer[0]);

    if (errorContainer[0]) {
        scrollTo(errorContainer);
    }
});

function scrollTo(element) {
    let body = $("html,body");

    body.scrollTop(element.offset().top - body.offset().top + element.scrollTop());
    // console.log("### Scrolling done");
}