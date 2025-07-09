if (window.makeStaplerProxy) {
    window.__old__makeStaplerProxy = window.makeStaplerProxy;
    window.makeStaplerProxy = UnoChoice.makeStaplerProxy2;
}

window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".dynamic-reference-parameter-data-holder").forEach((dataHolder) => {
        const { name, paramName, proxyName } = dataHolder.dataset;
        const referencedParameters = dataHolder.dataset.referencedParameters;
        if (referencedParameters === undefined || referencedParameters === null || referencedParameters.length === 0) {
            console.log(`[${name}] - dynamic-reference-parameter.js#querySelectorAll#forEach - No parameters referenced!`);
            return;
        }
        const referencedParametersList = dataHolder.dataset.referencedParameters.split(",").map((val) => val.trim());

        UnoChoice.renderDynamicRenderParameter(`#${paramName}`, name, paramName, referencedParametersList, window[proxyName]);

        // update spinner id
        var rootElmt = document.querySelector(`#${paramName}`);
        if (rootElmt) {
            var divElmt = rootElmt.querySelector("div");
            if (divElmt) {
                var spinnerId = divElmt.id.split("_").pop();
                document.querySelector(`#${paramName}-spinner`).setAttribute("id", spinnerId + "-spinner");
            }
        }
    });

    if (window.makeStaplerProxy && window.__old__makeStaplerProxy) {
        window.makeStaplerProxy = window.__old__makeStaplerProxy;
        delete window["__old__makeStaplerProxy"];
    }
});
