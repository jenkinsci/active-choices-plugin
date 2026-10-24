/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2026 Ioannis Moutsatsos, Bruno P. Kinoshita
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

import {beforeAll, beforeEach, describe, expect, test} from '@jest/globals';

// UnoChoice.es6 expects Jenkins' globals, so they are set before it is loaded.
// @ts-ignore
const jQuery = require('jquery');
// @ts-ignore
(global as any).jQuery3 = jQuery;
// @ts-ignore
(global as any).Behaviour = {
    applySubtree: () => {
    }
};

const SEPARATOR = '__LESEP__';
let UnoChoice: any;
// Per bound object: computes [values, keys] from the referenced values sent by doUpdate.
let backends: { [id: string]: (refs: { [name: string]: string }) => string[] } = {};
let refsById: { [id: string]: { [name: string]: string } } = {};
let failing: { [url: string]: boolean } = {};
let log: string[] = [];
let observer: MutationObserver | null = null;

const later = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Mimics Stapler: slow responses, and response bodies that arrive later still (the callback runs after the body).
function fakeFetch(url: string, init: any): Promise<any> {
    let [, id, method] = url.match(/\/bound\/([^/]+)\/(.+)$/) as RegExpMatchArray;
    let args = JSON.parse(init.body);
    return later(15 + Math.random() * 25).then(() => {
        if (failing[url]) {
            return {ok: false, status: 500, statusText: 'Server Error', headers: new Map()};
        }
        let data: any = null;
        if (method === 'doUpdate') {
            refsById[id] = {};
            String(args[0]).split(SEPARATOR).filter(Boolean).forEach(pair => {
                let [name, value] = pair.split('=');
                refsById[id][name] = value;
            });
        } else {
            let values = backends[id](refsById[id] || {});
            data = [values, values];
        }
        return {
            ok: true,
            status: 200,
            statusText: 'OK',
            headers: new Map([['content-type', 'application/json;charset=UTF-8']]),
            json: () => later(10 + Math.random() * 20).then(() => data),
        };
    });
}

function selectOf(id: string): HTMLSelectElement {
    return document.querySelector(`#${id} select`) as HTMLSelectElement;
}

function options(id: string): string[] {
    return Array.prototype.map.call(selectOf(id).options, (o: HTMLOptionElement) => o.value) as string[];
}

function build(): HTMLButtonElement {
    return document.getElementById('build') as HTMLButtonElement;
}

function activeChoice(id: string, name: string): string {
    return `<div id="${id}-spinner" class="ac-spinner"></div>
        <div name="parameter" id="${id}" class="active-choice"><input type="hidden" name="name" value="${name}">
            <div class="jenkins-select ac-ignore"><select name="value" class="jenkins-select__input"></select></div></div>`;
}

// Resolves once nothing has been pending for a while; fails the test if it never settles.
async function settle(): Promise<void> {
    let quiet = 0;
    for (let i = 0; i < 500 && quiet < 10; i++) {
        await later(10);
        quiet = UnoChoice.loadingGuard.pending === 0 ? quiet + 1 : 0;
    }
    expect(UnoChoice.loadingGuard.pending).toBe(0);
}

beforeAll(() => {
    // @ts-ignore
    (global as any).fetch = fakeFetch;
    // @ts-ignore
    require('../../main/js/UnoChoice.es6');
    UnoChoice = (window as any).UnoChoice;
});

