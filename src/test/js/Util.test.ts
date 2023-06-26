import {describe, test} from '@jest/globals';
import Util from '../../main/resources/org/biouno/unochoice/stapler/unochoice/Util.ts';
import expect from "expect";
import jQuery from "jquery";

const util = new Util(jQuery);

describe('makeRadio', () => {
    test('When only value and name are provided, a valid checkbox is built', () => {
        let radio = util.makeRadio('someval', 'somename');
        expect(radio).toBeInstanceOf(HTMLInputElement);
        expect(radio.type).toBe('radio');
        expect(radio.value).toBe('someval');
        expect(radio.getAttribute('json')).toBe('someval');
        expect(radio.name).toBe('somename');
        expect(radio.checked).toBe(false);
        expect(radio.disabled).toBe(false);
    });
    test('When checked is specified, it is set', () => {
        let radio = util.makeRadio('someval', 'somename', true);
        expect(radio).toBeInstanceOf(HTMLInputElement);
        expect(radio.type).toBe('radio');
        expect(radio.value).toBe('someval');
        expect(radio.getAttribute('json')).toBe('someval');
        expect(radio.name).toBe('somename');
        expect(radio.checked).toBe(true);
        expect(radio.disabled).toBe(false);
    });
    test('When disabled is specified, it is set', () => {
        let radio = util.makeRadio('someval', 'somename', true, true);
        expect(radio).toBeInstanceOf(HTMLInputElement);
        expect(radio.type).toBe('radio');
        expect(radio.value).toBe('someval');
        expect(radio.getAttribute('json')).toBe('someval');
        expect(radio.name).toBe('somename');
        expect(radio.checked).toBe(true);
        expect(radio.disabled).toBe(true);
    });
});

describe('makeCheckbox', () => {
    test('When only value is provided, a valid checkbox is built', () => {
        let checkbox = util.makeCheckbox('someval');
        expect(checkbox).toBeInstanceOf(HTMLInputElement);
        expect(checkbox.type).toBe('checkbox');
        expect(checkbox.value).toBe('someval');
        expect(checkbox.getAttribute('json')).toBe('someval');
        expect(checkbox.name).toBe('value');
        expect(checkbox.checked).toBe(false);
        expect(checkbox.disabled).toBe(false);
    });
    test('When checked is specified, it is set', () => {
        let checkbox = util.makeCheckbox('someval', true);
        expect(checkbox).toBeInstanceOf(HTMLInputElement);
        expect(checkbox.type).toBe('checkbox');
        expect(checkbox.value).toBe('someval');
        expect(checkbox.getAttribute('json')).toBe('someval');
        expect(checkbox.name).toBe('value');
        expect(checkbox.checked).toBe(true);
        expect(checkbox.disabled).toBe(false);
    });
    test('When disabled is specified, it is set', () => {
        let checkbox = util.makeCheckbox('someval', true, true);
        expect(checkbox).toBeInstanceOf(HTMLInputElement);
        expect(checkbox.type).toBe('checkbox');
        expect(checkbox.value).toBe('someval');
        expect(checkbox.getAttribute('json')).toBe('someval');
        expect(checkbox.name).toBe('value');
        expect(checkbox.checked).toBe(true);
        expect(checkbox.disabled).toBe(true);
    });
});

describe('makeHidden', () => {
    test('a valid hidden input is produced', () => {
        let hidden = util.makeHidden('id', 'json', 'name', 'value', 'clazz', 'title');
        expect(hidden).toBeInstanceOf(HTMLInputElement);
        expect(hidden.type).toBe('hidden');
        expect(hidden.id).toBe('id');
        expect(hidden.getAttribute('json')).toBe('json');
        expect(hidden.name).toBe('name');
        expect(hidden.value).toBe('value');
        expect(hidden.className).toBe('clazz');
        expect(hidden.title).toBe('title');

    });
});

describe('makeTd', () => {
    test('when no elements are provided, a valid td is produced', () => {
        let td = util.makeTd([]);
        expect(td).toBeInstanceOf(HTMLDivElement);
        expect(td.innerHTML).toBe('');
    });
    test('when 1 element is provided, a valid td is produced', () => {
        let td = util.makeTd([util.makeCheckbox('someval')]);
        expect(td).toBeInstanceOf(HTMLDivElement);
        expect(td.innerHTML).toBe('<input json="someval" name="value" value="someval" type="checkbox">');
    });
    test('when 2 elements are provided, a valid td is produced', () => {
        let td = util.makeTd([
            util.makeCheckbox('someval'),
            util.makeHidden('id', 'json', 'name', 'value', 'clazz', 'title')
        ]);
        expect(td).toBeInstanceOf(HTMLDivElement);
        expect(td.innerHTML).toBe('<input json="someval" name="value" value="someval" type="checkbox"><input id="id" json="json" name="name" value="value" class="clazz" type="hidden" title="title">');
    });
});

