$.ajaxSetup({cache: false});

function closeDialog() {
    if (dialog != null) {
        dialog.close();
    }
}

/*function loadPage(a) {
    let url = $(a).attr('href');
    return loadPageWithUrl(url);
}*/

function loadPageWithUrl(url) {
    $.ajax({
        type: 'GET',
        url: url,
        dataType: "html",
        success: function (d) {
            $('#content').html(d);
        },
        error: function (xhr, textStatus, errorThrown) {
        }
    });
    return true;
}

function loadData(url, data, containerId, dataTableId, nextSuccessAction) {
    waitingDialog.show(messageLoading);
    if (containerId === undefined || containerId === '' || containerId === "") containerId = "#table-content";
    if (dataTableId === undefined || dataTableId === '' || dataTableId === "") dataTableId = "#datatable";
    $.ajax({
        type: "GET",
        url: url,
        data: data,
        dataType: "html",
        success: function (d) {
            $(containerId).html(d);
            handlingDataTable(dataTableId);
            if (nextSuccessAction !== undefined) {
                if (nextSuccessAction !== null) {
                    nextSuccessAction();
                }
            }
        },
        error: function (xhr, textStatus, errorThrown) {
            waitingDialog.hide();
            handleAjaxResponse(xhr, url);
        }
    }).done(function () {
        setTimeout(function () {
            $('.modal-backdrop, .modal-dialog, .modal').remove();
        }, 1);
    });
    $('body').css('padding-right', '');
}

function loadViewData(url, data, containerId, dataTableId, isFirst, nextSuccessAction) {
    if (!isFirst) {
        waitingDialog.show(messageLoading);
    }

    if (containerId === undefined || containerId === '' || containerId === "") containerId = "#table-content";
    if (dataTableId === undefined || dataTableId === '' || dataTableId === "") dataTableId = "#datatable";
    $.ajax({
        type: "GET",
        url: url,
        data: data,
        dataType: "html",
        success: function (d) {
            waitingDialog.hide();
            $(containerId).html(d);
            handlingDataTable(dataTableId);
            if (nextSuccessAction !== undefined) {
                if (nextSuccessAction !== null) {
                    nextSuccessAction();
                }
            }
        },
        error: function (xhr, textStatus, errorThrown) {
            waitingDialog.hide();
            handleAjaxResponse(xhr, url);
        }
    });
    $('body').css('padding-right', '');
}

function handlingDataTable(dataTableId) {
    let dataTableType = $(dataTableId).attr('data-table-type'); //treeview or nothing
    let table = null;
    if (dataTableType === 'treeview') {
        table = $(dataTableId).treetable({
            expandable: true,
            initialState: "expanded",
            clickableNodeNames: true
        });
    } else {
        table = $(dataTableId).DataTable({
            "paging": false,
            "ordering": true,
            "info": false,
            "dom": '<"top"i>rt<"bottom"flp><"clear">'
        });
    }
    $(dataTableId + ' tbody').on('contextmenu', 'tr td', function (e) {
        let contextMenuTarget = $(this.parentNode).attr('data-context-menu-target');
        if (contextMenuTarget !== undefined) {
            contextMenu = $("#" + contextMenuTarget);
            var top = e.pageY;
            var left = e.pageX;
            $('#context-menu-holder').css({
                display: 'block',
                position: 'absolute',
                top: top,
                left: left
            });
            $('#context-menu-holder').html(contextMenu.html());
            $('#context-menu-holder').show();
            return false;
        }
    });
    $('#datatable_filter input').addClass('form-control input-sm'); // <-- add this line
    $('#datatable_filter input').attr("placeholder", "Type here to search");
}

function openDialog(a) {
    var disabled = $(a).attr("disabled");
    if (disabled === 'disabled') {
        return false;
    }
    var title = $(a).attr("data-title");
    var size = $(a).attr("data-size");
    var url = $(a).attr("data-url");
    var closable = $(a).attr("data-closable");
    var isTrueClosable = (closable?.toLowerCase?.() === 'true');
    if (closable === undefined) {
        isTrueClosable = true;
    }
    return openDialogWithParameters(title, url, size, isTrueClosable);
}

function openDialogWithParameters(title, url, size, closable) {

    if (title === undefined) title = "Undefined";
    if (url === undefined) {
        alert('URL is required');
        return false;
    }

    if (size === undefined) size = BootstrapDialog.SIZE_SMALL;
    if (size === "S") size = BootstrapDialog.SIZE_SMALL;
    if (size === "N") size = BootstrapDialog.SIZE_NORMAL;
    if (size === "W") size = BootstrapDialog.SIZE_WIDE;
    if (size === "F") size = BootstrapDialog.SIZE_FULL;

    waitingDialog.show(messageLoading);

    $.ajax({
        type: "GET",
        url: url,
        dataType: "html",
        success: function (d) {
            waitingDialog.hide();
            dialog = BootstrapDialog.show({
                message: d.replace(/(\r\n|\n|\r)/gm, ""),
                title: title,
                type: BootstrapDialog.TYPE_DEFAULT,
                size: size,
                closable: closable,
                closeByBackdrop: false,
                closeByKeyboard: false,
                closeButton: true
            });
        },
        error: function (xhr, textStatus, errorThrown) {
            waitingDialog.hide();
            handleAjaxResponse(xhr, url);
        }
    });
    return false;
}

