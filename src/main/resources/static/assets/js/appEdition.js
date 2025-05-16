function hideElement(listHidden) {
    for (let i = 0; i < listHidden.length; i++) {
        $("#" + listHidden[i]).hide();
    }
}

function showElement(listShow) {
    listShow.forEach(x => $("#" + x).show());
}

function switchForm(selectorType, firstPanel, secondPanel) {
    if ($("#" + selectorType).val() === "Individual") {
        $("#" + firstPanel).show();
        $("#" + secondPanel).hide();
    } else if ($("#" + selectorType).val() === "LegalEntity") {
        $("#" + firstPanel).hide();
        $("#" + secondPanel).show();
    } else {
        $("#" + firstPanel).hide();
        $("#" + secondPanel).hide();
    }
}

function switchTab(firstTab, secondTab, thirdTab, tabNo) {
    if (tabNo === 1) {
        $("#" + firstTab).addClass(" current").attr('aria-disabled', 'false').attr('aria-selected', 'true');
        $("#" + secondTab).removeClass(' current').attr('aria-disabled', 'true').attr('aria-selected', 'false');
        $("#" + thirdTab).removeClass(' current').attr('aria-disabled', 'true').attr('aria-selected', 'false');
    }

    if (tabNo === 2) {
        $("#" + firstTab).removeClass(' current').attr('aria-disabled', 'true').attr('aria-selected', 'false');
        $("#" + secondTab).addClass(" current").attr('aria-disabled', 'false').attr('aria-selected', 'true');
        $("#" + thirdTab).removeClass(' current').attr('aria-disabled', 'true').attr('aria-selected', 'false');
    }

    if (tabNo === 3) {
        $("#" + firstTab).removeClass(' current').attr('aria-disabled', 'true').attr('aria-selected', 'false');
        $("#" + secondTab).removeClass(' current').attr('aria-disabled', 'true').attr('aria-selected', 'false');
        $("#" + thirdTab).addClass(" current").attr('aria-disabled', 'false').attr('aria-selected', 'true');
    }

}

function setElementText(listElement) {
    for (let i = 0; i < listElement.length; i++) {
        $('#' + listElement[i].keySet).text($("#" + listElement[i].keyGet).val());
    }
}

function toggleTab(position, standingPage, backBtn, nextBtn, isToggle) {
    if (isToggle) {
        if (position === 'next') {
            standingPage = standingPage + 1;
        }
        if (position === 'back') {
            standingPage = standingPage - 1;
        }

        if (standingPage > 1) {
            $("#" + backBtn).show();
        } else {
            $("#" + backBtn).hide();
        }
        if (standingPage < 3) {
            $("#" + nextBtn).show();
        } else {
            $("#" + nextBtn).hide();
        }
    }

    return standingPage;
}

function syncValueIndividual(listDatePicker, listInput, listSelect) {
    listInput.forEach(x => {
        let val = document.querySelectorAll("#" + x + 'Legal');
        if (val.length > 0) {
            setInputValue(x, val[0].value, false);
        }
    });

    listDatePicker.forEach(x => {
        let val = document.querySelectorAll("#" + x + 'Legal-datepicker');
        if (val.length > 0) {
            setDatePickerValue(x + 'Ind-datepicker', val[0].value, true);
        }
    });

    listSelect.forEach(x => {
        let val = document.querySelectorAll("#" + x + 'Legal');
        if (val.length > 0) {
            $("#" + x).selectpicker('val', val[0].value);
        }
    });
}

function syncValueLegal(listDatePicker, listInput, listSelect) {
    listInput.forEach(x => {
        let val = document.querySelectorAll("#" + x);
        if (val.length > 0) {
            setInputValue(x + 'Legal', val[0].value);
        }
    });

    listDatePicker.forEach(x => {
        let val = document.querySelectorAll("#" + x + 'Ind-datepicker');
        if (val.length > 0) {
            setDatePickerValue(x + 'Legal-datepicker', val[0].value);
        }
    });

    listSelect.forEach(x => {
        let val = document.querySelectorAll("#" + x);
        if (val.length > 0) {
            $("#" + x + 'Legal').selectpicker('val', val[0].value);
        }
    });
}

function setDatePickerValue(pickerId, value) {
    $("#" + pickerId).data("DateTimePicker").date(moment(value, 'DD/MM/YYYY'));
}

function setInputValue(inputId, value) {
    document.querySelectorAll("#" + inputId).forEach(element => {
        element.value = value
    });
}

function checkValidTab(listInd, listLegal, listAttInd, listAttLegal, selectorType, page, listEmailInd, listEmailLegal) {
    var listElement = null;
    var listEmail;

    let isValid = true;

    if (page === 1) {
        if ($('#' + selectorType).val() === 'Individual') {
            listElement = listInd;
            listEmail = listEmailInd;
        }
        if ($('#' + selectorType).val() === 'LegalEntity') {
            listElement = listLegal;
            listEmail = listEmailLegal;
        }

    }

    if (page === 2) {
        if ($('#' + selectorType).val() === 'Individual') {
            listElement = listAttInd;
        }
        if ($('#' + selectorType).val() === 'LegalEntity') {
            listElement = listAttLegal;
        }
    }

    $('input').removeClass('error-input-form');
    $('input[required]').each(function () {
        let inputId = $(this).attr('id');
        if (Array.isArray(listElement) && listElement.includes(inputId)) {
            if (!$.trim($(this).val())) {
                $(this).addClass('error-input-form');
                isValid = false;
            } else {
                $(this).removeClass('error-input-form');
            }
        }
        if (Array.isArray(listEmail) && listEmail.includes(inputId)) {
            if (!isValidEmail($(this).val())) {
                $(this).addClass('error-input-form');
                isValid = false;
            } else {
                $(this).removeClass('error-input-form');
            }
        }
    });
    return isValid;
}

