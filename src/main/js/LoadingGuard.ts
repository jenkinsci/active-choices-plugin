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

/**
 * Keeps the submit buttons of a parameters form disabled while Active Choices
 * parameters are updating (JENKINS-73556).
 *
 * See also:
 *
 * - https://github.com/jenkinsci/active-choices-plugin/issues/868
 * - https://github.com/jenkinsci/active-choices-plugin/issues/902
 * - https://github.com/jenkinsci/active-choices-plugin/pull/1039
 *
 * @since 2.8.11
 */
export default class LoadingGuard {

    doc: Document;
    pending: number = 0;
    failed: boolean = false;
    busySince: number = 0;
    ticker: number | null = null;
    forms: HTMLFormElement[] = [];
    statuses: HTMLElement[] = [];
    // Only buttons this guard disabled are re-enabled, so other scripts' disabled state is kept.
    disabledByGuard: HTMLButtonElement[] = [];

    constructor(doc: Document) {
        this.doc = doc;
        // Blocks implicit submission (Enter in a text field) and programmatic requestSubmit() while busy.
        doc.addEventListener('submit', (e: Event) => {
            if (this.isBusy() && this.forms.indexOf(e.target as HTMLFormElement) >= 0) {
                e.preventDefault();
                e.stopImmediatePropagation();
            }
        }, true);
    }

    /**
     * Marks an update as started; the returned function marks it finished and is safe to call more than once.
     *
     * @param element {Element?}: The Active Choices HTML element.
     */
    public begin(element?: Element): () => void {
        this.watch(element);
        if (this.pending === 0) {
            this.busySince = Date.now();
        }
        this.pending++;
        this.render();
        let finished = false;
        return () => {
            if (finished) {
                return;
            }
            finished = true;
            this.pending--;
            this.render();
        };
    }

    /**
     * A failed update leaves a stale value behind, so the form stays blocked until the page is reloaded.
     */
    public fail(): void {
        this.failed = true;
        this.render();
    }

    public isBusy(): boolean {
        return this.failed || this.pending > 0;
    }

    private watch(element?: Element): void {
        let form = element ? element.closest('form') : null;
        if (!form || this.forms.indexOf(form) >= 0) {
            return;
        }
        this.forms.push(form);
        let buttons = this.submitButtons(form);
        let bar = buttons.length > 0 ? buttons[buttons.length - 1].parentElement : null;
        if (bar) {
            let status = this.doc.createElement('span');
            status.className = 'ac-loading-status';
            status.setAttribute('aria-live', 'polite');
            bar.appendChild(status);
            this.statuses.push(status);
        }
    }

    /**
     * Submit controls of the form itself; anything inside a parameter (div[name=parameter]) is left alone.
     *
     * @param form {HTMLFormElement}: The HTML submit form element.
     * @return {HTMLButtonElement[]}: List of buttons that are of type submit and have the name "parameter".
     */
    private submitButtons(form: HTMLFormElement): HTMLButtonElement[] {
        let buttons: HTMLButtonElement[] = [];
        let candidates = form.querySelectorAll('button, input[type="submit"]');
        for (let i = 0; i < candidates.length; i++) {
            let button = candidates[i] as HTMLButtonElement;
            if (button.type === 'submit' && !button.closest('[name="parameter"]')) {
                buttons.push(button);
            }
        }
        return buttons;
    }

    private render(): void {
        let busy = this.isBusy();
        if (busy) {
            for (let i = 0; i < this.forms.length; i++) {
                let buttons = this.submitButtons(this.forms[i]);
                for (let j = 0; j < buttons.length; j++) {
                    if (!buttons[j].disabled) {
                        buttons[j].disabled = true;
                        this.disabledByGuard.push(buttons[j]);
                    }
                }
            }
        } else {
            for (let i = 0; i < this.disabledByGuard.length; i++) {
                this.disabledByGuard[i].disabled = false;
            }
            this.disabledByGuard = [];
        }
        let text = '';
        if (this.failed) {
            text = 'A parameter failed to load. Reload the page before building.';
        } else if (busy) {
            text = `Calculating parameters… ${Math.round((Date.now() - this.busySince) / 1000)}s`;
        }
        for (let i = 0; i < this.statuses.length; i++) {
            this.statuses[i].textContent = text;
            this.statuses[i].classList.toggle('ac-loading-status--error', this.failed);
        }
        // Ticks the elapsed seconds while busy; stops once settled or failed.
        if (busy && !this.failed && this.ticker === null && this.statuses.length > 0) {
            this.ticker = window.setInterval(() => this.render(), 1000);
        } else if ((!busy || this.failed) && this.ticker !== null) {
            window.clearInterval(this.ticker);
            this.ticker = null;
        }
    }
}
