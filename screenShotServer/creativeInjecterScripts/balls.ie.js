for (let selectorIndex = 0; selectorIndex < allSelectors.length; ++selectorIndex) {

	if (allSelectors[selectorIndex].selector().includes("div-gpt-gallery-top")){
		allSelectors.splice(selectorIndex, 1);
	}

}
