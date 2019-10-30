// ==UserScript==
// @name         Customs Exports Internal AutoComplete
// @namespace    http://tampermonkey.net/
// @version      0.1
// @description  Customs Exports Internal AutoComplete
// @author       You
// @match        http*://*/customs-exports-internal*
// @grant        none
// @updateURL    https://raw.githubusercontent.com/hmrc/customs-exports-internal-frontend/master/docs/Customs%20Exports%20Internal%20AutoComplete.js
// ==/UserScript==

(function() {
    'use strict';
    document.getElementById('global-header').appendChild(createQuickButton());
})();

function createQuickButton() {
    let button = document.createElement('button');
    button.id="quickSubmit";
    button.classList.add('button-start');
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

function selectRadioOption(element, index){
    let inputs = element.getElementsByTagName('input');
    if (inputs && index < inputs.length) {
        inputs[index].checked = true
    }
}

function currentPageIs(path) {
    let matches = window.location.pathname.match(path);
    return matches && matches.length > 0
}

function completePage() {
    if (currentPageIs('/customs-exports-internal/start')) {
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/choice")){
        selectRadioOption(document.getElementById("choice"), 0);
        document.getElementsByClassName('button')[0].click()
    }

    if(currentPageIs("/customs-exports-internal/consignment-references")){
        selectRadioOption(document.getElementById("reference"), 0);
        document.getElementById('ducrValue').value = '8GB12345' + Math.floor(Math.random() * 8999) + 100 + '-101SHIP1';
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/arrival-reference")){
        document.getElementById("reference").value = "REF" + Math.floor(Math.random() * 8999) + 100;
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/movement-details")){
        let title = document.title.toLowerCase();
        const departure = new Date();
        departure.setDate(departure.getDate() - 1); // One day before
        if(title.indexOf('departure') != -1) {
            document.getElementById('dateOfDeparture_day').value = now.getDate();
            document.getElementById('dateOfDeparture_month').value = now.getMonth();
            document.getElementById('dateOfDeparture_year').value = now.getFullYear();
            document.getElementById('timeOfDeparture_hour').value = '21';
            document.getElementById('timeOfDeparture_minute').value = '37';
        }
        if(title.indexOf('arrival') != -1) {
            document.getElementById('dateOfArrival_day').value = now.getDate();
            document.getElementById('dateOfArrival_month').value = now.getMonth();
            document.getElementById('dateOfArrival_year').value = now.getFullYear();
            document.getElementById('timeOfArrival_hour').value = '21';
            document.getElementById('timeOfArrival_minute').value = '37';
        }
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/location")){
        document.getElementById('code').value = 'GBAUEMAEMAEMA';
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/transport")){
        selectRadioOption(document.getElementById("modeOfTransport"), 0);
        document.getElementById('nationality').value = 'GB';
        document.getElementById('transportId').value = 'TransportReference';
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/summary")){
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/mucr-options")){
        document.getElementById("mucrOptions.create").checked = true;
        document.getElementById("newMucr").value = "GB/1234-123ABC456DEFIIIII"
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/associate-ducr$")){
        selectRadioOption(document.getElementById("kind"), 0);
        const now = new Date()
        document.getElementById("ducr").value = `5GB123456789000-${now.valueOf()}IIIII`
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/associate-ducr-summary")){
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/disassociate-ucr-summary")){
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-exports-internal/disassociate-ucr")){
        selectRadioOption(document.getElementById("kind"), 0);
        const now = new Date()
        document.getElementById("ducr").value = `5GB123456789000-${now.valueOf()}IIIII`
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("customs-exports-internal/shut-mucr")){
        const now = new Date()
        document.getElementById("mucr").value = `GB/ABCDE1234-${now.valueOf()}IIIII`
        document.getElementsByClassName('button')[0].click()
    }
}