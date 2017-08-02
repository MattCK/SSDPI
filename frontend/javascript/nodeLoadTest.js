"use strict";
var elementLoadTest;
(function (elementLoadTest) {
    let testElement;
    $(function () {
        testElement = document.getElementById("output");
    });
    function runTest() {
        testElement.innerHTML = "Inserted Text";
    }
    elementLoadTest.runTest = runTest;
})(elementLoadTest || (elementLoadTest = {}));