describe('makeTr', () => {
    test('creates a valid tr with undefined id', () => {
        let tr = util.makeTr(undefined);
        expect(tr).toBeInstanceOf(HTMLDivElement);
        expect(tr.id).toBe('');
        expect(tr.getAttribute('style')).toBe('white-space:nowrap');
        expect(tr.className).toBe('tr');
        expect(tr.innerHTML).toBe('');
    });
    test('creates a valid tr with valid id', () => {
        let tr = util.makeTr('something');
        expect(tr).toBeInstanceOf(HTMLDivElement);
        expect(tr.id).toBe('something');
        expect(tr.getAttribute('style')).toBe('white-space:nowrap');
        expect(tr.className).toBe('tr');
        expect(tr.innerHTML).toBe('');
    });
});

describe('makeLabel', () => {
    test('creates a valid label with title', () => {
        let label = util.makeLabel('<p></p>', 'title');
        expect(label).toBeInstanceOf(HTMLLabelElement);
        expect(label.className).toBe('attach-previous');
        expect(label.title).toBe('title');
        expect(label.innerHTML).toBe('<p></p>');
    });

    test('creates a valid label without title', () => {
        let label = util.makeLabel('<p></p>');
        expect(label).toBeInstanceOf(HTMLLabelElement);
        expect(label.className).toBe('attach-previous');
        expect(label.title).toBe('');
        expect(label.innerHTML).toBe('<p></p>');
    });
});

describe('getSelectValues', () => {
    test('returns empty array when no option is selected', () => {
        let theSelect = jQuery(`<select id="select" multiple>
            <option value="1">1</option>    
            <option value="2">2</option>    
            </select>`) as JQuery<HTMLSelectElement>;

        expect(util.getSelectValues(theSelect)).toEqual([]);
    });
    test('returns singleton array when one option is selected', () => {
        let theSelect = jQuery(`<select id="select" multiple>
            <option value="1" selected>1</option>    
            <option value="2">2</option>    
            </select>`) as JQuery<HTMLSelectElement>;

        expect(util.getSelectValues(theSelect)).toEqual(["1"]);
    });
    test('returns all selected options when multiple are selected', () => {
        let theSelect = jQuery(`<select id="select" multiple>
            <option value="1" selected>1</option>    
            <option value="2" selected>2</option>    
            <option value="3" >3</option>    
            </select>`) as JQuery<HTMLSelectElement>;

        expect(util.getSelectValues(theSelect)).toEqual(["1", "2"]);
    });
});

describe('getElementValue', () => {
    describe('When HTML Select is passed', () => {
        test('returns empty array when no option is selected', () => {
            let theSelect = jQuery(`<select id="select" multiple>
            <option value="1">1</option>    
            <option value="2">2</option>    
            </select>`) as JQuery<HTMLSelectElement>;

            expect(util.getElementValue(theSelect)).toEqual("");
        });
        test('returns singleton array when one option is selected', () => {
            let theSelect = jQuery(`<select id="select" multiple>
            <option value="1" selected>1</option>    
            <option value="2">2</option>    
            </select>`) as JQuery<HTMLSelectElement>;

            expect(util.getElementValue(theSelect)).toEqual("1");
        });
        test('returns all selected options when multiple are selected', () => {
            let theSelect = jQuery(`<select id="select" multiple>
            <option value="1" selected>1</option>    
            <option value="2" selected>2</option>    
            <option value="3" >3</option>    
            </select>`) as JQuery<HTMLSelectElement>;

            expect(util.getElementValue(theSelect)).toEqual("1,2");
        });
    });
    describe('When checkbox is passed', () => {
        test('returns empty string when no option is selected', () => {
            let theCheckbox = jQuery(`<input type="checkbox" id="checkbox" value="1">`) as JQuery<HTMLInputElement>;

            expect(util.getElementValue(theCheckbox)).toEqual("");
        });
        test('returns value when checkbox is selected', () => {
            let theCheckbox = jQuery(`<input type="checkbox" id="checkbox" value="1" checked>`) as JQuery<HTMLInputElement>;

            expect(util.getElementValue(theCheckbox)).toEqual("1");
        });
    });
    describe('When radio is passed', () => {
        test('returns empty string when no option is selected', () => {
            let theRadio = jQuery(`<input type="radio" id="radio" value="1">`) as JQuery<HTMLInputElement>;

            expect(util.getElementValue(theRadio)).toEqual("");
        });
        test('returns value when radio is selected', () => {
            let theRadio = jQuery(`<input type="radio" id="radio" value="1" checked>`) as JQuery<HTMLInputElement>;

            expect(util.getElementValue(theRadio)).toEqual("1");
        });
    });
    describe('When text is passed', () => {
        test('returns empty string no input is provided', () => {
            let theText = jQuery(`<input type="text" id="text">`) as JQuery<HTMLInputElement>;

            expect(util.getElementValue(theText)).toEqual("");
        });
        test('returns value when text is provided', () => {
            let theText = jQuery(`<input type="text" id="text" value="1">`) as JQuery<HTMLInputElement>;

            expect(util.getElementValue(theText)).toEqual("1");
        });
    });
});
