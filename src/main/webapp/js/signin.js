var $window = $(window);

$window.load(function() {
	var msgBox = $("#signin-form-messages");
    if (msgBox.has("div").length) {
    	$("#signin-form-panel").width("80%");
	    PF("spot").show();
    }
});

function pushState() {
	history.pushState({}, "", "home");
}

$window.unload(pushState());

$window.on("popstate", pushState);