function enableRequired(listIndividualRequired, listLegalRequired) {
    listLegalRequired.forEach(x => {
        let val = document.querySelectorAll("#" + x);
        if (val.length > 0) {
            if (val[0].hasAttribute('required') !== true) {
                val[0].setAttribute("required", "");
            }
        }
    });
    listIndividualRequired.forEach(x => {
        let val = document.querySelectorAll("#" + x);
        if (val.length > 0) {
            if (val[0].hasAttribute('required') !== true) {
                val[0].setAttribute("required", "");
            }
        }
    });
}

function disableRequired(selector, listIndividualRequired, listLegalRequired) {
    if ($("#" + selector).val() === "Individual") {
        listLegalRequired.forEach(x => {
            let val = document.querySelectorAll("#" + x);
            if (val.length > 0) {
                if (val[0].hasAttribute('required')) {
                    val[0].removeAttribute("required");
                }
            }
        });
    }
    if ($("#" + selector).val() === "LegalEntity") {
        listIndividualRequired.forEach(x => {
            let val = document.querySelectorAll("#" + x);
            if (val.length > 0) {
                if (val[0].hasAttribute('required')) {
                    val[0].removeAttribute("required");
                }
            }
        });
    }
}

/*fragment app beneficiary account*/
function addObjectToStorage(newObj, key) {
    let existingArray = JSON.parse(localStorage.getItem(key)) || [];
    existingArray.push(newObj);
    localStorage.setItem(key, JSON.stringify(existingArray));
}

function removeObjectById(id, key) {
    let existingArray = JSON.parse(localStorage.getItem(key)) || [];
    existingArray = existingArray.filter(obj => obj.id !== id);
    localStorage.setItem(key, JSON.stringify(existingArray));
}

function clearStorageForKey(key) {
    localStorage.removeItem(key);
}

function getItemByKey(key) {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : null;
}

function isValidEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
}

function setElementTextByValue(listElement, valueObj) {
    for (let i = 0; i < listElement.length; i++) {
        $('#' + listElement[i].keySet).text(valueObj[listElement[i].keyGet]);
    }
}

function toggleElementATag(listDisable, remove) {
    remove === true ? listDisable.forEach(x => $("#" + x).addClass("engagement-disabled")) :
        listDisable.forEach(x => $("#" + x).removeClass("engagement-disabled"));
}

function checkValTagInputByTab(listElement, page){
    var listInput;
    var isValid = true;
    if(page === 1){
        listInput = listElement;
    }
    $('input').removeClass('error-input-form');
    $('input[required]').each(function () {
        let inputId = $(this).attr('id');
        if (Array.isArray(listInput) && listInput.includes(inputId)) {
            if (!$.trim($(this).val())) {
                $(this).addClass('error-input-form');
                isValid = false;
            } else {
                $(this).removeClass('error-input-form');
            }
        }
    });
    return isValid;
}

function addOptions(selectorId, data, btnCollapse) {
    let selectedVal = $(`#${selectorId}`).val();
    let displayTextMap = {
        'trustorEntity': value => value.trustorType === 'Individual' ? value.fullName + " - " + value.documentNumber : value.representativeName + " - " + value.documentNumber,
        'beneficiaryEntity': value => value.beneficiaryType === 'Individual' ? value.fullName + " - " + value.documentNumber : value.representativeName + " - " + value.documentNumber,
        'portfolioEntity': value => value.portfolioName + " - " + value.serviceType.serviceTypeName + " - " + value.total,
        'fundManagerEntity': value => value.fundManagerName + " - " + value.cisName
    };
    $(`#${selectorId} option`).each(function () {
        if ($(this).val() !== '') {
            $(this).remove();
        }
    });

    data.forEach(value => {
        let displayText = displayTextMap[selectorId](value);
        $(`#${selectorId}`)
            .append($("<option></option>")
                .attr("value", value.id)
                .text(displayText));
    });

    if (selectedVal != null && Array.isArray(selectedVal)) {
        $(`#${selectorId} option`).prop('selected', false);

        selectedVal.forEach(value => {
            $(`#${selectorId} option[value="${value}"]`).prop('selected', true);
        });
    }
    $(`#${selectorId}`).selectpicker('refresh');


    if (Number($(`#${selectorId}`).val()) === 0) {
        return toggleElementATag([`${btnCollapse}`], true);
    } else {
        toggleElementATag([`${btnCollapse}`], false);
    }
}

function checkOptionEnggmnt(enggmntAddSptList) {
    let invalid = true;
    enggmntAddSptList.forEach(x => {
        console.log($("#" + x).val());
        $("#" + x + "Div").removeClass('error-div-form');

        if ($("#" + x).val() === "0") {
            invalid = false;
            $("#" + x + "Div").addClass('error-div-form');
        }

        if ($("#" + x).val().length === 0) {
            invalid = false;
            $("#" + x + "Div").addClass('error-div-form');
        }

        if ($("#" + x).val()[0] === '0') {
            invalid = false;
            $("#" + x + "Div").addClass('error-div-form');
        }

    });
    return invalid;
}
