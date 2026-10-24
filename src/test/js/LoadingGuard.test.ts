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

import {afterEach, beforeEach, describe, expect, test} from '@jest/globals';
// @ts-ignore
import LoadingGuard from '../../main/js/LoadingGuard.ts';

let guard: LoadingGuard;
let form: HTMLFormElement;
let build: HTMLButtonElement;
let param: HTMLElement;

beforeEach(() => {
    document.body.innerHTML = `
        <form name="parameters">
            <div name="parameter" id="p1"><input type="hidden" name="name" value="P1"><select name="value"></select>
                <button id="inner">inside a parameter</button></div>
            <div class="jenkins-buttons-row"><button id="build">Build</button><a href="./">Cancel</a></div>
        </form>`;
    form = document.querySelector('form') as HTMLFormElement;
    build = document.getElementById('build') as HTMLButtonElement;
    param = document.getElementById('p1') as HTMLElement;
    guard = new LoadingGuard(document);
});

afterEach(() => {
    // Settles any open ticker so tests don't leak intervals.
    guard.pending = 0;
    guard.failed = false;
    guard.begin()();
});

function status(): string {
    return (document.querySelector('.ac-loading-status') as HTMLElement).textContent || '';
}

function submitPrevented(): boolean {
    let event = new Event('submit', {bubbles: true, cancelable: true});
    form.dispatchEvent(event);
    return event.defaultPrevented;
}

describe('LoadingGuard', () => {
    test('disables Build while an update is pending and re-enables it once done', () => {
        let done = guard.begin(param);
        expect(build.disabled).toBe(true);
        expect(status()).toMatch(/^Calculating parameters/);
        done();
        expect(build.disabled).toBe(false);
        expect(status()).toBe('');
    });

    test('stays disabled until the last of overlapping updates is done', () => {
        let first = guard.begin(param);
        let second = guard.begin();
        first();
        expect(build.disabled).toBe(true);
        second();
        expect(build.disabled).toBe(false);
    });

    test('a release function counts only once', () => {
        let first = guard.begin(param);
        let second = guard.begin();
        first();
        first();
        expect(guard.pending).toBe(1);
        expect(build.disabled).toBe(true);
        second();
        expect(build.disabled).toBe(false);
    });

    test('a failure keeps Build disabled after all updates are done', () => {
        let done = guard.begin(param);
        guard.fail();
        done();
        expect(build.disabled).toBe(true);
        expect(status()).toBe('A parameter failed to load. Reload the page before building.');
        expect(document.querySelector('.ac-loading-status--error')).not.toBeNull();
    });

    test('buttons inside a parameter are left alone', () => {
        let done = guard.begin(param);
        expect((document.getElementById('inner') as HTMLButtonElement).disabled).toBe(false);
        done();
    });

    test('a button disabled by someone else stays disabled', () => {
        build.disabled = true;
        let done = guard.begin(param);
        done();
        expect(build.disabled).toBe(true);
    });

    test('blocks submitting the form while busy', () => {
        let done = guard.begin(param);
        expect(submitPrevented()).toBe(true);
        done();
        expect(submitPrevented()).toBe(false);
    });

    test('ignores updates of elements outside a form', () => {
        let outside = document.createElement('div');
        document.body.appendChild(outside);
        let done = guard.begin(outside);
        expect(build.disabled).toBe(false);
        expect(document.querySelector('.ac-loading-status')).toBeNull();
        done();
    });
});
