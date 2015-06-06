function onAjaxCompleteForSelectionBox() {
    setContextMenuForSelections();
    
    setupFragmentOverlayTrigger($("#selection-box-form\\:selection-box-panel"));
}

function setContextMenuForSelections() {
    var menu = $("#selection-box-context-menu");
    
    $(".each-selected-frg, #selection-box-form\\:selection-box-panel").bind("contextmenu", function(event) {
        var target = $(event.target).closest(".each-selected-frg");
        if (isNaN(parseInt(target.attr("_fid")))) {
            menu.find("#selection-box-form\\:bookmark").show();
            menu.find("#selection-box-form\\:relate").show();
            menu.find("#selection-box-form\\:trash").show();
            menu.find("#selection-box-form\\:select_unselect").hide();
            if (listSelectedFragments())
                showPopup(menu, event);
        }
        else {
            menu.find("#selection-box-form\\:bookmark").hide();
            menu.find("#selection-box-form\\:relate").hide();
            menu.find("#selection-box-form\\:trash").hide();
            menu.find("#selection-box-form\\:select_unselect").show();
            showPopup(menu, event);
        }
            
        menu.data("target-fragment", target);
        event.preventDefault();
    });

    $(document).bind("click", function(event) {
        menu.hide();
    });
}

function listSelectedFragments() {
    var output = "";
    $("#selection-box-form\\:selection-box-panel").find(".each-selected-frg").each(function() {
        var $this = $(this);
        if ($this.hasClass("middle-line"))
            return;
        output += "\n#" + $this.attr("_fid") + "  " + $this.attr("_ft") + "\n";
    });
    return output;
}

function confirmRelatingSelectedFragments() {
    
}

function confirmTrashingSelectedFragments() {
    $("#fragment-group-form\\:ok").click(function() {
        document.forms["fragment-group-form"]["fragment-group-form:ok-trash-fragments"].click();
    });
    
    var mainMsg = MSG.confirm_trashing;
    var subMsg = listSelectedFragments();
    showConfirmDlg(mainMsg, subMsg, "fa-trash", "orangered");
}

function selectOrUnselectFragment() {
    var target = $("#selection-box-context-menu").data("target-fragment");
    if (target.hasClass("middle-line")) {
        // mark as selected
        target.removeClass("middle-line");
    }
    else {
        // mark as unselected
        target.addClass("middle-line");
    }
}
