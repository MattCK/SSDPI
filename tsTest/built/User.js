var User = (function () {
    function User() {
    }
    User.getName = function () { return "Class User Name"; };
    Object.defineProperty(User, "CREATED", {
        get: function () { return 'CREATED'; },
        enumerable: true,
        configurable: true
    });
    /**
     *
     *
     * @returns {number}
     * @memberof User
     */
    User.prototype.id = function () { return this._id; };
    User.someFunction = function (aVar) {
        return "Name" + aVar;
    };
    User.CONSTANT = "theconstant";
    return User;
}());
