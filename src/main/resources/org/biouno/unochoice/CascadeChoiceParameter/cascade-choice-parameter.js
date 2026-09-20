Behaviour.specify(".cascade-choice-parameter-data-holder", "uno-choice-cascade", 0, function (dataHolder) {
    const { name, paramName, randomName, proxyName } = dataHolder.dataset;
    const referencedParameters = dataHolder.dataset.referencedParameters;
    if (referencedParameters === undefined || referencedParameters === null || referencedParameters.length === 0) {
        console.log(`[${name}] - cascade-choice-parameters.js#querySelectorAll#forEach - No parameters referenced!`);
        return;
    }
    const referencedParametersList = dataHolder.dataset.referencedParameters.split(",").map((val) => val.trim());
    const filterable = dataHolder.dataset.filterable === "true";
    const filterLength = parseInt(dataHolder.dataset.filterLength);

    UnoChoice.renderCascadeChoiceParameter(`#${paramName}`, filterable, name, randomName, filterLength, paramName, referencedParametersList, window[proxyName]);
});

