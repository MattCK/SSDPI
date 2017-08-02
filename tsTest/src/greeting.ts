import { UserName } from "./UserName";

function greeter(person: string) : string {
    return "Hello, " + person;
}

let user : string = User.getName(); //"Jane User";

document.body.innerHTML = greeter(User.getName());

