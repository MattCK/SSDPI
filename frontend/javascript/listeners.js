"use strict";
$(function () {
    /********************************************************************************************/
    /************************************* Main App *********************************************/
    /********************************************************************************************/
    //Setup the page listeners
    // let orderSearchButton = base.nodeFromID("orderSearchButton");
    // orderSearchButton.addEventListener('click', asr.searchOrders, false);
    // let orderSearchTermInput = base.nodeFromID("orderSearchTerm");
    // orderSearchTermInput.addEventListener('keyup', function(event) {if(event.which == 13){asr.searchOrders();}}, false);
    $("#orderSearchTerm").keyup(function (event) { if (event.which == 13) {
        asr.searchOrders();
    } });
    $("#orderSearchButton").click(asr.searchOrders);
    // let orderSelect = base.nodeFromID("orderSelect");
    // orderSelect.addEventListener('change', asr.displayOrderNotes, false);
    $("#orderSelect").change(asr.displayOrderNotes);
    // let selectAllLineItemsButton = base.nodeFromID("selectAllLineItemsButton");
    // selectAllLineItemsButton.addEventListener('click', asr.selectAllLineItems, false);
    // let unselectAllLineItemsButton = base.nodeFromID("unselectAllLineItemsButton");
    // unselectAllLineItemsButton.addEventListener('click', asr.unselectAllLineItems, false);
    // let lineItemsButton = base.nodeFromID("lineItemsButton");
    // lineItemsButton.addEventListener('click', asr.useSelectedLineItems, false);
    $("#getOrderDataButton").click(asr.requestOrderData);
    $("#selectAllLineItemsButton").click(asr.selectAllLineItems);
    $("#unselectAllLineItemsButton").click(asr.unselectAllLineItems);
    $("#lineItemsButton").click(asr.useSelectedLineItems);
    // let changeBackgroundButton = base.nodeFromID("changeBackgroundButton");
    // changeBackgroundButton.addEventListener('click', function() {base.hide('changeBackgroundButtonDiv'); base.show('uploadBackgroundDiv');}, false);
    // let uploadBackgroundButton = base.nodeFromID("uploadBackgroundButton");
    // uploadBackgroundButton.addEventListener('click', asr.uploadPowerPointBackground, false);
    $("#changeBackgroundButton").click(function () { base.hide('changeBackgroundButtonDiv'); base.show('uploadBackgroundDiv'); });
    $("#uploadBackgroundButton").click(asr.uploadPowerPointBackground);
    // let domainInput = base.nodeFromID("domain");
    // domainInput.addEventListener('keyup', function(event) {if(event.keyCode == 13){asr.getMenu();}}, false);
    // let getMenuButton = base.nodeFromID("getMenuButton");
    // getMenuButton.addEventListener('click', asr.getMenu, false);
    $("#domain").keyup(function (event) { if (event.which == 13) {
        asr.getMenu();
    } });
    $("#getMenuButton").click(asr.getMenu);
    // let addSiteSectionButton = base.nodeFromID("addSiteSectionButton");
    // addSiteSectionButton.addEventListener('click', asr.addMenuSectionRow, false);
    // let addURLButton = base.nodeFromID("addURLButton");
    // addURLButton.addEventListener('click', asr.addURLRow, false);
    //Setup drag and drop tag listeners
    let textFileDropZone = base.nodeFromID('textFileDropZone');
    if (textFileDropZone) {
        textFileDropZone.addEventListener('dragover', tagParser.handleDragOver, false);
        textFileDropZone.addEventListener('drop', tagParser.handleTagTextFileDrop, false);
        textFileDropZone.addEventListener('dragleave', tagParser.handleDragLeave, false);
    }
    // $("#textFileDropZone").on("dragover", tagParser.handleDragOver);
    // $("#textFileDropZone").on("drop", tagParser.handleTagTextFileDrop);
    // $("#textFileDropZone").on("dragleave", tagParser.handleDragLeave);
    let zipFileDropZone = base.nodeFromID('zipFileDropZone');
    if (zipFileDropZone) {
        zipFileDropZone.addEventListener('dragover', tagParser.handleDragOver, false);
        zipFileDropZone.addEventListener('drop', tagParser.handleTagZipFileDrop, false);
        zipFileDropZone.addEventListener('dragleave', tagParser.handleDragLeave, false);
    }
    // let tagTextTextboxButton = base.nodeFromID("tagTextTextboxButton");
    // tagTextTextboxButton.addEventListener('click', tagParser.handleTagTextboxInput, false);
    $("#tagTextTextboxButton").click(tagParser.handleTagTextboxInput);
    // let getTagImagesButton = base.nodeFromID("getTagImagesButton");
    // getTagImagesButton.addEventListener('click', asr.getTagImages, false);
    // let getScreenshotsButton = base.nodeFromID("getScreenshotsButton");
    // getScreenshotsButton.addEventListener('click', asr.requestScreenshots, false);
    // $("#getTagImagesButton").click(asr.getTagImages);
    $("#getScreenshotsButton").click(asr.requestScreenshots);
    $(".contactIssueLink").click(function () { contactForm.reset(); contactForm.selectIssue(); contactFormDialog.open(); });
    $(".contactIdeaLink").click(function () { contactForm.reset(); contactFormDialog.open(); });
    $("#contactIdeaRadio").click(contactForm.selectIdea);
    $("#contactIssueRadio").click(contactForm.selectIssue);
    $("#contactSendButton").click(contactForm.submitForm);
});
