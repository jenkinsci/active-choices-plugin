/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Ioannis Moutsatsos, Bruno P. Kinoshita
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import Util from './Util.ts';

jQuery3.noConflict();
/**
 * <h2>Uno Choice Javascript module.</h2>
 *
 * <p>This Javascript module is used in Uno-Choice Plug-in, and was created to enable users to have different
 * types of parameters in Jenkins.</p>
 *
 * <p>In Jenkins parameters are used to customize Job variables. However, the range of parameters and their
 * features is limited. Specially in the UI, as for example, elements that are updated reacting to changes in
 * other elements (e.g. city and state combo boxes).</p>
 *
 * <p>This module <strong>depends on JQuery</strong> only.</p>
 *
 * @param jQuery3 jQuery3 global var
 * @author Bruno P. Kinoshita <brunodepaulak@yahoo.com.br>
 * @since 0.20
 */
var UnoChoice = UnoChoice || (jQuery3 => {
    let util = new Util(jQuery3);
    // The final public object
    let instance = {};
    let SEPARATOR = '__LESEP__';
    let cascadeParameters = [];

    // Plug-in classes
    // --- Cascade Parameter
    /**
     * A parameter that references parameters.
     * @param paramName {String} parameter name
     * @param paramElement {HTMLElement} parameter HTML element
     * @param randomName {String} randomName given to the parameter
     * @param proxy Stapler proxy object that references the CascadeChoiceParameter
     */
    class CascadeParameter {
        constructor(paramName, paramElement, randomName, proxy) {
            this.paramName = paramName;
            this.paramElement = paramElement;
            this.randomName = randomName;
            this.proxy = proxy;
            this.referencedParameters = [];
            this.filterElement = null;
        }

        getParameterName() {
            return this.paramName;
        }

        getParameterElement() {
            return this.paramElement;
        }

        getReferencedParameters() {
            return this.referencedParameters;
        }

        getRandomName() {
            return this.randomName;
        }

        getFilterElement() {
            return this.filterElement;
        }

        setFilterElement(e) {
            this.filterElement = e;
        }

        /**
         * Used to create the request string that will update the cascade parameter values. Returns a
         * String, with name=value for each referenced parameter.
         *
         * @return {string} String with name=value for each referenced parameter
         */
        getReferencedParametersAsText() {
            let parameterValues = [];
            // get the parameters' values
            for (let j = 0; j < this.getReferencedParameters().length; j++) {
                let referencedParameter = this.getReferencedParameters()[j];
                let name = referencedParameter.getParameterName();
                let value = getParameterValue(referencedParameter.getParameterElement());
                parameterValues.push(`${name}=${value}`);
            }
            return parameterValues.join(SEPARATOR);
        }

        /**
         * Updates the CascadeParameter object.
         *
         * <p>Once this method gets called, it will call the Java code (using Stapler proxy),
         * that is responsible for updating the referenced parameter values. The Java method receives the value of
         * other referenced parameters.</p>
         *
         * <p>Then, we call the Java code again, now to decide the next values to be displayed. From here, the
         * flow gets split into several branches, one for each HTML element type supported (SELECT, INPUT, UL, etc).
         * Each HTML element gets rendered accordingly and events are triggered.</p>
         *
         * <p>In the last part of the method, before updating other elements, it checks for recursive calls. If
         * this parameter references itself, we need to avoid updating it forever.</p>
         *
         * @param avoidRecursion {boolean} flag to decide whether we want to permit self-reference parameters or not
         */
        async update(avoidRecursion) {
            let parametersString = this.getReferencedParametersAsText(); // gets the array parameters, joined by , (e.g. a,b,c,d)
            // Update the CascadeChoiceParameter Map of parameters
            await this.proxy.doUpdate(parametersString);

            let spinner, rootDiv;
            if (this.getRandomName()) {
                let spinnerId = this.getRandomName().split('_').pop();
                spinner = jQuery3(`div#${spinnerId}-spinner`);
                // Show spinner
                if (spinner) {
                    spinner.show();
                }
                // Disable DIV changes
                rootDiv = jQuery3(`div#${spinnerId}`);
                if (rootDiv) {
                    rootDiv.css('pointer-events', 'none');
                }
            }

            // Now we get the updated choices, after the Groovy script is eval'd using the updated Map of parameters
            // The inner function is called with the response provided by Stapler. Then we update the HTML elements.
            let _self = this; // re-reference this to use within the inner function
            console.log('Calling Java server code to update HTML elements...');
            await this.proxy.getChoicesForUI(t => {
                let data = t.responseObject();
                let newValues = data[0];
                let newKeys = data[1];
                let selectedElements = [];
                let disabledElements = [];
                // filter selected and disabled elements and create a matrix for selection and disabled
                // some elements may have key or values with the suffixes :selected and/or :disabled
                // we want to remove these suffixes
                for (let i = 0; i < newValues.length; i++) {
                    let newValue = String(newValues[i]);
                    if (newValue && (newValue.endsWith(':selected') || newValue.endsWith(':selected:disabled'))) {
                        selectedElements.push(i);
                        newValues[i] = newValues[i].replace(/:selected$/, '').replace(/:selected:disabled$/, ':disabled');
                    }
                    if (newValue && (newValue.endsWith(':disabled') || newValue.endsWith(':disabled:selected'))) {
                        disabledElements.push(i);
                        newValues[i] = newValues[i].replace(/:disabled$/, '').replace(/:disabled:selected$/, ':selected');
                    }
                    let newKey = String(newKeys[i]);
                    if (newKey && typeof newKey === "string" && (newKey.endsWith(':selected') || newKey.endsWith(':selected:disabled'))) {
                        newKeys[i] = newKeys[i].replace(/:selected$/, '').replace(/:selected:disabled$/, ':disabled');
                    }
                    if (newKey && typeof newKey === "string" && (newKey.endsWith(':disabled') || newKey.endsWith(':disabled:selected'))) {
                        newKeys[i] = newKeys[i].replace(/:disabled$/, '').replace(/:disabled:selected$/, ':selected');
                    }
                }
                if (_self.getFilterElement()) {
                    console.log('Updating values in filter array');
                }
                // FIXME
                // http://stackoverflow.com/questions/6364748/change-the-options-array-of-a-select-list
                let parameterElement = _self.getParameterElement();
                if (parameterElement.tagName === 'SELECT') { // handle SELECT's
                    while (parameterElement.options.length > 0) {
                        parameterElement.remove(parameterElement.options.length - 1);
                    }
                    for (let i = 0; i < newValues.length; i++) {
                        let opt = document.createElement('option');
                        let value = newKeys[i];
                        let entry = newValues[i];
                        if (!entry instanceof String) {
                            opt.text = JSON.stringify(entry);
                            opt.value = JSON.stringify(value); //JSON.stringify(entry);
                        } else {
                            opt.text = entry;
                            opt.value = value;
                        }
                        if (selectedElements.indexOf(i) >= 0) {
                            opt.setAttribute('selected', 'selected');
                        }
                        if (disabledElements.indexOf(i) >= 0) {
                            opt.setAttribute('disabled', 'disabled');
                        }
                        parameterElement.add(opt, null);
                    }
                    if (parameterElement.getAttribute('multiple') === 'multiple') {
                        parameterElement.setAttribute('size', `${newValues.length > 10 ? 10 : newValues.length}px`);
                    }
                    // Update the values for the filtering
                    let originalArray = [];
                    for (let i = 0; i < _self.getParameterElement().options.length; ++i) {
                        originalArray.push(_self.getParameterElement().options[i]);
                    }
                    if (_self.getFilterElement()) {
                        _self.getFilterElement().setOriginalArray(originalArray);
                    }
                } else if (parameterElement.tagName === 'DIV' || parameterElement.tagName === 'SPAN') {
                    if (parameterElement.children.length > 0 && (parameterElement.children[0].tagName === 'DIV' || parameterElement.children[0].tagName === 'SPAN')) {
                        let tbody = parameterElement.children[0];
                        jQuery3(tbody).empty();
                        let originalArray = [];
                        // Check whether it is a radio or checkbox element
                        if (parameterElement.className === 'dynamic_checkbox') {
                            for (let i = 0; i < newValues.length; i++) {
                                let entry = newValues[i];
                                let key = newKeys[i];
                                let idValue = `ecp_${_self.getRandomName()}_${i}`;
                                idValue = idValue.replace(' ', '_');
                                // <INPUT>
                                let input = util.makeCheckbox(key, selectedElements.indexOf(i) >= 0, disabledElements.indexOf(i) >= 0);
                                if (!entry instanceof String) {
                                    input.setAttribute("title", JSON.stringify(entry));
                                    input.setAttribute("alt", JSON.stringify(entry));
                                } else {
                                    input.setAttribute("title", entry);
                                    input.setAttribute("alt", entry);
                                }
                                // <LABEL>
                                let label = util.makeLabel(!entry instanceof String ? JSON.stringify(entry) : entry, undefined);
                                originalArray.push(input);
                                // Put everything together
                                let td = util.makeTd([input, label]);
                                let tr = util.makeTr(idValue)
                                tr.appendChild(td);
                                tbody.appendChild(tr);
                            }
                            // Update the values for the filtering
                            if (_self.getFilterElement()) {
                                _self.getFilterElement().setOriginalArray(originalArray);
                            }
                        } else { // radio
                            for (let i = 0; i < newValues.length; i++) {
                                let entry = newValues[i];
                                let key = newKeys[i];
                                let idValue = `ecp_${_self.getRandomName()}_${i}`;
                                idValue = idValue.replace(' ', '_');
                                // <INPUT>
                                let input = util.makeRadio(key, _self.getParameterName(), selectedElements.indexOf(i) >= 0, disabledElements.indexOf(i) >= 0);
                                input.setAttribute('onchange', `UnoChoice.fakeSelectRadioButton("${_self.getParameterName()}", "${idValue}")`);
                                input.setAttribute('otherId', idValue);
                                if (!entry instanceof String) {
                                    input.setAttribute('alt', JSON.stringify(entry));
                                } else {
                                    input.setAttribute('alt', entry);
                                }
                                // <LABEL>
                                let label = util.makeLabel(!entry instanceof String ? JSON.stringify(entry) : entry, undefined);
                                // <HIDDEN>
                                let hiddenValue = util.makeHidden(idValue, key, selectedElements.indexOf(i) >= 0 ? 'value' : '', key, _self.getParameterName(), entry instanceof String ? entry : JSON.stringify(entry));
                                originalArray.push(input);
                                let td = util.makeTd([input, label, hiddenValue]);
                                let tr = util.makeTr(undefined)
                                tr.appendChild(td);
                                tbody.appendChild(tr);
                                let endTr = document.createElement('div');
                                endTr.setAttribute('style', 'display: none');
                                endTr.setAttribute('class', 'radio-block-end');
                                tbody.appendChild(endTr);
                            }
                            // Update the values for the filtering
                            if (_self.getFilterElement()) {
                                _self.getFilterElement().setOriginalArray(originalArray);
                            }
                        } // if (oldSel.className === 'dynamic_checkbox')
                        /*
                         * This height is equivalent to setting the number of rows displayed in a select/multiple
                         */
                        parameterElement.style.height = newValues.length > 10 ? '230px' : 'auto';
                    } // if (parameterElement.children.length > 0 && parameterElement.children[0].tagName === 'DIV') {
                } // if (parameterElement.tagName === 'SELECT') { // } else if (parameterElement.tagName === 'DIV') {
            });
            // propagate change
            // console.log('Propagating change event from ' + this.getParameterName());
            // let e1 = $.Event('change', {parameterName: this.getParameterName()});
            // jQuery3(this.getParameterElement()).trigger(e1);
            if (!avoidRecursion) {
                if (cascadeParameters && cascadeParameters.length > 0) {
                    for (let i = 0; i < cascadeParameters.length; i++) {
                        let other = cascadeParameters[i];
                        if (this.referencesMe(other)) {
                            console.log(`Updating ${other.getParameterName()} from ${this.getParameterName()}`);
                            await other.update(true);
                        }
                    }
                }
            } else {
                console.log('Avoiding infinite loop due to recursion!');
            }
            // Hide spinner
            if (spinner) {
                spinner.hide();
            }
            // Activate DIV changes
            if (rootDiv) {
                rootDiv.css('pointer-events', 'auto');
            }
        }

        /**
         * Returns <code>true</code> iff the given parameter is not null, and one of its
         * reference parameters is the same parameter as <code>this</code>. In other words,
         * it returns whether the given parameter references this parameter.
         *
         * @since 0.22
         * @param cascadeParameter {CascadeParameter} a given parameter
         * @return {boolean} <code>true</code> iff the given parameter references this parameter
         */
        referencesMe(cascadeParameter) {
            if (!cascadeParameter || !cascadeParameter.getReferencedParameters() || cascadeParameter.getReferencedParameters().length === 0) return false;
            for (let i = 0; i < cascadeParameter.getReferencedParameters().length; i++) {
                let referencedParameter = cascadeParameter.getReferencedParameters()[i];
                if (referencedParameter.getParameterName() === this.getParameterName()) return true;
            }
            return false;
        }
    }

    // --- Referenced Parameter
    /**
     * <p>A parameter that is referenced by other parameters. Stores a list of cascade parameters, that reference this
     * parameter.</p>
     *
     * <p>Whenever this parameter changes, it will notify each cascade parameter.</p>
     *
     * @param paramName {string} parameter name
     * @param paramElement {HTMLElement} parameter HTML element
     * @param cascadeParameter {CascadeParameter}
     */
    class ReferencedParameter {
        constructor(paramName, paramElement, cascadeParameter) {
            this.paramName = paramName;
            this.paramElement = paramElement;
            this.cascadeParameter = cascadeParameter;
            console.log(`ReferencedParameter: ${paramName}`);
            // Add event listener
            let _self = this;
            jQuery3(this.paramElement).on('change', e => {
                if (e.parameterName === _self.paramName) {
                    console.log('Skipping self reference to avoid infinite loop!');
                    e.stopImmediatePropagation();
                } else {
                    console.log(`Cascading changes from parameter ${_self.paramName}...`);
                    //_self.cascadeParameter.loading(true);
                    jQuery3(".behavior-loading").show();
                    // start updating in separate async function so browser will be able to repaint and show 'loading' animation , see JENKINS-34487
                    setTimeout(async () => {
                        await _self.cascadeParameter.update(false);
                        for (let i = 0; i < cascadeParameters.length; i++) {
                            let other = cascadeParameters[i];
                            if (_self.referencesMe(other)) {
                                console.log(`Updating ${other.getParameterName()} from ${this.getParameterName()}`);
                                await other.update(true);
                            }
                        }
                        jQuery3(".behavior-loading").hide();
                    }, 0);
                }
            });
            cascadeParameter.getReferencedParameters().push(this);
        }

        getParameterName() {
            return this.paramName;
        }

        getParameterElement() {
            return this.paramElement;
        }

        getCascadeParameter() {
            return this.cascadeParameter;
        }

        referencesMe(cascadeParameter) {
            if (!cascadeParameter || !cascadeParameter.getReferencedParameters() || cascadeParameter.getReferencedParameters().length === 0) return false;
            for (let i = 0; i < cascadeParameter.getReferencedParameters().length; i++) {
                let referencedParameter = cascadeParameter.getReferencedParameters()[i];
                if (referencedParameter.getParameterName() === this.getParameterName()) return true;
            }
            return false;
        }
    }

    // --- Dynamic Reference Parameter
    /**
     * A parameter that is used only as a render mechanism for other referenced parameters.
     *
     * @param paramName {string} parameter name
     * @param paramElement {HTMLElement} parameter HTML element
     * @param proxy Stapler proxy object that references the CascadeChoiceParameter
     */
    class DynamicReferenceParameter extends CascadeParameter {
        constructor(paramName, paramElement, proxy) {
            super(paramName, paramElement, null, proxy);
        }

        /**
         * <p>Updates the DynamicReferenceParameter object. Debug information goes into the browser console.</p>
         *
         * <p>Once this method gets called, it will call the Java code (using Stapler proxy),
         * that is responsible for updating the referenced parameter values. The Java method receives the value of
         * other referenced parameters.</p>
         *
         * <p>Then, we call the Java code again, now to decide the next values to be displayed. From here, the
         * flow gets split into several branches, one for each HTML element type supported (SELECT, INPUT, UL, etc).
         * Each HTML element gets rendered accordingly and events are triggered.</p>
         *
         * <p>In the last part of the method, before updating other elements, it checks for recursive calls. If
         * this parameter references itself, we need to avoid updating it forever.</p>
         *
         * @param avoidRecursion {boolean} flag to decide whether we want to permit self-reference parameters or not
         */
        async update(avoidRecursion) {
            let parametersString = this.getReferencedParametersAsText(); // gets the array parameters, joined by , (e.g. a,b,c,d)
            // Update the Map of parameters
            await this.proxy.doUpdate(parametersString);
            let parameterElement = this.getParameterElement();

            let spinner, rootDiv;
            if (parameterElement.id) {
                let spinnerId = parameterElement.id.split('_').pop();
                spinner = jQuery3(`div#${spinnerId}-spinner`);
                // Show spinner
                if (spinner) {
                    spinner.show();
                }
                rootDiv = jQuery3(`div#${spinnerId}`);
                // Disable DIV changes
                if (rootDiv) {
                    rootDiv.css('pointer-events', 'none');
                }
            }
            // Here depending on the HTML element we might need to call a method to return a Map of elements,
            // or maybe call a string to put as value in a INPUT.
            if (parameterElement.tagName === 'OL') { // handle OL's
                console.log('Calling Java server code to update HTML elements...');
                await this.proxy.getChoicesForUI(t => {
                    jQuery3(parameterElement).empty(); // remove all children elements
                    const data = t.responseObject();
                    let newValues = data[0];
                    // let newKeys = data[1];
                    for (let i = 0; i < newValues.length; ++i) {
                        let li = document.createElement('li');
                        li.innerHTML = newValues[i];
                        parameterElement.appendChild(li); // append new elements
                    }
                });
            } else if (parameterElement.tagName === 'UL') { // handle OL's
                jQuery3(parameterElement).empty(); // remove all children elements
                console.log('Calling Java server code to update HTML elements...');
                await this.proxy.getChoicesForUI(t => {
                    jQuery3(parameterElement).empty(); // remove all children elements
                    const data = t.responseObject();
                    let newValues = data[0];
                    // let newKeys = data[1];
                    for (let i = 0; i < newValues.length; ++i) {
                        let li = document.createElement('li');
                        li.innerHTML = newValues[i];
                        parameterElement.appendChild(li); // append new elements
                    }
                });
            } else if (parameterElement.id.indexOf('inputElement_') > -1) { // handle input text boxes
                await this.proxy.getChoicesAsStringForUI(t => {
                    parameterElement.value = t.responseObject();
                });
            } else if (parameterElement.id.indexOf('formattedHtml_') > -1) { // handle formatted HTML
                await this.proxy.getChoicesAsStringForUI(t => {
                    parameterElement.innerHTML = t.responseObject();
                });
            }
            if (!avoidRecursion) {
                if (cascadeParameters && cascadeParameters.length > 0) {
                    for (let i = 0; i < cascadeParameters.length; i++) {
                        let other = cascadeParameters[i];
                        if (this.referencesMe(other)) {
                            console.log(`Updating ${other.getParameterName()} from ${this.getParameterName()}`);
                            await other.update(true);
                        }
                    }
                }
            } else {
                console.log('Avoiding infinite loop due to recursion!');
            }
            // Hide spinner
            if (spinner) {
                spinner.hide();
            }
            // Activate DIV changes
            if (rootDiv) {
                rootDiv.css('pointer-events', 'auto');
            }
        }
    }

    // --- Filter Element
    /**
     * An element that acts as filter for other elements.
     *
     * @param paramElement {HTMLElement} HTML element being filtered
     * @param filterElement {HTMLElement} HTML element where the user enter the filter
     * @param filterLength {number} filter length
     */
    class FilterElement {
        constructor(paramElement, filterElement, filterLength) {
            this.paramElement = paramElement;
            this.filterElement = filterElement;
            this.filterLength = filterLength;
            this.originalArray = [];
            // push existing values into originalArray array
            if (this.paramElement.tagName === 'SELECT') { // handle SELECTS
                let options = jQuery3(paramElement).children().toArray();
                for (let i = 0; i < options.length; ++i) {
                    this.originalArray.push(options[i]);
                }
            } else if (paramElement.tagName === 'DIV' || paramElement.tagName === 'SPAN') { // handle CHECKBOXES
                if (jQuery3(paramElement).children().length > 0 && (paramElement.children[0].tagName === 'DIV' || paramElement.children[0].tagName === 'SPAN')) {
                    let tbody = paramElement.children[0];
                    let trs = jQuery3(tbody).find('div');
                    for (let i = 0; i < trs.length; ++i) {
                        let tds = jQuery3(trs[i]).find('div');
                        let inputs = jQuery3(tds[0]).find('input');
                        let input = inputs[0];
                        this.originalArray.push(input);
                    }
                } // if (jQuery3(paramElement).children().length > 0 && paramElement.children[0].tagName === 'DIV') {
            }
            this.initEventHandler();
        }

        getParameterElement() {
            return this.paramElement;
        }

        getFilterElement() {
            return this.filterElement;
        }

        getOriginalArray = function () {
            return this.originalArray;
        }

        getFilterLength() {
            return this.filterLength;
        }

        setOriginalArray(originalArray) {
            this.originalArray = originalArray;
            this.clearFilterElement();
        }

        clearFilterElement() {
            this.getFilterElement().value = '';
        }

        /**
         * Initiates an event listener for Key Up events. Depending on the element type it will interpret the filter, and
         * the filtered element, to update its values.
         */
        initEventHandler() {
            let _self = this;
            jQuery3(_self.filterElement).keyup(e => {
                //let filterElement = e.target;
                let filterElement = _self.getFilterElement();
                let filteredElement = _self.getParameterElement();
                let text = filterElement.value.toLowerCase();
                if (text.length !== 0 && text.length < _self.getFilterLength()) {
                    //console.log("Filter pattern too short: [" + text.length + " < " + _self.getFilterLength() + "]");
                    return;
                }
                let options = _self.originalArray;
                let newOptions = Array();
                for (let i = 0; i < options.length; i++) {
                    if (typeof options[i] !== 'undefined' && options[i].tagName === 'INPUT') {
                        if (options[i].getAttribute('alt') && options[i].getAttribute('alt') !== options[i].value) {
                            if (options[i].getAttribute('alt').toLowerCase().match(text)) {
                                newOptions.push(options[i]);
                            }
                        } else {
                            if (options[i].value.toLowerCase().match(text)) {
                                newOptions.push(options[i]);
                            }
                        }
                    } else {
                        if (typeof options[i] !== 'undefined' && options[i].innerHTML.toLowerCase().match(text)) {
                            newOptions.push(options[i]);
                        }
                    }
                }
                let tagName = filteredElement.tagName;

                if (tagName === 'SELECT') { // handle SELECT's
                    jQuery3(filteredElement).children().remove();
                    for (let i = 0; i < newOptions.length; ++i) {
                        let opt = document.createElement('option');
                        opt.value = newOptions[i].value;
                        opt.innerHTML = newOptions[i].innerHTML;
                        jQuery3(filteredElement).append(opt);
                    }
                } else if (tagName === 'DIV' || tagName === 'SPAN') { // handle CHECKBOXES, RADIOBOXES and other elements (Jenkins renders them as tables)
                    if (jQuery3(filteredElement).children().length > 0 && (jQuery3(filteredElement).children()[0].tagName === 'DIV' || jQuery3(filteredElement).children()[0].tagName === 'SPAN')) {
                        let tbody = filteredElement.children[0];
                        jQuery3(tbody).empty();
                        if (filteredElement.className === 'dynamic_checkbox') {
                            for (let i = 0; i < newOptions.length; i++) {
                                let entry = newOptions[i];
                                let idValue = `ecp_${e.target.randomName}_${i}`;
                                idValue = idValue.replace(' ', '_');

                                let input = entry instanceof String ? util.makeCheckbox(entry, undefined, undefined) : entry.tagName === 'INPUT' ? entry : util.makeRadio(JSON.stringify(entry.value), 'value', undefined, undefined);

                                // LABEL
                                let label = (entry instanceof String || entry.tagName === 'INPUT') ? util.makeLabel(entry.getAttribute('title'), entry.getAttribute('title')) : util.makeLabel(input, undefined);

                                // Put everything together
                                let td = util.makeTd([input, label]);
                                let tr = util.makeTr(idValue)
                                tr.appendChild(td);
                                tbody.appendChild(tr);
                            }
                        } else {
                            for (let i = 0; i < newOptions.length; i++) {
                                let entry = newOptions[i];
                                let idValue = '';
                                if (!(entry instanceof String)) {
                                    if (entry.tagName === 'INPUT') {
                                        idValue = `ecp_${entry.getAttribute('name')}_${i}`;
                                    }
                                } else {
                                    idValue = `ecp_${entry}_${i}`;
                                }
                                idValue = idValue.replace(' ', '_');
                                // INPUTs
                                let input = document.createElement('input');
                                input = entry;
                                input.checked = false;
                                let jsonInput = util.makeHidden(input.getAttribute('otherid'), input.getAttribute('json'), '', input.getAttribute('value'), input.getAttribute('name'), input.getAttribute('alt'));

                                let label = util.makeLabel(input.getAttribute('alt'), undefined);
                                // Put everything together
                                let td = util.makeTd([input, label, jsonInput]);
                                let tr = util.makeTr(idValue)
                                tr.appendChild(td);
                                tbody.appendChild(tr);
                            }
                        }
                    } // if (jQuery3(filteredElement).children().length > 0 && jQuery3(filteredElement).children()[0].tagName === 'DIV') {
                } // if (tagName === 'SELECT') { // } else if (tagName === 'DIV') {
                // Propagate the changes made by the filter
                console.log('Propagating change event after filtering');
                let e1 = $.Event('change', {parameterName: 'Filter Element Event'});
                jQuery3(filteredElement).trigger(e1);
            });
        }

    }


    // HTML utility methods
    /**
     * <p>Fake selects a radio button.</p>
     *
     * <p>In Jenkins, parameters in general have two main HTML elements. One which name is name with the value as the
     * parameter name. And the other which name is value and with the value as the parameter value. For example:</p>
     *
     * <code>
     * &lt;div name='name' value='parameter1'&gt;
     * &lt;div name='value' value='Sao Paulo'&gt;
     * </code>
     *
     * <p>This code ensures that only one radio button, in a radio group, contains the name value. Avoiding several
     * values to be submitted.</p>
     *
     * @param clazzName {string} HTML element class name
     * @param id {string} HTML element ID
     *
     * @see issue #21 in GitHub - github.com/biouno/uno-choice-plugin/issues
     */
    function fakeSelectRadioButton(clazzName, id) {
        let element = jQuery3(`#${id}`).get(0);
        // deselect all radios with the class=clazzName
        let radios = jQuery3(`input[class="${clazzName}"]`);
        radios.each(function (index) {
            jQuery3(this).attr('name', '');
        });
        // select the radio with the id=id
        let parent = element.parentNode;
        let children = parent.childNodes;
        for (let i = 0; i < children.length; i++) {
            let child = children[i];
            if (child.className === clazzName) {
                child.name = 'value';
            }
        }
    }

    /**
     * <p>Gets the value of a HTML element to use it as value in a parameter in Jenkins.</p>
     *
     * <p>For a HTML element which name is 'value', we use the {@link #getElementValue()} method to retrieve it.</p>
     *
     * <p>For a DIV, we look for children elements with the name equal to 'value'.</p>
     *
     * <p>For a input with type equal file, we look for files to use as value.</p>
     *
     * <p>When there are multiple elements as return value, we append all the values to an Array and return its
     * value as string (i.e. toString()).</p>
     *
     * @param htmlParameter {HTMLElement} HTML element
     * @return {string} the value of the HTML element used as parameter value in Jenkins, as a string
     */
    function getParameterValue(htmlParameter) {
        let e = jQuery3(htmlParameter);
        let value = '';

        if (e.is('select')) {
            if (Array.isArray(e.val())) {
                // Multi Select
                value = e.val().join(', ')
            } else {
                // Single Select
                value = e.val()
            }
        } else if (e.is('div')) {
            // Check Boxes
            if (e.hasClass('dynamic_checkbox')) {
                value = e.find("input[type='checkbox']:checked").map(function () {
                    return jQuery(this).val()
                }).get().join(', ')
            } else if (e.find("div[class='radio-block-end']").length > 0){
                // Radio Button
                value = e.find("input[type='radio']:checked").map(function () {
                    return jQuery(this).val()
                }).get().join(', ')
            } else {
                // Formatted HTML / Formatted hidden HTML
                value = e.text()
            }
        } else if (e.is('input')){
            value = e.val()
        } else if (e.find("div[class='dynamic_checkbox']").length > 0) {
            value = e.find("input[type='checkbox']:checked").map(function () {
                return jQuery(this).val()
            }).get().join(', ')
        } else if (e.is('ul, ol')) {
            // Numbered List / Bullet items list
            let options = []
            e.find('li').each(function () {
                options.push(jQuery(this).text())
            })
            value = options.join(', ');
        } else if (e.find('[type="text"]') > 0) {
            // Input text box
            let textElement = e.find('[type="text"]');
            value = textElement.val();
        }


        // if (e.attr('name') === 'value') {
        //     value = util.getElementValue(e);
        // } else if (e.attr('tagName') === 'INPUT') {
        //     value = e.val();
        // } else if (e.prop('tagName') === 'DIV' || e.prop('tagName') === 'SPAN') {
        //     let subElements = e.find('[name="value"]');
        //     let textSubElement = e.find('[type="text"]');
        //     if (subElements && subElements.length > 0) {
        //         let valueBuffer = Array();
        //         subElements.each(function () {
        //             let tempValue = util.getElementValue(jQuery3(this));
        //             if (tempValue) valueBuffer.push(tempValue);
        //         });
        //         value = valueBuffer.toString();
        //     } else if (textSubElement && textSubElement.length > 0) {
        //         value = textSubElement.text();
        //     } else {
        //         value = e.text();
        //     }
        // } else if (e.attr('type') === 'file') {
        //     let filesList = e.get(0).files;
        //     if (filesList && filesList.length > 0) {
        //         let firstFile = filesList[0]; // ignoring other files... but we could use it...
        //         value = firstFile.name;
        //     }
        // } else if (e.prop('tagName') === 'INPUT' && !['', 'name'].includes(e.attr('name'))) {
        //     value = util.getElementValue(e);
        // }
        return value;
    }

    // Hacks in Jenkins core
    /**
     * <p>This function is the same as makeStaplerProxy available in Jenkins core, but executes calls
     * <strong>synchronously</strong>. Since many parameters must be filled only after other parameters have been
     * updated, calling Jenkins methods asynchronously causes several unpredictable errors.</p>
     *
     * <p>JENKINS-71909: Stapler had to be updated when Prototype and jQuery dependencies
     * were removed from Jenkins. This means that we also had to update this function to
     * match what was done there - thanks asc3ns10n (GH).</p>
     *
     * @param url {string} The URL
     * @param staplerCrumb {string} The crumb
     * @param methods {Array<string>} The methods
     */
    function makeStaplerProxy2(url, staplerCrumb, methods) {
        if (url.substring(url.length - 1) !== '/') url += '/';
        let proxy = {};
        let stringify;
        if (Object.toJSON) // needs to use Prototype.js if it's present. See commit comment for discussion
            stringify = Object.toJSON;  // from prototype
        else if (typeof (JSON) == "object" && JSON.stringify) stringify = JSON.stringify; // standard
        let genMethod = methodName => {
            proxy[methodName] = async function () {
                let args = arguments;
                // the final argument can be a callback that receives the return value
                let callback = (() => {
                    if (args.length === 0) return null;
                    let tail = args[args.length - 1];
                    return (typeof (tail) == 'function') ? tail : null;
                })();
                // 'arguments' is not an array, so we convert it into an array
                let a = [];
                for (let i = 0; i < args.length - (callback != null ? 1 : 0); i++) a.push(args[i]);
                let headers = {
                    'Content-Type': 'application/x-stapler-method-invocation;charset=UTF-8', 'Crumb': staplerCrumb,
                }
                // If running in Jenkins, add Jenkins-Crumb header.
                if (typeof crumb !== 'undefined') {
                    headers = crumb.wrap(headers);
                }
                // Active-Choices: this is the main difference to Jenkins' proxy;
                // we block the call so that each parameter is resolved in-order.
                // Not optimal, but without reactivity in Jenkins, it is hard to
                // design a model where async code works and elements are rendered
                // correctly -- we tried, and failed big-time:
                // https://github.com/jenkinsci/active-choices-plugin/pull/79
                // If you'd like this, we need to have control on how parameters
                // are rendered, and have a proper reactivity system that control
                // what is rendered when, and properly chain certain actions (e.g.
                // a dependant parameter is only rendered after its parent/referenced
                // parameter).
                await fetch(url + methodName, {
                    method: 'POST', headers: headers, body: stringify(a),
                })
                        .then(function (response) {
                            if (response.ok) {
                                const t = {
                                    status: response.status, statusText: response.statusText,
                                };
                                if (response.headers.has('content-type') && response.headers.get('content-type').startsWith('application/json')) {
                                    response.json().then(function (responseObject) {
                                        t.responseObject = function () {
                                            return responseObject;
                                        };
                                        t.responseJSON = responseObject;
                                        if (callback != null) {
                                            callback(t);
                                        }
                                    });
                                } else {
                                    response.text().then(function (responseText) {
                                        t.responseText = responseText;
                                        if (callback != null) {
                                            callback(t);
                                        }
                                    });
                                }
                            }
                        })
            }
        };
        for (let mi = 0; mi < methods.length; mi++) {
            genMethod(methods[mi]);
        }
        return proxy;
    }

    function renderChoiceParameter(paramName, filterLength) {
        let parentDiv = jQuery3(`#${paramName}`);
        let parameterHtmlElement = parentDiv.find('DIV');
        if (!parameterHtmlElement || parameterHtmlElement.length === 0) {
            console.log('Could not find element by name, perhaps it is a DIV?');
            parameterHtmlElement = parentDiv.find('*[name="value"]');
        }
        if (parameterHtmlElement && parameterHtmlElement.get(0)) {
            let filterHtmlElement = parentDiv.find('.uno_choice_filter');
            if (filterHtmlElement && filterHtmlElement.get(0)) {
                parameterHtmlElement.filterElement = new UnoChoice.FilterElement(parameterHtmlElement.get(0), filterHtmlElement.get(0), filterLength); // TBD: not very elegant
            } else {
                console.log('Filter error: Missing filter element!');
            }
        } else {
            console.log('Filter error: Missing parameter element!');
        }
    }


    /**
     * Renders a cascade choice parameter by dynamically binding the parameter to its referenced
     * parameters and applying filters if necessary.
     *
     * @async
     * @function renderCascadeChoiceParameter
     * @param {HTMLElement} parentDivRef - The parent element reference containing the parameter element.
     * @param {boolean} filterable - Determines whether this parameter supports filtering.
     * @param {string} name - The name of the cascade choice parameter.
     * @param {string} randomName - A randomly generated name for parameter identification.
     * @param {number} filterLength - The length of the filter applied to this parameter.
     * @param {string} paramName - The parameter name used for identification in the UI.
     * @param {string[]} referencedParameters - The set of parameters referenced by this item.
     * @param {object} cascadeChoiceParameter - A JavaScript object representing the cascade choice parameter.
     *
     * @description
     * Finds and wires up the cascade parameter to its referenced parameters. If the parameter supports
     * filtering, it adds a filter element. The function ensures the parameter relationships and filters
     * are correctly configured and updates the parameter state.
     *
     * Logs appropriate warnings if parameter elements or filter elements are missing in the DOM.
     */
    async function renderCascadeChoiceParameter(parentDivRef, filterable, name, randomName, filterLength, paramName, referencedParameters, cascadeChoiceParameter) {
        // find the cascade parameter element
        let parentDiv = jQuery3(parentDivRef);
        let parameterHtmlElement = parentDiv.find('DIV');
        if (!parameterHtmlElement || parameterHtmlElement.length === 0) {
            console.log('Could not find element by name, perhaps it is a DIV?');
            parameterHtmlElement = parentDiv.find('*[name="value"]');
        }
        if (parameterHtmlElement && parameterHtmlElement.get(0)) {
            let cascadeParameter = new UnoChoice.CascadeParameter(name, parameterHtmlElement.get(0), randomName, cascadeChoiceParameter);
            UnoChoice.cascadeParameters.push(cascadeParameter);
            // filter
            if (filterable) {
                let filterHtmlElement = parentDiv.find('.uno_choice_filter');
                if (filterHtmlElement && filterHtmlElement.get(0)) {
                    let filterElement = new UnoChoice.FilterElement(parameterHtmlElement.get(0), filterHtmlElement.get(0), filterLength);
                    cascadeParameter.setFilterElement(filterElement);
                } else {
                    console.log('Filter error: Missing filter element!');
                }
            }
            for (let i = 0; i < referencedParameters.length; ++i) {
                let parameterElement = null;
                // FIXME: review the block below
                let divs = jQuery3('div[name="parameter"]');
                for (let j = 0; j < divs.length; j++) {
                    let div = divs[j];
                    let hiddenNames = jQuery3(div).find('input[name="name"]');
                    if (hiddenNames[0].value === referencedParameters[i]) {
                        let children = div.children;
                        for (let k = 0; k < children.length; ++k) {
                            let child = children[k];
                            if (child.getAttribute('name') === 'value') {
                                parameterElement = child;
                                break;
                            } else if (child.tagName === 'DIV' || child.tagName === 'SPAN') {
                                let subValues = jQuery3(child).find('input[name="value"]');
                                if (subValues && subValues.get(0)) {
                                    parameterElement = child;
                                    break;
                                } else {
                                    parameterElement = child;
                                    break;
                                }
                            } else if (child.getAttribute('type') === 'file') {
                                parameterElement = child;
                                break;
                            } else if (child.tagName === 'INPUT' && child.readOnly && child.disabled && child.type === 'text' && child.id.startsWith('inputElement_choice-parameter')) {
                                parameterElement = child;
                                break;
                            } else if (['OL', 'UL'].includes(child.tagName)) {
                                // Numbered List / Bullet items list
                                parameterElement = child;
                                break;
                            }
                        }
                    }
                }

                let rp = new UnoChoice.ReferencedParameter(referencedParameters[i], parameterElement, cascadeParameter);
                if (cascadeParameters && cascadeParameters.length > 0) {
                    for (let i = 0; i < cascadeParameters.length; i++) {
                        let other = cascadeParameters[i];
                        if (rp.referencesMe(other)) {
                            console.log(`Updating ${other.getParameterName()} from ${rp.getParameterName()}`);
                            await other.update(true);
                        }
                    }
                }
            }

            // call update methods in Java passing the HTML values
            // console.log('Updating cascade of parameter [', name, '] ...');
            // await cascadeParameter.update(false);
        } else {
            console.log('Parameter error: Missing parameter [', paramName, '] HTML element!');
        }
    }

    async function renderDynamicRenderParameter(parentDivRef, name, paramName, referencedParameters, dynamicReferenceParameter) {
        // find the cascade parameter element
        let parentDiv = jQuery3(parentDivRef);
        // if the parameter class has been set to hidden, then we hide it now
        if (parentDiv.get(0).getAttribute('class') === 'hidden_uno_choice_parameter') {
            let parentTbody = jQuery3(parentDiv.get(0)).parents('tbody');
            // FIXME: temporary fix to support both TABLE and DIV in the Jenkins UI
            //        remove after most users have migrated to newer versions with DIVs
            if (!parentTbody || parentTbody.length === 0) {
                parentTbody = jQuery3(parentDiv.get(0)).parents('div > div.tr');
            }
            if (parentTbody && parentTbody.length > 0) {
                jQuery3(parentTbody.get(0)).attr('style', 'visibility:hidden;position:absolute;');
            }
        }
        let parameterHtmlElement = null;
        for (let i = 0; i < parentDiv.children().length; i++) {
            let child = parentDiv.children()[i];
            if (child.getAttribute('name') === 'value' || child.id.indexOf('ecp_') > -1) {
                parameterHtmlElement = jQuery3(child);
                break;
            }
            if (child.id.indexOf('inputElement_') > -1) {
                parameterHtmlElement = jQuery3(child);
                break;
            }
            if (child.id.indexOf('formattedHtml_') > -1) {
                parameterHtmlElement = jQuery3(child);
                break;
            }
            if (child.id.indexOf('imageGallery_') > -1) {
                parameterHtmlElement = jQuery3(child);
                break;
            }
        }
        if (parameterHtmlElement && parameterHtmlElement.get(0)) {
            let dynamicParameter = new UnoChoice.DynamicReferenceParameter(name, parameterHtmlElement.get(0), dynamicReferenceParameter);
            UnoChoice.cascadeParameters.push(dynamicParameter); // TODO review whether it is right or not to add a dynamic parameter here
            for (let i = 0; i < referencedParameters.length; ++i) {
                let parameterElement = null;
                // FIXME: review the block below
                let divs = jQuery3('div[name="parameter"]');
                for (let j = 0; j < divs.length; j++) {
                    let div = divs[j];
                    let hiddenNames = jQuery3(div).find('input[name="name"]');
                    if (hiddenNames[0].value === referencedParameters[i]) {
                        let children = div.children;
                        for (let k = 0; k < children.length; ++k) {
                            let child = children[k];
                            if (child.getAttribute('name') === 'value') {
                                parameterElement = child;
                                break;
                            } else if (child.tagName === 'DIV' || child.tagName === 'SPAN') {
                                let subValues = jQuery3(child).find('input[name="value"]');
                                if (subValues && subValues.get(0)) {
                                    parameterElement = child;
                                    break;
                                } else {
                                    parameterElement = child;
                                    break;
                                }
                            } else if (child.getAttribute('type') === 'file') {
                                parameterElement = child;
                                break;
                            } else if (child.tagName === 'INPUT' && child.readOnly && child.disabled && child.type === 'text' && child.id.startsWith('inputElement_choice-parameter')) {
                                // Input text box
                                parameterElement = child;
                                break;
                            } else if (['OL', 'UL'].includes(child.tagName)) {
                                // Numbered List / Bullet items list
                                parameterElement = child;
                                break;
                            }
                        }
                    }
                }

                let rp = new UnoChoice.ReferencedParameter(referencedParameters[i], parameterElement, dynamicParameter);
                if (cascadeParameters && cascadeParameters.length > 0) {
                    for (let i = 0; i < cascadeParameters.length; i++) {
                        let other = cascadeParameters[i];
                        if (rp.referencesMe(other)) {
                            console.log(`Updating ${other.getParameterName()} from ${rp.getParameterName()}`);
                            await other.update(true);
                        }
                    }
                }
            }

            // // call update methods in Java passing the HTML values
            // console.log('Updating cascade of parameter [', name, '] ...');
            // await dynamicParameter.update(false);
        } else {
            console.log('Parameter error: Missing parameter [', paramName, '] HTML element!');
        }
    }

    // Deciding on what is exported and returning instance
    instance.ReferencedParameter = ReferencedParameter;
    instance.CascadeParameter = CascadeParameter;
    instance.DynamicReferenceParameter = DynamicReferenceParameter;
    instance.FilterElement = FilterElement;
    instance.fakeSelectRadioButton = fakeSelectRadioButton;
    instance.getParameterValue = getParameterValue;
    instance.makeStaplerProxy2 = makeStaplerProxy2;
    instance.cascadeParameters = cascadeParameters;
    instance.renderChoiceParameter = renderChoiceParameter;
    instance.renderCascadeChoiceParameter = renderCascadeChoiceParameter;
    instance.renderDynamicRenderParameter = renderDynamicRenderParameter;
    return instance;
})(jQuery3);
window.UnoChoice = UnoChoice;
