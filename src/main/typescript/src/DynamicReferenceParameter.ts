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
import {JenkinsProxy} from "./Proxy";

export class DynamicReferenceParameter extends CascadeParameter {
  constructor(paramName: string, paramElement: JQuery<HTMLElement>, randomName: string, proxy: JenkinsProxy, cascadeParameters: CascadeParameter[]) {
    super(paramName, paramElement, randomName, proxy, cascadeParameters)
  }

  public update(avoidRecursion: boolean = false) {
    const paramElement = this.paramElement.get(0)
    if (!paramElement) {
      throw new Error(`The dynamic reference parameter ${this.paramName} could not locate the parameter HTML element in the UI (null/undefined).`)
    }
    const parametersString = this.getReferencedParametersAsText(); // gets the array parameters, joined by , (e.g. a,b,c,d)
    log('Values retrieved from Referenced Parameters: ' + parametersString)
    // Update the Map of parameters
    this.proxy.doUpdate(parametersString)
    // Here depending on the HTML element we might need to call a method to return a Map of elements,
    // or maybe call a string to put as value in a INPUT.
    if (paramElement.tagName === 'OL') {
      log('Calling Java server code to update HTML elements...')
      this.proxy.getChoicesForUI((t: XMLHttpRequest) => {
        jQuery(this.paramElement).empty(); // remove all children elements
        const choices = t.responseText
        log('Values returned from server: ' + choices)
        const data = JSON.parse(choices)
        const newValues = data[0]
        // var newKeys = data[1]
        for (const newValue of newValues) {
          const li = document.createElement('li')
          li.innerHTML = newValues
          paramElement.appendChild(li); // append new elements
        }
      })
    } else if (paramElement.tagName === 'UL') {
      jQuery(this.paramElement).empty(); // remove all children elements
      log('Calling Java server code to update HTML elements...')
      this.proxy.getChoicesForUI((t: XMLHttpRequest) => {
        const choices = t.responseText
        log('Values returned from server: ' + choices)
        const data = JSON.parse(choices)
        const newValues = data[0]
        // var newKeys = data[1]
        for (const newValue of newValues) {
          const li = document.createElement('li')
          li.innerHTML = newValue
          paramElement.appendChild(li) // append new elements
        }
      })
    } else if (paramElement.id.indexOf('inputElement_') > -1) { // handle input text boxes
      this.proxy.getChoicesAsStringForUI((t: XMLHttpRequest) => {
        (paramElement as HTMLInputElement).value = t.responseText
      })
    } else if (paramElement.id.indexOf('formattedHtml_') > -1) { // handle formatted HTML
      this.proxy.getChoicesAsStringForUI((t: XMLHttpRequest) => {
        const options = t.responseText
        paramElement.innerHTML = JSON.parse(options)
      })
    }
    // propagate change
    // log('Propagating change event from ' + this.getParameterName())
    // var e = jQuery.Event('change', {parameterName: this.getParameterName()})
    // jQuery(this.getParameterElement()).trigger(e)
    if (!avoidRecursion) {
      if (this.cascadeParameters && this.cascadeParameters.length > 0) {
        for (const other of this.cascadeParameters) {
          if (this.referencesMe(other)) {
            log(`Updating ${other.paramName} from ${this.paramName}`)
            other.update(true)
          }
        }
      }
    } else {
      log('Avoiding infinite loop due to recursion!')
    }
  }
}
