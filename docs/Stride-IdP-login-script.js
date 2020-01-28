// ==UserScript==
// @name     Stride IdP login
// @namespace  http://tampermonkey.net/
// @version   0.3
// @description Authenticates with CDS Declare
// @author    You
// @match     http*://*/stride-idp-stub/auth-request?*
// @grant     none
// ==/UserScript==

(function() {
    'use strict';

    document.getElementById("pid").value = "1234";

    document.getElementById("roles").value = "write:customs-inventory-linking-exports";

})();

function createQuickButton() {
    let button = document.createElement('button');
    button.id="quickSubmit";
    button.innerHTML = 'Quick Submit';
    button.onclick = () => document.getElementsByClassName('button')[0].click();
    return button;
}
