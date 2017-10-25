let photoCards = document.querySelector(".photo-cards");
if (photoCards) {
	let allCards = photoCards.children;
	let cardIndex = allCards.length - 1;
	let cardsRemoved = 0;
	let cardsToRemove = 8; //Must be a multiple of 2
	while ((cardsRemoved < cardsToRemove) && (cardIndex >= 0)) {
		let currentCard = allCards[cardIndex];
		if ((currentCard != null) && (currentCard.nodeName == "LI")) {
			photoCards.removeChild(currentCard);
			++cardsRemoved;
		}
		--cardIndex;
	}
}

if (allSelectors.length == 0) {
	allSelectors.push(new AdSelector(".generic-box.ad .deferred-iframe-target", true).addSize(300, 250));
}