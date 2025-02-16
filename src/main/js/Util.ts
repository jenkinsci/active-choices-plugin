export default class Util {

    jQuery: JQueryStatic;

    constructor(jQuery: JQueryStatic) {
        this.jQuery = jQuery;
    }

    /**
     * Creates a radio input element.
     * @param value {string} json and value of the element
     * @param name {string } name of the element
     * @param checked {boolean} if true, the radio is checked
     * @param disabled {boolean} if true, the radio is disabled
     * @returns {HTMLInputElement}
     */
    public makeRadio(value: string, name: string, checked: boolean = false, disabled: boolean = false): HTMLInputElement {
        let input = document.createElement('input');
        input.setAttribute('json', value);
        input.setAttribute('name', name);
        input.setAttribute("value", value);
        input.setAttribute("type", "radio");
        if (checked) input.setAttribute("checked", "checked");
        if (disabled) input.setAttribute("disabled", "disabled");
        return input;
    }

    /**
     * Creates a checkbox input element.
     * @param entry {string} json and value of the element
     * @param checked {boolean} if true, the checkbox is checked
     * @param disabled {boolean} if true, the checkbox is disabled
     * @returns {HTMLInputElement}
     */
    public makeCheckbox(entry: string, checked: boolean = false, disabled: boolean = false): HTMLInputElement {
        let input = document.createElement('input');
        input.setAttribute('json', entry);
        input.setAttribute('name', 'value');
        input.setAttribute("value", entry);
        input.setAttribute("type", "checkbox");
        if (checked) input.setAttribute("checked", "checked");
        if (disabled) input.setAttribute("disabled", "disabled");
        return input
    }
    /**
     * Creates a hidden input element.
     * @param id {string} id of the element
     * @param json {string} json of the element
     * @param name {string} name of the element
     * @param value {string} value of the element
     * @param clazz {string} class of the element
     * @param title {string} title of the element
     * @returns {HTMLInputElement}
     */
    public makeHidden(id: string, json: string, name: string, value: string, clazz: string, title: string): HTMLInputElement {
        let hidden = document.createElement('input'); // used to help in the selection
        hidden.setAttribute('id', id);
        hidden.setAttribute('json', json);
        hidden.setAttribute('name', name);
        hidden.setAttribute("value", value);
        hidden.setAttribute("class", clazz);
        hidden.setAttribute("type", "hidden");
        hidden.setAttribute('title', title);
        return hidden;
    }

    /**
     * Creates a table cell with the given elements.
     * @param elements {Array<HTMLElement>} elements to be added to the cell
     * @returns {HTMLDivElement}
     */
    public makeTd(elements: Array<HTMLElement>): HTMLDivElement {
        let td = document.createElement('div');
        for (let i = 0; i < elements.length; i++) {
            td.appendChild(elements[i]);
        }
        return td;
    }

    /**
     * Creates a table row with the given id.
     * @param id {string} id of the row
     * @returns {HTMLDivElement}
     */
    public makeTr(id?: string): HTMLDivElement {
        let tr = document.createElement('div');
        tr.setAttribute('style', 'white-space:nowrap');
        tr.setAttribute('class', 'tr');
        if (id) tr.setAttribute('id', id);
        return tr
    }

    /**
     * Creates a label with the given elements.
     * @param innerHTML {string} innerHTML of the cell
     * @param title {string} title of the cell
     * @returns {HTMLLabelElement}
     */
    public makeLabel(innerHTML: string, title?: string) {
        let label = document.createElement('label');
        label.innerHTML = innerHTML;
        label.className = "attach-previous";
        if (title) label.title = title;
        return label;
    }

    /**
     * Gets an array of the selected option values in a HTML select element.
     *
     * @param select {JQuery<HTMLSelectElement>} HTML DOM select element
     * @return {Array<string>} <code>Array</code>
     *
     * @see http://stackoverflow.com/questions/5866169/getting-all-selected-values-of-a-multiple-select-box-when-clicking-on-a-button-u
     */
    public getSelectValues(select: JQuery<HTMLSelectElement>): string[] {
        return select.val() as string[];
    }

    /**
     * Gets the value of a HTML element as string. If the returned value is an Array it gets serialized first.
     * Correctly handles SELECT, CHECKBOX, RADIO, and other types.
     *
     * @param e {JQuery<HTMLElement>} HTML element
     * @return {string} the returned value as string. Empty by default.
     */
    public getElementValue(e: JQuery): string {
        if (e.prop('tagName') === 'SELECT') {
            return this.getSelectValues(e as JQuery<HTMLSelectElement>).toString();
        } else if (e.attr('type') === 'checkbox' || e.attr('type') === 'radio') {
            return e.prop('checked') ? e.val().toString(): '';
        } else {
            return e.val().toString();
        }
    }

}
