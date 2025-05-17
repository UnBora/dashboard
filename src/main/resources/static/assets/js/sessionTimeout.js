var sessionAlive = maximumSessionTimeout; // default session timeout 30m
var notifyBefore = 120; // notify before 2m
var loadDialog = function () {
    var sessionTimeout = null;
    setTimeout(function () {
        $(function () {
            $("#dialogSessionWarning").dialog({
                dialogClass: "no-close",
                autoOpen: true,
                maxWidth: 400,
                maxHeight: 200,
                width: 400,
                height: 200,
                modal: true,
                open: function (event, ui) {
                    sessionTimeout = setTimeout(function () {
                        $(function () {
                            $("#dialogSessionWarning").dialog("close");
                            $("#dialogSessionExpired").dialog({
                                dialogClass: "no-close",
                                autoOpen: true,
                                maxWidth: 400,
                                maxHeight: 200,
                                width: 400,
                                height: 200,
                                modal: true,
                                buttons: {
                                    "Logout": function () {
                                        window.location.href = "/admin/authentication/logout";
                                    }
                                }
                            });
                        });
                    }, notifyBefore * 1000);
                },
                buttons: {
                    "Yes": function () {
                        if (sessionTimeout) {
                            clearTimeout(sessionTimeout); //cancel the previous timer.
                            sessionTimeout = null;
                        }
                        $.get("/admin/authentication/session-timeout", function (data, status) {
                            if (data === "true" && status === "success") {
                                $("#dialogSessionWarning").dialog("close");
                                loadDialog();
                            } else {
                                window.location.href = "/admin/authentication/logout";
                            }
                        });
                    },
                    "No": function () {
                        window.location.href = "/admin/authentication/logout";
                    }
                }
            });
        });
        countDownTime();
    }, (sessionAlive - notifyBefore) * 1000);
};

var countDownTime = function () {
    var count = notifyBefore, timer = setInterval(function () {
        $("#secondCounter").html(count--);
        if (count === 0) clearInterval(timer);
    }, 1000);
};

loadDialog();