function confirmDialog(a) {
    var url = $(a).attr("data-url");
    var title = $(a).attr("data-title");
    waitingDialog.show(messageLoading);
    $.ajax({
        type: "GET",
        url: url,
        dataType: "html",
        success: function (d) {
            waitingDialog.hide();
            swal({
                title: title + '?',
                html: d,
                type: 'question',
                focusCancel: true,
                showCancelButton: true,
                confirmButtonColor: '#3085d6',
                cancelButtonColor: '#d33',
                confirmButtonText: 'Yes, do it!',
                reverseButtons: true
            }).then(function (result) {
                if (result.value) {
                    $('#main-form').submit();
                }
            })
        },
        error: function (xhr, textStatus, errorThrown) {
            waitingDialog.hide();
            handleAjaxResponse(xhr, url);
        }
    });
}

function submitForm(form, submitSuccessNext) {
    waitingDialog.show(messageLoading);
    var vForm = $(form);
    var url = vForm.attr('action');
    var viewType = vForm.attr("data-view-type");
    if (url === undefined || url === "" || url === null) {
        alert("FormSubmit missed Action URL");
        return false;
    }
    var submitButton = $('button[type="submit"]');
    submitButton.addClass("disabled");
    $.ajax({
        type: "POST",
        url: url,
        content: "application/json; charset=utf-8",
        dataType: "html",
        data: vForm.serialize(),
        success: function (response, textStatus, xhr) {
            closeDialog();
            waitingDialog.hide();
            $('.custom-modal-dialog').modal('hide');
            if (viewType === undefined || viewType === null) {
                swal({
                    //position: 'top',
                    title: messageExecutedSuccessfully,
                    html: response,
                    type: "success"
                }).then(function () {
                    if (submitSuccessNext !== undefined) {
                        if (submitSuccessNext !== null) {
                            submitSuccessNext();
                        }
                    }
                });
            } else if (viewType === "FILE") {
                // window.location = response;
                swal({
                    //position: 'top',
                    title: messageExecutedSuccessfully,
                    html: "Please download file",
                    type: "success",
                    confirmButtonText: "Download"
                }).then(function () {
                    downloadFileHandler(xhr, response);
                });
                submitButton.removeClass("disabled");
            }

        },
        error: function (xhr, textStatus, errorThrown) {
            waitingDialog.hide();
            submitButton.removeClass("disabled");
            handleAjaxResponse(xhr, url);
        }
    });
    return false;
}

function submitAttForm(form, submitSuccessNext) {
    var vForm = $(form);
/*    var disabled = vForm.attr("disabled");
    if (disabled === 'disabled') {
        return false;
    }*/
    waitingDialog.show(messageLoading);
    var url = vForm.attr('action');
    var formData = new FormData(form);
    var viewType = vForm.attr("data-view-type");
    var title = vForm.attr("data-title");
    var size = vForm.attr("data-size");

    if (size == undefined) size = BootstrapDialog.SIZE_SMALL;
    if (size == "S") size = BootstrapDialog.SIZE_SMALL;
    if (size == "N") size = BootstrapDialog.SIZE_NORMAL;
    if (size == "W") size = BootstrapDialog.SIZE_WIDE;
    if (size == "L") size = BootstrapDialog.SIZE_LARGE;
    if (size == "F") size = BootstrapDialog.SIZE_FULL;

    // alert(vForm.attr("id"))
    if (url === undefined || url === "" || url === null) {
        alert("FormSubmit missed Action URL");
        return false;
    }
/*    alert(formData.values());
    for (var value of formData.values()) {
        console.log('V => ' + value);
    }
    for (var key of formData.keys()) {
        console.log('K => ' + key);
    }*/

    $.ajax({
        type: "POST",
        url: url,
        data: formData,
        enctype: 'multipart/form-data',
        contentType: false,
        cache: false,
        processData: false,
        success: function (response, status, xhr) {
            closeDialog();
            waitingDialog.hide();
            if (viewType == undefined || viewType == null) {
                swal({
                    title: messageExecutedSuccessfully,
                    html: response,
                    type: 'success'
                }).then(function () {
                    if (submitSuccessNext !== undefined) {
                        if (submitSuccessNext !== null) {
                            submitSuccessNext();
                        }
                    }
                });
            } else if (viewType == "DIALOG") {
                dialog = BootstrapDialog.show({
                    message: response.replace(/(\r\n|\n|\r)/gm, ""),
                    title: title,
                    type: BootstrapDialog.TYPE_DEFAULT,
                    size: size,
                    draggable: true,
                    closable: false,
                    closeButton: true,
                    onshown: function (dialogRef) {
                        $("#datatable").DataTable({
                            keys: true
                        });
                        /*}
                        alert(table);
                        $('#datatable_filter input').addClass('form-control input-sm'); // <-- add this line
                        $('#datatable_filter input').attr("placeholder", "Type here to search");*/
                    }
                });
            }
        },
        error: function (xhr, textStatus, errorThrown) {
            waitingDialog.hide();
            handleAjaxResponse(xhr, url);
        }
    });
    return false;
}

