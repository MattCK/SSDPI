class User {

    static readonly CONSTANT = "theconstant";

    static getName() {return "Class User Name";}
    
	static get CREATED() {return 'CREATED';}
    
    /**
     * 
     * 
     * @private
     * @type {number}
     * @memberof User
     */
    private _id : number;

    /**
     * 
     * 
     * @returns {number} 
     * @memberof User
     */
    public id() : number {return this._id;}

    public static someFunction(aVar : number | null) : string {
        return "Name" + aVar;
    }
}