beforeEach(async () => {
    // Every test starts from a fresh page; the module-level guard and parameter list are reset.
    await settle();
    UnoChoice.cascadeParameters.length = 0;
    UnoChoice.loadingGuard.failed = false;
    UnoChoice.loadingGuard.forms = [];
    UnoChoice.loadingGuard.statuses = [];
    UnoChoice.loadingGuard.disabledByGuard = [];
    if (observer) observer.disconnect();
    failing = {};
    log = [];
    document.body.innerHTML = `
        <form name="parameters" class="jenkins-form">
            <div name="parameter"><input type="hidden" name="name" value="Environment">
                <div class="jenkins-select"><select name="value" class="jenkins-select__input">
                    <option value="a" selected>a</option><option value="b">b</option></select></div></div>
            ${activeChoice('service', 'Service')}
            ${activeChoice('version', 'Version')}
            <div class="jenkins-buttons-row"><button id="build" class="jenkins-button jenkins-button--primary">Build</button>
                <a href="./" class="jenkins-button">Cancel</a></div>
        </form>`;
    backends = {
        service: refs => [`${refs.Environment}-svc1`, `${refs.Environment}-svc2`],
        version: refs => [`${refs.Service}-v1`],
    };
    // Records, in order, every re-render of a dependent select and every change of the Build button.
    observer = new MutationObserver(records => records.forEach(r => {
        if (r.type === 'attributes') {
            log.push(build().disabled ? 'build:disabled' : 'build:enabled');
        } else if ((r.target as Element).tagName === 'SELECT') {
            log.push(`render:${(r.target as Element).closest('[id]')!.id}`);
        }
    }));
    observer.observe(document.body, {subtree: true, childList: true, attributes: true, attributeFilter: ['disabled']});
    for (let [id, name, refs] of [['service', 'Service', ['Environment']], ['version', 'Version', ['Service']]] as [string, string, string[]][]) {
        let proxy = UnoChoice.makeStaplerProxy2(`/bound/${id}`, 'crumb', ['doUpdate', 'getChoicesForUI']);
        UnoChoice.renderCascadeChoiceParameter(`#${id}`, false, name, id, 0, id, refs, proxy);
    }
});

// Build must stay disabled from the first scheduled update until the very last render.
function expectNoEarlyEnable(): void {
    let lastRender = log.map(e => e.startsWith('render:')).lastIndexOf(true);
    let firstEnable = log.indexOf('build:enabled');
    expect(lastRender).toBeGreaterThanOrEqual(0);
    expect(firstEnable).toBeGreaterThan(lastRender);
}

describe('Build button while cascading parameters update (JENKINS-73556)', () => {
    test('is disabled as soon as the initial updates are scheduled, until the whole cascade has rendered', async () => {
        expect(build().disabled).toBe(true);
        await settle();
        expect(options('service')).toEqual(['a-svc1', 'a-svc2']);
        expect(options('version')).toEqual(['a-svc1-v1']);
        expect(build().disabled).toBe(false);
        expectNoEarlyEnable();
        expect(document.querySelector('.ac-loading-status')!.textContent).toBe('');
    });

    test('is disabled while a change cascades through dependent parameters', async () => {
        await settle();
        log = [];
        let environment = document.querySelector('select') as HTMLSelectElement;
        environment.value = 'b';
        jQuery(environment).trigger('change');
        expect(build().disabled).toBe(true);
        expect(document.querySelector('.ac-loading-status')!.textContent).toMatch(/^Calculating parameters/);
        await settle();
        expect(options('service')).toEqual(['b-svc1', 'b-svc2']);
        expect(options('version')).toEqual(['b-svc1-v1']);
        expect(build().disabled).toBe(false);
        expectNoEarlyEnable();
    });

    test('blocks form submission while updating', async () => {
        let event = new Event('submit', {bubbles: true, cancelable: true});
        document.querySelector('form')!.dispatchEvent(event);
        expect(event.defaultPrevented).toBe(true);
        await settle();
        let after = new Event('submit', {bubbles: true, cancelable: true});
        document.querySelector('form')!.dispatchEvent(after);
        expect(after.defaultPrevented).toBe(false);
    });

    test('stays disabled with a reload message when an update fails', async () => {
        await settle();
        failing['/bound/version/getChoicesForUI'] = true;
        let environment = document.querySelector('select') as HTMLSelectElement;
        environment.value = 'b';
        jQuery(environment).trigger('change');
        await settle();
        expect(options('service')).toEqual(['b-svc1', 'b-svc2']);
        expect(build().disabled).toBe(true);
        expect(document.querySelector('.ac-loading-status')!.textContent)
                .toBe('A parameter failed to load. Reload the page before building.');
    });
});
