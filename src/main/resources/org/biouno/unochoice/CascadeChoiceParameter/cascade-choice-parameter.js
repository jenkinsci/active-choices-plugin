if (window.makeStaplerProxy) {
    window.__old__makeStaplerProxy = window.makeStaplerProxy;
    window.makeStaplerProxy = UnoChoice.makeStaplerProxy2;
}

window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".cascade-choice-parameter-data-holder").forEach((dataHolder) => {
        const { name, paramName, randomName, proxyName } = dataHolder.dataset;
        const referencedParameters = dataHolder.dataset.referencedParameters || '';
        const referencedParametersList = referencedParameters.split(",").map((val) => val.trim());
        const filterable = dataHolder.dataset.filterable === "true";
        const filterLength = parseInt(dataHolder.dataset.filterLength);

        UnoChoice.renderCascadeChoiceParameter(`#${paramName}`, filterable, name, randomName, filterLength, paramName, referencedParametersList, window[proxyName]);
    });

    if (window.makeStaplerProxy && window.__old__makeStaplerProxy) {
        window.makeStaplerProxy = window.__old__makeStaplerProxy;
        delete window["__old__makeStaplerProxy"];
    }
});
