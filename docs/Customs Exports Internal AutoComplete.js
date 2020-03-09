// ==UserScript==
// @name         Customs Exports Internal AutoComplete
// @namespace    http://tampermonkey.net/
// @version      1.0
// @description  Customs Exports Internal AutoComplete
// @author       You
// @match        http*://*/*customs-exports-internal*
// @grant        none
// @updateURL    https://raw.githubusercontent.com/hmrc/customs-exports-internal-frontend/master/docs/Customs%20Exports%20Internal%20AutoComplete.js
// ==/UserScript==

(function() {
    'use strict';
    document.getElementsByTagName("body")[0].appendChild(createQuickButton());
})();

function createQuickButton() {
    let button = document.createElement('button');
    button.id="quickSubmit";
    if (!!document.getElementById('global-header')) {
        button.classList.add('button-start');
    } else {
        button.classList.add('govuk-button');
    }
    button.style.position = "absolute"
    button.style.top = "50px"
    button.innerHTML = 'Quick Submit';
    button.onclick = () => completePage();
    return button;
}

// selected can be an index or a value
function selectFromAutoPredict(element, selected) {
    let index = typeof selected == "number" ? selected : 0;
    let selects = element.getElementsByTagName('select');
    let inputs = element.getElementsByTagName('input');
    for(let j = 0; j < selects.length; j++){
        let options = selects[j].getElementsByTagName('option');
        let option = options[index];
        if(typeof selected == "string"){
            for(let o = 0; o < options.length; o++) {
                if(options[o].value === selected) {
                    option = options[o];
                }
            }
        }
        option.selected = "selected";
        selects[j].value = option.value;
        inputs[j].value = option.value;
    }
}

function currentPageIs(path) {
    let matches = window.location.pathname.match(path + "$");
    return matches && matches.length > 0
}

function completePage() {
    if(currentPageIs('/customs-exports-internal/start')){
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/choice")){
        document.getElementById("choice").checked = true
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/movement-details")){
        let title = document.title.toLowerCase();
        const validDate = new Date();
        validDate.setDate(validDate.getDate() - 1); // One day before
        if (title.indexOf('departure') !== -1) {
            document.getElementById('dateOfDeparture_day').value = validDate.getDate();
            document.getElementById('dateOfDeparture_month').value = validDate.getMonth()+1;
            document.getElementById('dateOfDeparture_year').value = validDate.getFullYear();
            document.getElementById('timeOfDeparture_hour').value = '10';
            document.getElementById('timeOfDeparture_minute').value = '00';
        }
        if (title.indexOf('arrival') !== -1) {
            document.getElementById('dateOfArrival_day').value = validDate.getDate();
            document.getElementById('dateOfArrival_month').value = validDate.getMonth()+1;
            document.getElementById('dateOfArrival_year').value = validDate.getFullYear();
            document.getElementById('timeOfArrival_hour').value = '10';
            document.getElementById('timeOfArrival_minute').value = '00';
        }
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/location")){
        document.getElementById('code').value = 'GBAUEMAEMAEMA';
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/goods-departed")){
        document.getElementById("departureLocation").checked = true
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/transport")){
        document.getElementById("modeOfTransport").checked = true
        document.getElementById('nationality').value = 'GB';
        document.getElementById('transportId').value = 'TransportReference';
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/summary")){}
    if(currentPageIs("/customs-exports-internal/mucr-options")){
        document.getElementById("createOrAdd").checked = true;
        document.getElementById("newMucr").value = "GB/1234-123ABC456DEFIIIII";
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/associate-ucr")){
        document.getElementById("kind").checked = true
        const now = new Date();
        document.getElementById("ducr").value = `5GB123456789000-${now.valueOf()}IIIII`;
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/associate-ucr-summary")){}
    if(currentPageIs("/customs-exports-internal/dissociate-ucr-summary")){}
    if(currentPageIs("/customs-exports-internal/shut-mucr-summary")){}
}
