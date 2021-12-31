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

import {fakeSelectRadioButton, log} from "./utils";
import {afterEach, beforeEach, describe, it} from 'mocha';
import {expect} from 'chai';
import * as sinon from 'sinon';
import {initializeTestDom} from "./test-utils";
import $ from "jquery";

describe('utils', () => {
  describe('log', () => {
    let spy: any
    beforeEach(() => {
      spy = sinon.spy(console, 'log')
    })
    afterEach(() => {
      spy.restore()
    })
    it('log', () => {
      log('test')
      expect(spy.calledOnce).to.equal(true)
    })
  })
  describe('fakeSelectRadioButton', () => {
    beforeEach (initializeTestDom)
    it ('invalid element does not break anything', () => {
      const id = 'empty_element'
      fakeSelectRadioButton('', id)
    })
    it ('invalid element does not break anything', () => {
      $('body').append($(`
      <div class="parameter">
        <input name='name'  value='parameter1'     id="parameter1" />
        <input name='value' value='Sao Paulo'      class="radio"   />
      </div>
      <div class="parameter">
        <input name='name'  value='parameter2'     id="parameter2" />
        <input name='value' value='Rio de Janeiro' class="radio"   />
      </div>
      `))
      // This will set the name attribute to empty to all elements with class
      // "radio", then look for the one with ID "parameter2" and set its name
      // attribute to "value".
      fakeSelectRadioButton('radio', 'parameter2')
      expect($('#parameter1').parent().find('.radio').get(0)?.getAttribute('name')).to.equal('')
      expect($('#parameter2').parent().find('.radio').get(0)?.getAttribute('name')).to.equal('value')
    })
  })
})
