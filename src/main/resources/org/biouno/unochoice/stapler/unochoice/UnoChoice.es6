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
 * @param $ jQuery3 global var
 * @author Bruno P. Kinoshita <brunodepaulak@yahoo.com.br>
 * @since 0.20
 */
var UnoChoice = UnoChoice || ($ => {
    let util = new Util($);
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
    function CascadeParameter(paramName, paramElement, randomName, proxy) {
        this.paramName = paramName;
        this.paramElement = paramElement;
        this.randomName = randomName;
        this.proxy = proxy;
        this.referencedParameters = [];
        this.filterElement = null;
    }
    /**
     * Gets the parameter name.
     *
     * @return {string} parameter name
     */
    CascadeParameter.prototype.getParameterName = function() {
        return this.paramName;
    }
    /**
     * Gets the parameter HTML element.
     *
     * @return {HTMLElement} HTML element
     */
    CascadeParameter.prototype.getParameterElement = function() {
        return this.paramElement;
    }
    /**
     * Gets the array of referenced parameters.
     *
     * @return {Array<ReferencedParameter>} Array of the ReferencedParameter's
     */
    CascadeParameter.prototype.getReferencedParameters = function() {
        return this.referencedParameters;
    }
    /**
     * Gets the parameter random name.
     *
     * @return {string} parameter random name
     */
    CascadeParameter.prototype.getRandomName = function() {
        return this.randomName;
    }
    /**
     * Gets the filter element.
     *
     * @return {FilterElement}
     */
    CascadeParameter.prototype.getFilterElement = function() {
        return this.filterElement;
    }
    /**
     * Sets the filter element.
     *
     * @param e {FilterElement}
     */
    CascadeParameter.prototype.setFilterElement = function(e) {
        this.filterElement = e;
    }
    /**
     * Used to create the request string that will update the cascade parameter values. Returns a
     * String, with name=value for each referenced parameter.
     *
     * @return {string} String with name=value for each referenced parameter
     */
    CascadeParameter.prototype.getReferencedParametersAsText = function() {
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
    CascadeParameter.prototype.update = async function(avoidRecursion) {
        let parametersString = this.getReferencedParametersAsText(); // gets the array parameters, joined by , (e.g. a,b,c,d)
        console.log(`Values retrieved from Referenced Parameters: ${parametersString}`);
        // Update the CascadeChoiceParameter Map of parameters
        await new Promise((resolve) => this.proxy.doUpdate(parametersString, t => resolve(t)));

        let spinner, rootDiv;
        if (this.getRandomName()) {
            let spinnerId = this.getRandomName().split('_').pop();
            spinner = jQuery(`div#${spinnerId}-spinner`);
            // Show spinner
            if (spinner) {
                spinner.show();
            }
            // Disable DIV changes
            rootDiv = jQuery(`div#${spinnerId}`);
            if (rootDiv) {
                rootDiv.css('pointer-events', 'none');
            }
        }

        // Now we get the updated choices, after the Groovy script is eval'd using the updated Map of parameters
        // The inner function is called with the response provided by Stapler. Then we update the HTML elements.
        let _self = this; // re-reference this to use within the inner function
        console.log('Calling Java server code to update HTML elements...');
        await new Promise((resolve) => this.proxy.getChoicesForUI(t => {
            let data = t.responseObject();
            console.log(`Values returned from server: ${data}`);
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
                    newValues[i] = newValues[i].replace(/:selected$/,'').replace(/:selected:disabled$/, ':disabled');
                }
                if (newValue && (newValue.endsWith(':disabled') || newValue.endsWith(':disabled:selected'))) {
                    disabledElements.push(i);
                    newValues[i] = newValues[i].replace(/:disabled$/,'').replace(/:disabled:selected$/, ':selected');
                }
                let newKey = String(newKeys[i]);
                if (newKey && typeof newKey === "string" && (newKey.endsWith(':selected') || newKey.endsWith(':selected:disabled'))) {
                    newKeys[i] = newKeys[i].replace(/:selected$/,'').replace(/:selected:disabled$/,':disabled');
                }
                if (newKey && typeof newKey === "string" && (newKey.endsWith(':disabled') || newKey.endsWith(':disabled:selected'))) {
                    newKeys[i] = newKeys[i].replace(/:disabled$/,'').replace(/:disabled:selected$/,':selected');
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
                    $(tbody).empty();
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
            resolve(t)
        }));
        // propagate change
        // console.log('Propagating change event from ' + this.getParameterName());
        // let e1 = $.Event('change', {parameterName: this.getParameterName()});
        // $(this.getParameterElement()).trigger(e1);
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
     * it returns whether or not the given parameter references this parameter.
     *
     * @since 0.22
     * @param cascadeParameter {CascadeParameter} a given parameter
     * @return {boolean} <code>true</code> iff the given parameter references this parameter
     */
    CascadeParameter.prototype.referencesMe = function(cascadeParameter) {
        if (!cascadeParameter ||
            !cascadeParameter.getReferencedParameters() ||
            cascadeParameter.getReferencedParameters().length === 0)
            return false;
        for (let i = 0; i < cascadeParameter.getReferencedParameters().length; i++) {
            let referencedParameter = cascadeParameter.getReferencedParameters()[i];
            if (referencedParameter.getParameterName() === this.getParameterName())
                return true;
        }
        return false;
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
    function ReferencedParameter(paramName, paramElement, cascadeParameter) {
        this.paramName = paramName;
        this.paramElement = paramElement;
        this.cascadeParameter = cascadeParameter;
        // Add event listener
        let _self = this;
        $(this.paramElement).change(e => {
            if (e.parameterName === _self.paramName) {
                console.log('Skipping self reference to avoid infinite loop!');
                e.stopImmediatePropagation();
            } else {
                console.log(`Cascading changes from parameter ${_self.paramName}...`);
                //_self.cascadeParameter.loading(true);
                $(".behavior-loading").show();
                // start updating in separate async function so browser will be able to repaint and show 'loading' animation , see JENKINS-34487
                setTimeout(async () => {
                    await _self.cascadeParameter.update(false);
                    $(".behavior-loading").hide();
                }, 0);
            }
        });
        cascadeParameter.getReferencedParameters().push(this);
    }
    ReferencedParameter.prototype.getParameterName = function() {
        return this.paramName;
    }
    ReferencedParameter.prototype.getParameterElement = function() {
        return this.paramElement;
    }
    ReferencedParameter.prototype.getCascadeParameter = function() {
        return this.cascadeParameter;
    }
    // --- Dynamic Reference Parameter
    /**
     * A parameter that is used only as a render mechanism for other referenced parameters.
     *
     * @param paramName {string} parameter name
     * @param paramElement {HTMLElement} parameter HTML element
     * @param proxy Stapler proxy object that references the CascadeChoiceParameter
     */
    function DynamicReferenceParameter(paramName, paramElement, proxy) {
        this.paramName = paramName;
        this.paramElement = paramElement;
        this.proxy = proxy;
        this.referencedParameters = [];
    }
    /**
     * Extend the cascade parameter.
     */
    DynamicReferenceParameter.prototype = new CascadeParameter();
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
    DynamicReferenceParameter.prototype.update = async function(avoidRecursion) {
        let parametersString = this.getReferencedParametersAsText(); // gets the array parameters, joined by , (e.g. a,b,c,d)
        console.log(`Values retrieved from Referenced Parameters: ${parametersString}`);
        // Update the Map of parameters
        await new Promise((resolve) => this.proxy.doUpdate(parametersString, t => resolve(t)));
        let parameterElement = this.getParameterElement();

        let spinner, rootDiv;
        if (parameterElement.id) {
            let spinnerId = parameterElement.id.split('_').pop();
            spinner = jQuery(`div#${spinnerId}-spinner`);
            // Show spinner
            if (spinner) {
                spinner.show();
            }
            rootDiv = jQuery(`div#${spinnerId}`);
            // Disable DIV changes
            if (rootDiv) {
                rootDiv.css('pointer-events', 'none');
            }
        }
        // Here depending on the HTML element we might need to call a method to return a Map of elements,
        // or maybe call a string to put as value in a INPUT.
        if (parameterElement.tagName === 'OL') { // handle OL's
            console.log('Calling Java server code to update HTML elements...');
            await new Promise((resolve) => this.proxy.getChoicesForUI(t => {
                $(parameterElement).empty(); // remove all children elements
                let data = t.responseObject();
                console.log(`Values returned from server: ${data}`);
                let newValues = data[0];
                // let newKeys = data[1];
                for (let i = 0; i < newValues.length; ++i) {
                    let li = document.createElement('li');
                    li.innerHTML = newValues[i];
                    parameterElement.appendChild(li); // append new elements
                }
                resolve(t)
            }));
        } else if (parameterElement.tagName === 'UL') { // handle OL's
            $(parameterElement).empty(); // remove all children elements
            console.log('Calling Java server code to update HTML elements...');
            await new Promise(resolve => this.proxy.getChoicesForUI(t => {
                let data = t.responseObject();
                console.log(`Values returned from server: ${data}`);
                let newValues = data[0];
                // let newKeys = data[1];
                for (let i = 0; i < newValues.length; ++i) {
                    let li = document.createElement('li');
                    li.innerHTML = newValues[i];
                    parameterElement.appendChild(li); // append new elements
                }
                resolve(t)
            }));
        } else if (parameterElement.id.indexOf('inputElement_') > -1) { // handle input text boxes
            await new Promise(resolve => this.proxy.getChoicesAsStringForUI(t => {
                parameterElement.value = t.responseObject();
                resolve(t)
            }));
        } else if (parameterElement.id.indexOf('formattedHtml_') > -1) { // handle formatted HTML
            await new Promise(resolve => this.proxy.getChoicesAsStringForUI(t => {
                parameterElement.innerHTML = t.responseObject();
                resolve(t)
            }));
        }
        // propagate change
        // console.log('Propagating change event from ' + this.getParameterName());
        // let e1 = $.Event('change', {parameterName: this.getParameterName()});
        // $(this.getParameterElement()).trigger(e1);
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
    // --- Filter Element
    /**
     * An element that acts as filter for other elements.
     *
     * @param paramElement {HTMLElement} HTML element being filtered
     * @param filterElement {HTMLElement} HTML element where the user enter the filter
     * @param filterLength {number} filter length
     */
    function FilterElement(paramElement, filterElement, filterLength) {
        this.paramElement = paramElement;
        this.filterElement = filterElement;
        this.filterLength = filterLength;
        this.originalArray = [];
        // push existing values into originalArray array
        if (this.paramElement.tagName === 'SELECT') { // handle SELECTS
            let options = $(paramElement).children().toArray();
            for (let i = 0; i < options.length; ++i) {
                this.originalArray.push(options[i]);
            }
        } else if (paramElement.tagName === 'DIV' || paramElement.tagName === 'SPAN') { // handle CHECKBOXES
            if ($(paramElement).children().length > 0 && (paramElement.children[0].tagName === 'DIV' || paramElement.children[0].tagName === 'SPAN')) {
                let tbody = paramElement.children[0];
                let trs = $(tbody).find('div');
                for (let i = 0; i < trs.length ; ++i) {
                    let tds = $(trs[i]).find('div');
                    let inputs = $(tds[0]).find('input');
                    let input = inputs[0];
                    this.originalArray.push(input);
                }
            } // if ($(paramElement).children().length > 0 && paramElement.children[0].tagName === 'DIV') {
        }
        this.initEventHandler();
    }
    /**
     * Gets the parameter HTML element.
     *
     * @return {HTMLElement} HTML element
     */
    FilterElement.prototype.getParameterElement = function() {
        return this.paramElement;
    }
    /**
     * Gets the filter element.
     *
     * @return {HTMLElement} HTML element
     */
    FilterElement.prototype.getFilterElement = function() {
        return this.filterElement;
    }
    /**
     * Gets an array with the original options of the filtered element. Useful for recreating the initial setting.
     *
     * @return {Array<HTMLElement>} <code>Array</code> with HTML elements
     */
    FilterElement.prototype.getOriginalArray = function() {
        return this.originalArray;
    }
    /**
     * Get the filter length.
     * @return {number} filter length
     */
    FilterElement.prototype.getFilterLength = function() {
        return this.filterLength;
    }
    /**
     * Sets the array with the original options of the filtered element. Once the array has been
     * set, it empties the value of the filter input box, thus allowing the user to type in again.
     *
     * @param originalArray
     */
    FilterElement.prototype.setOriginalArray = function(originalArray) {
        this.originalArray = originalArray;
        this.clearFilterElement();
    }
    /**
     * Clears the filter input box.
     *
     * @since 0.23
     */
    FilterElement.prototype.clearFilterElement = function() {
        this.getFilterElement().value = '';
    }
    /**
     * Initiates an event listener for Key Up events. Depending on the element type it will interpret the filter, and
     * the filtered element, to update its values.
     */
    FilterElement.prototype.initEventHandler = function() {
        let _self = this;
        $(_self.filterElement).keyup(e => {
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
                if (typeof options[i] !== 'undefined' && options[i].tagName === 'INPUT' ) {
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
               $(filteredElement).children().remove();
               for (let i = 0; i < newOptions.length ; ++i) {
                   let opt = document.createElement('option');
                   opt.value = newOptions[i].value;
                   opt.innerHTML = newOptions[i].innerHTML;
                   $(filteredElement).append(opt);
               }
            } else if (tagName === 'DIV' || tagName === 'SPAN') { // handle CHECKBOXES, RADIOBOXES and other elements (Jenkins renders them as tables)
                if ($(filteredElement).children().length > 0 && ($(filteredElement).children()[0].tagName === 'DIV' || $(filteredElement).children()[0].tagName === 'SPAN')) {
                    let tbody = filteredElement.children[0];
                    $(tbody).empty();
                    if (filteredElement.className === 'dynamic_checkbox') {
                        for (let i = 0; i < newOptions.length; i++) {
                            let entry = newOptions[i];
                            let idValue = `ecp_${e.target.randomName}_${i}`;
                            idValue = idValue.replace(' ', '_');

                            let input =
                                    entry instanceof String ?
                                            util.makeCheckbox(entry, undefined, undefined) :
                                            entry.tagName === 'INPUT' ?
                                                    entry :
                                                    util.makeRadio(JSON.stringify(entry.value), 'value', undefined, undefined);

                            // LABEL
                            let label = (entry instanceof String || entry.tagName === 'INPUT') ?
                                    util.makeLabel(entry.getAttribute('title'), entry.getAttribute('title')) :
                                    util.makeLabel(input, undefined);

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
                } // if ($(filteredElement).children().length > 0 && $(filteredElement).children()[0].tagName === 'DIV') {
            } // if (tagName === 'SELECT') { // } else if (tagName === 'DIV') {
            // Propagate the changes made by the filter
            console.log('Propagating change event after filtering');
            let e1 = $.Event('change', {parameterName: 'Filter Element Event'});
            $(filteredElement).trigger(e1);
        });
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
        let element = $(`#${id}`).get(0);
        // deselect all radios with the class=clazzName
        let radios = $(`input[class="${clazzName}"]`);
        radios.each(function(index) {
            $(this).attr('name', '');
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
        let e = $(htmlParameter);
        let value = '';
        if (e.attr('name') === 'value') {
            value = util.getElementValue(e);
        }  else if (e.prop('tagName') === 'DIV' || e.prop('tagName') === 'SPAN') {
            let subElements = e.find('[name="value"]');
            if (subElements) {
                let valueBuffer = Array();
                subElements.each(function() {
                    let tempValue = util.getElementValue($(this));
                    if (tempValue)
                        valueBuffer.push(tempValue);
                });
                value = valueBuffer.toString();
            }
        } else if (e.attr('type') === 'file') {
            let filesList = e.get(0).files;
            if (filesList && filesList.length > 0) {
                let firstFile = filesList[0]; // ignoring other files... but we could use it...
                value = firstFile.name;
            }
        } else if (e.prop('tagName') === 'INPUT' && !['', 'name'].includes(e.attr('name'))) {
            value = util.getElementValue(e);
        }
        return value;
    }

    function renderChoiceParameter(paramName, filterLength) {
        let parentDiv = $(`#${paramName}`);
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

    async function renderCascadeChoiceParameter(parentDivRef, filterable, name, randomName, filterLength, paramName, referencedParameters, cascadeChoiceParameter) {
        // find the cascade parameter element
        let parentDiv = jQuery(parentDivRef);
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
            for (let i  = 0; i < referencedParameters.length ; ++i) {
                let parameterElement = null;
                // FIXME: review the block below
                let divs = jQuery('div[name="parameter"]');
                for (let j = 0; j < divs.length ; j++) {
                    let div = divs[j];
                    let hiddenNames = jQuery(div).find('input[name="name"]');
                    if (hiddenNames[0].value === referencedParameters[i]) {
                        let children = div.children;
                        for (let k = 0; k < children.length; ++k) {
                            let child = children[k];
                            if (child.getAttribute('name') === 'value') {
                                parameterElement = child;
                                break;
                            } else if (child.tagName === 'DIV' || child.tagName === 'SPAN') {
                                let subValues = jQuery(child).find('input[name="value"]');
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
                            } else if (child.tagName === 'INPUT' && !['', 'name'].includes(child.name)) {
                                parameterElement = child;
                                break;
                            }
                        }
                    }
                }

                new UnoChoice.ReferencedParameter(referencedParameters[i], parameterElement, cascadeParameter);
            }

            // call update methods in Java passing the HTML values
            console.log('Updating cascade of parameter [', name, '] ...');
            await cascadeParameter.update(false);
        } else {
            console.log('Parameter error: Missing parameter [', paramName, '] HTML element!');
        }
    }

    async function renderDynamicRenderParameter(parentDivRef, name, paramName, referencedParameters, dynamicReferenceParameter) {
        // find the cascade parameter element
        let parentDiv = jQuery(parentDivRef);
        // if the parameter class has been set to hidden, then we hide it now
        if (parentDiv.get(0).getAttribute('class') === 'hidden_uno_choice_parameter') {
            let parentTbody = jQuery(parentDiv.get(0)).parents('tbody');
            // FIXME: temporary fix to support both TABLE and DIV in the Jenkins UI
            //        remove after most users have migrated to newer versions with DIVs
            if (!parentTbody || parentTbody.length === 0) {
                parentTbody = jQuery(parentDiv.get(0)).parents('div > div.tr');
            }
            if (parentTbody && parentTbody.length > 0) {
                jQuery(parentTbody.get(0)).attr('style', 'visibility:hidden;position:absolute;');
            }
        }
        let parameterHtmlElement = null;
        for(let i = 0; i < parentDiv.children().length; i++) {
            let child = parentDiv.children()[i];
            if (child.getAttribute('name') === 'value' || child.id.indexOf('ecp_') > -1) {
                parameterHtmlElement = jQuery(child);
                break;
            }
            if (child.id.indexOf('inputElement_') > -1) {
                parameterHtmlElement = jQuery(child);
                break;
            }
            if (child.id.indexOf('formattedHtml_') > -1) {
                parameterHtmlElement = jQuery(child);
                break;
            }
            if (child.id.indexOf('imageGallery_') > -1) {
                parameterHtmlElement = jQuery(child);
                break;
            }
        }
        if (parameterHtmlElement && parameterHtmlElement.get(0)) {
            let dynamicParameter = new UnoChoice.DynamicReferenceParameter(name, parameterHtmlElement.get(0), dynamicReferenceParameter);
            UnoChoice.cascadeParameters.push(dynamicParameter); // TODO review whether it is right or not to add a dynamic parameter here
            for (let i  = 0; i < referencedParameters.length ; ++i) {
                let parameterElement = null;
                // FIXME: review the block below
                let divs = jQuery('div[name="parameter"]');
                for (let j = 0; j < divs.length ; j++) {
                    let div = divs[j];
                    let hiddenNames = jQuery(div).find('input[name="name"]');
                    if (hiddenNames[0].value === referencedParameters[i]) {
                        let children = div.children;
                        for (let k = 0; k < children.length; ++k) {
                            let child = children[k];
                            if (child.getAttribute('name') === 'value') {
                                parameterElement = child;
                                break;
                            } else if (child.tagName === 'DIV' || child.tagName === 'SPAN') {
                                let subValues = jQuery(child).find('input[name="value"]');
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
                            } else if (child.tagName === 'INPUT' && !['', 'name'].includes(child.name)) {
                                parameterElement = child;
                                break;
                            }
                        }
                    }
                }

                new UnoChoice.ReferencedParameter(referencedParameters[i], parameterElement, dynamicParameter);
            }

            // call update methods in Java passing the HTML values
            console.log('Updating cascade of parameter [', name, '] ...');
            await dynamicParameter.update(false);
        } else {
            console.log('Parameter error: Missing parameter [', paramName,'] HTML element!');
        }
    }

    // Deciding on what is exported and returning instance
    instance.fakeSelectRadioButton = fakeSelectRadioButton;
    instance.getParameterValue = getParameterValue;
    instance.CascadeParameter = CascadeParameter;
    instance.DynamicReferenceParameter = DynamicReferenceParameter;
    instance.ReferencedParameter = ReferencedParameter;
    instance.FilterElement = FilterElement;
    instance.cascadeParameters = cascadeParameters;
    instance.renderChoiceParameter = renderChoiceParameter;
    instance.renderCascadeChoiceParameter = renderCascadeChoiceParameter;
    instance.renderDynamicRenderParameter = renderDynamicRenderParameter;
    return instance;
})(jQuery3);
window.UnoChoice = UnoChoice;
