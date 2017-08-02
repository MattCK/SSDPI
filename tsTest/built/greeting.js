define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    function greeter(person) {
        return "Hello, " + person;
    }
    var user = User.getName(); //"Jane User";
    document.body.innerHTML = greeter(User.getName());
});
