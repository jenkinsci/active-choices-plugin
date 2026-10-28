/*
 * required-validation.js
 *
 * Browser-side enforcement for Active Choices parameters marked as required.
 * Provides an inline error message and disables the form's primary submit button
 * when no value is selected. Server-side validation via hudson.model.Failure is
 * the authoritative gate; this script is a UX convenience only.
 */
(function () {
    "use strict";

    // Set of paramNames that are currently invalid.
    var invalidParams = new Set();

    function getSubmitButton(form) {
        if (!form) return null;
        return (
            form.querySelector('button[name="Submit"]') ||
            form.querySelector(".jenkins-button--primary")
        );
    }

    function setSubmitState(form) {
        var btn = getSubmitButton(form);
        if (!btn) return;
        btn.disabled = invalidParams.size > 0;
    }

    function hasSelection(container) {
        // Check checked checkboxes
        var checked = container.querySelectorAll('input[name="value"]:checked');
        if (checked.length > 0) return true;
        // Check selected options in a select
        var selects = container.querySelectorAll('select[name="value"]');
        for (var i = 0; i < selects.length; i++) {
            if (selects[i].value && selects[i].value.trim() !== "") return true;
        }
        return false;
    }

    function getOrCreateErrorEl(container, label) {
        var id = "ac-required-error-" + container.id;
        var el = document.getElementById(id);
        if (!el) {
            el = document.createElement("div");
            el.id = id;
            el.className = "ac-required-error";
            el.style.cssText = "color:#cc0000;font-size:0.85em;margin-top:4px;display:none;";
            el.textContent = "\u26a0 \u201c" + label + "\u201d is required \u2014 please select at least one option.";
            container.parentNode.insertBefore(el, container.nextSibling);
        }
        return el;
    }

    function validate(paramName, label, form) {
        var container = document.getElementById(paramName);
        if (!container) return;
        var errorEl = getOrCreateErrorEl(container, label);
        if (hasSelection(container)) {
            invalidParams.delete(paramName);
            errorEl.style.display = "none";
        } else {
            invalidParams.add(paramName);
            errorEl.style.display = "block";
        }
        setSubmitState(form);
    }

    function attachToContainer(paramName, label, form) {
        var container = document.getElementById(paramName);
        if (!container) return;

        // Event delegation on the container for interactive changes
        container.addEventListener("change", function () {
            validate(paramName, label, form);
        });

        // MutationObserver to handle cascade re-renders that replace inner DOM
        var observer = new MutationObserver(function () {
            validate(paramName, label, form);
        });
        observer.observe(container, { childList: true, subtree: true });

        // Initial state
        validate(paramName, label, form);
    }

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll(".ac-required-data-holder").forEach(function (holder) {
            var paramName = holder.dataset.paramName;
            var label = holder.dataset.paramLabel || paramName;
            if (!paramName) return;
            var form = holder.closest("form");
            attachToContainer(paramName, label, form);
        });
    });
}());
