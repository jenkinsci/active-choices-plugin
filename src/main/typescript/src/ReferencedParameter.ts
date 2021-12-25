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

import {CascadeParameter} from "./CascadeParameter"
import {log} from "./utils"
import {AbstractParameter} from "./AbstractParameter";
import ChangeEvent = JQuery.ChangeEvent;

export class ReferencedParameter extends AbstractParameter {
  cascadeParameter: CascadeParameter

  constructor(paramName: string, paramElement: JQuery<HTMLElement>, cascadeParameter: CascadeParameter) {
    super(paramName, paramElement)
    this.cascadeParameter = cascadeParameter
    jQuery(this.paramElement).on('change', (e: ChangeEvent) => {
      if ((e as any).parameterName === this.paramName) {
        log('Skipping self reference to avoid infinite loop!')
        e.stopImmediatePropagation()
      } else {
        log('Cascading changes from parameter ' + this.paramName + '...')
        // _self.cascadeParameter.loading(true)
        jQuery(".behavior-loading").show()
        // start updating in separate async function so browser will be able to repaint and show 'loading' animation , see JENKINS-34487
        setTimeout(() => {
          this.cascadeParameter.update(false)
          jQuery(".behavior-loading").hide()
        }, 0)
      }
    })
    cascadeParameter.reference(this)
  }

  getParameterValue(htmlParameter: JQuery<HTMLElement>): string {
    throw new Error("Method not implemented.");
  }

  getElementValue(htmlParameter: JQuery<HTMLElement>): string | string[] {
    throw new Error("Method not implemented.");
  }

  getSelectValues(select: JQuery<HTMLElement>): string[] {
    throw new Error("Method not implemented.");
  }

  fakeSelectRadioButton(clazzName: string, id: string): void {
    throw new Error("Method not implemented.");
  }
}
