window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".choice-parameter-data-holder").forEach((dataHolder) => {
        const { paramName, filterLength } = dataHolder.dataset;
        UnoChoice.renderChoiceParameter(paramName, filterLength);
    });
});
