namespace elementLoadTest {
	let testElement: HTMLDivElement;
	$(function() {
		testElement = <HTMLDivElement> document.getElementById("output");
	});

	export function runTest() {
		testElement.innerHTML = "Inserted Text";
	}
}

