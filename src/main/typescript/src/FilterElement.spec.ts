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

import {beforeEach, describe, it} from 'mocha';
import {expect} from 'chai';
import {FilterElement} from './FilterElement';
import {initializeTestDom} from './test-utils';
import * as jsdom from 'jsdom';

describe('FilterElement', () => {
  beforeEach (initializeTestDom)
  it('constructor', () => {
    const paramElement = new jsdom.JSDOM(`
        <div>
          <div><!-- tbody[0] -->
            <div><!-- trs -->
              <div><!-- tds[0] -->
                <div><!-- inputs[0] -->
                  <input name="parameter" value="" />
                </div>
              </div>
            </div>
          </div>
        </div>
      `).window.document.querySelector('div')
    const filterElement = document.createElement('input')
    // @ts-ignore
    const filter = new FilterElement(paramElement, filterElement, 0)
    expect(filter.paramElement).to.equal(paramElement)
    expect(filter.filterElement).to.equal(filterElement)
    expect(filter.filterLength).to.equal(0)
  })
  it('clear filter element', () => {
    const paramElement = new jsdom.JSDOM(`
        <div>
          <div><!-- tbody[0] -->
            <div><!-- trs -->
              <div><!-- tds[0] -->
                <div><!-- inputs[0] -->
                  <input name="parameter" value="" />
                </div>
              </div>
            </div>
          </div>
        </div>
      `).window.document.querySelector('div')
    const filterElement = document.createElement('input')
    filterElement.value = '123'
    // @ts-ignore
    const filter = new FilterElement(paramElement, filterElement, 0)
    filter.setOriginalArray([1, 2, 3])
    expect(filterElement.value).to.equal('')
    expect(filter.originalArray).to.deep.equal([1, 2, 3])
  })
  it('set original array', () => {
    const paramElement = new jsdom.JSDOM(`
        <div>
          <div><!-- tbody[0] -->
            <div><!-- trs -->
              <div><!-- tds[0] -->
                <div><!-- inputs[0] -->
                  <input name="parameter" value="" />
                </div>
              </div>
            </div>
          </div>
        </div>
      `).window.document.querySelector('div')
    const filterElement = document.createElement('input')
    filterElement.value = '123'
    // @ts-ignore
    const filter = new FilterElement(paramElement, filterElement, 0)
    filter.clearFilterElement()
    expect(filterElement.value).to.equal('')
  })
  it('filter SELECT', () => {
    const paramElement = document.createElement('select')
    const createOption = (value: string) => {
      const option = document.createElement('option')
      option.value = value
      return option
    }
    paramElement.appendChild(createOption('1234'))
    paramElement.appendChild(createOption('9999'))
    const filterElement = document.createElement('input')
    const filter = new FilterElement(paramElement, filterElement, 0)
    expect(filter.originalArray.length).to.equal(2)
  })
  describe('filter DIV', () => {
    it('when Jenkins used TABLEs', () => {
      const paramElement = new jsdom.JSDOM(`
        <div>
          <table>
            <tbody>
              <tr>
                <td>
                  <input name="parameter" value="" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      `).window.document.querySelector('div')
      if (paramElement === null) {
        throw new Error('Missing parameter element!')
      }
      const filterElement = document.createElement('input')
      const filter = new FilterElement(paramElement, filterElement, 0)
      expect(filter.originalArray.length).to.equal(1)
    })
    it('when Jenkins switched to DIVs', () => {
      const paramElement = new jsdom.JSDOM(`
        <div>
          <div><!-- tbody[0] -->
            <div><!-- trs -->
              <div><!-- tds[0] -->
                <div><!-- inputs[0] -->
                  <input name="parameter" value="" />
                </div>
              </div>
            </div>
          </div>
        </div>
      `).window.document.querySelector('div')
      if (paramElement === null) {
        throw new Error('Missing parameter element!')
      }
      const filterElement = document.createElement('input')
      const filter = new FilterElement(paramElement, filterElement, 0)
      expect(filter.originalArray.length).to.equal(1)
    })
    it('throws error for invalid children tag names', () => {
      const paramElement = new jsdom.JSDOM(`
        <div>
          <p><!-- tbody[0] -->
            <div><!-- trs -->
              <div><!-- tds[0] -->
                <div><!-- inputs[0] -->
                  <input name="parameter" value="" />
                </div>
              </div>
            </div>
          </p>
        </div>
      `).window.document.querySelector('div')
      const filterElement = document.createElement('input')
      // @ts-ignore
      expect(() => new FilterElement(paramElement, filterElement, 0)).to.throw(Error)
    })
    it('throws error for childless parameter', () => {
      const paramElement = new jsdom.JSDOM(`<div></div>`).window.document.querySelector('div')
      const filterElement = document.createElement('input')
      // @ts-ignore
      expect(() => new FilterElement(paramElement, filterElement, 0)).to.throw(Error)
    })
  })
})