function downloadFileHandler(xhr, data, callback) {
    var filename = "";
    var disposition = xhr.getResponseHeader('Content-Disposition');
    if (disposition && disposition.indexOf('attachment') !== -1) {
        var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        var matches = filenameRegex.exec(disposition);
        if (matches != null && matches[1]) filename = matches[1].replace(/['"]/g, '');
    }
    var type = xhr.getResponseHeader('Content-Type');
    var blob = new Blob([data], {type: type});
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
        // IE workaround for "HTML7007: One or more blob URLs were revoked by closing the blob for which they were created. These URLs will no longer resolve as the data backing the URL has been freed."
        window.navigator.msSaveBlob(blob, filename);
    } else {
        var URL = window.URL || window.webkitURL;
        var downloadUrl = URL.createObjectURL(blob);
        if (filename) {
            // use HTML5 a[download] attribute to specify filename
            var a = document.createElement("a");
            // safari doesn't support this yet
            if (typeof a.download === 'undefined') {
                window.location = downloadUrl;
            } else {
                a.href = downloadUrl;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
            }
        } else {
            window.location = downloadUrl;
        }
        if (callback !== undefined)
            callback();

        setTimeout(function () {
            URL.revokeObjectURL(downloadUrl);
        }, 100); // cleanup
    }
}

function handleAjaxResponse(xhr, url) {
    if (xhr.status === 0) {
        swal(offlineTitle, offlineMessage, "error");
    } else if (xhr.status === 404 || xhr.status === 405) {
        swal(pageNotFoundTitle, pageNotFoundMessage, "error");
    } else if (xhr.status === 403) {
        swal(forbiddenTitle, forbiddenMessage, "error");
    } else if (xhr.status === 422) {
        toastr.options = {
            "closeButton": true,
            "debug": false,
            "newestOnTop": false,
            "progressBar": true,
            "positionClass": "toast-top-right",
            "preventDuplicates": false,
            "onclick": null,
            "showDuration": "300",
            "hideDuration": "1500",
            "timeOut": "5000",
            "extendedTimeOut": "1000",
            "showEasing": "swing",
            "hideEasing": "linear",
            "showMethod": "fadeIn",
            "hideMethod": "fadeOut"
        }
        toastr.error(xhr.responseText, 'Error');
    } else if (xhr.status === 401) {
        swal(sessionExpiredTitle, sessionExpiredMessage, "error");
    } else if (xhr.status === 500) {
        swal(internalServerErrorTitle, xhr.responseText, "error");
    } else {
        swal(somethingWentWrongTitle, xhr.responseText, "error");
    }
}


function handlingDataTable(dataTableId) {
    let dataTableType = $(dataTableId).attr('data-table-type'); //treeview or nothing
    let table = null;
    if (dataTableType === 'treeview') {
        // table = $(dataTableId).DataTable({
        //     "paging": false,
        //     "ordering": false,
        //     "info": false,
        //     "dom": '<"top"i>rt<"bottom"flp><"clear">',
        // });
        table = $(dataTableId).treetable({
            expandable: true,
            initialState: "expanded",
            clickableNodeNames: true
        });
    } else {
        table = $(dataTableId).DataTable({
            "paging": false,
            "ordering": true,
            "info": false,
            "dom": '<"top"i>rt<"bottom"flp><"clear">'
        });
    }

    $(dataTableId + ' tbody').on('click', 'tr td:not(:last-child,:first-child)', function (e) {
        let tr = this.parentNode;
        let dataUrl = $(tr).attr('data-url');
        let size = $(tr).attr('data-size');
        let title = $(tr).attr('data-title');
        let exceptionalTags = ['a', 'img'];
        if (!exceptionalTags.includes(e.target.tagName.toLowerCase())) {
            openDialogWithParameters(title, dataUrl, size);
            if (window.event.ctrlKey) {
            }
        }
    });

    $(dataTableId + ' tbody').on('contextmenu', 'tr td', function (e) {
        let contextMenuTarget = $(this.parentNode).attr('data-context-menu-target');
        if (contextMenuTarget != undefined) {
            contextMenu = $("#" + contextMenuTarget);
            var top = e.pageY;
            var left = e.pageX;
            $('#context-menu-holder').css({
                display: 'block',
                position: 'absolute',
                top: top,
                left: left
            });
            $('#context-menu-holder').html(contextMenu.html());
            $('#context-menu-holder').show();
            return false;
        }
    });
    $('#datatable_filter input').addClass('form-control input-sm'); // <-- add this line
    $('#datatable_filter input').attr("placeholder", "Type here to search");
}

// local storage management as list.
function setListToLocalStorage(key, value) {
    let list = JSON.parse(localStorage.getItem(key)) || [];
    list.push(value);
    localStorage.setItem(key, JSON.stringify(list));
}

function getListFromLocalStorage(key) {
    return JSON.parse(localStorage.getItem(key)) || [];
}

function removeFromLocalStorage(key) {
    localStorage.removeItem(key);
}

