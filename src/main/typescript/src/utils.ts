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

export const log = (message: string): void => {
  // tslint:disable-next-line:no-console
  console.log(message)
}

/**
 * Fake selects a radio button.
 *
 * In Jenkins, parameters in general have two main HTML elements. One which name is
 * name with the value as the parameter name. And the other which name is value and
 * with the value as the parameter value. For example:
 *
 * <div class='parameter' id='parameter1'>
 *   <div name='name'  value='parameter1' />
 *   <div name='value' value='Sao Paulo'  />
 * </div>
 *
 * This code ensures that only one radio button, in a radio group, contains the name
 * value. Avoiding several values to be submitted.
 *
 * @param clazzName the HTML class of the radio elements
 * @param id HTML element ID
 * @see issue #21 in GitHub
 */
export function fakeSelectRadioButton (clazzName: string, id: string): void {
  const element = $(`#${id}`).get(0)
  if (element != null) {
    // deselect all radios with the class=clazzName
    const radios = $(`input[class="${clazzName}"]`).get()
    for (const input of radios) {
      input.setAttribute('name', '')
    }
    // select the radio with the id=id
    const parent = element.parentNode
    if (parent != null) {
      for (const child of parent.children) {
        if (child.className === clazzName) {
          child.setAttribute('name', 'value')
        }
      }
    }
  }
}
