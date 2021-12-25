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

import $ from "jquery";
import {Parameter} from "./Parameter";
import {log} from "./utils";

export abstract class AbstractParameter implements Parameter {
  paramName: string;
  paramElement: JQuery<HTMLElement>;

  protected constructor(paramName: string, paramElement: JQuery<HTMLElement>) {
    this.paramName = paramName
    this.paramElement = paramElement
  }

  getParameterValue($element: JQuery<HTMLElement>): string {
    const paramElement = $element.get(0)
    if (!paramElement) {
      throw new Error(`Cannot get parameter value for ${this.paramName} due to non existent (null/undefined) element.`)
    }
    const attributeName: string = paramElement.getAttribute('name') || ''
    const tagName: string = paramElement.tagName
    const type: string = paramElement.getAttribute('type') || ''
    if (attributeName === 'value') {
      const value = this.getElementValue($element)
      if (typeof(value) === 'string') {
        return value
      }
      log(`Incompatible parameter type ${typeof(value)} for the attribute value.`)
    }

    if (tagName === 'DIV') {
      const subElements = $element.find('[name="value"]')
      if (!subElements) {
        return ''
      }
      const valueBuffer: any[] = []
      subElements.each(() => {
        const tempValue = this.getElementValue($element)
        if (tempValue)
          valueBuffer.push(tempValue)
      })
      return valueBuffer.toString()
    }

    if (type === 'file') {
      if (!paramElement || !(paramElement instanceof HTMLInputElement)) {
        return ''
      }
      const filesList = (paramElement as HTMLInputElement).files
      if (filesList && filesList.length > 0) {
        const firstFile = filesList[0] // ignoring other files... but we could use it...
        return firstFile.name
      }
    }

    if (tagName === 'INPUT' && !['', 'name'].includes(attributeName)) {
      const value : string | string[] = this.getElementValue($element)
      if (typeof(value) === 'string') {
        return value
      }
      log(`Incompatible parameter type ${typeof(value)} for an INPUT tag.`)
    }
    return ''
  }

  getElementValue (element: JQuery<HTMLElement>): string | string[] {
    if (element.prop('tagName') === 'SELECT') {
      return this.getSelectValues(element)
    }
    if (element.attr('type') === 'checkbox' || element.attr('type') === 'radio') {
      return element.prop('checked') ? element.val() as string : ''
    }
    return element.val() as string
  }

  getSelectValues (select: JQuery<HTMLElement>): string[] {
    if (!select) {
      return []
    }
    const options: JQuery<HTMLOptionElement> = select.children('option:selected') as JQuery<HTMLOptionElement>
    if (!options) {
      return []
    }
    const values: string[] = []
    for (const option of options) {
      if (option instanceof HTMLOptionElement) {
        values.push(option.value || option.text)
      }
    }
    return values
  }

  fakeSelectRadioButton (clazzName: string, id: string): void {
    const element = $(`#${id}`).get(0)
    if (element != null) {
      // deselect all radios with the class=clazzName
      const radios = $('input[class="' + clazzName + '"]')
      radios.each(() => {
        $(this).attr('name', '')
      })
      // select the radio with the id=id
      const parent = element.parentNode
      if (parent) {
        const children = parent.childNodes
        for (const child of children) {
          if ((child as any).className === clazzName) {
            (child as any).name = 'value'
          }
        }
      }
    }
  }
}
