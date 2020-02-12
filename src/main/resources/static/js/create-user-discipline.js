$(document).ready(function() {
    setChangeListenersOnButtons();
    $("#suspension-radio-button").prop('checked', true);
    $("#ban-radio-button").prop('checked', false);
});

function setChangeListenersOnButtons() {
    $("#suspension-radio-button").change(function (event) {
        event.preventDefault();
        event.stopPropagation();
        if ($(this).is(':checked')) {
            showSuspensionDurationContainer();
        }
    });

    $("#ban-radio-button").change(function (event) {
        event.preventDefault();
        event.stopPropagation();

        if ($(this).is(':checked')) {
            hideSuspensionDurationContainer();
        }
    });
}

function showSuspensionDurationContainer() {
    let container = $("#suspension-duration-container");
    container.show();
}

function hideSuspensionDurationContainer() {
    let container = $("#suspension-duration-container");
    container.hide();
}